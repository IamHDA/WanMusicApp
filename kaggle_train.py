"""
kaggle_train.py  ─  Vietnamese Music Genre Classification  v5
==============================================================
Chốt hạ Accuracy > 40%:
  ✅ BỎ resize chiều temporal (trở về 128x1293). Giữ full context âm thanh.
  ✅ BỎ WeightedRandomSampler (do overfit cực mạnh vào minority classes).
  ✅ Sử dụng Shuffle=True mặc định.
  ✅ Quay lại EfficientNet-B3 (vì B4 quá to, dễ bị overfitting với dataset siêu nhỏ 2619 samples).
  ✅ Giữ các config DataLoader siêu tốc của v4 (persistent_workers, prefetch_factor).
  ✅ Label Smoothing 0.15 phạt model bớt tự tin khi đoán sai.
"""

import os
import json
import time
import argparse
import logging
import sys
from pathlib import Path

import numpy as np
from sklearn.model_selection import StratifiedKFold, GroupKFold
from sklearn.metrics import classification_report

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader
from torch.amp import GradScaler, autocast
import torchvision.models as tv_models

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)


# ═══════════════════════════════════════════════════════════════════════════
# CONFIG
# ═══════════════════════════════════════════════════════════════════════════

def _is_notebook() -> bool:
    try:
        return get_ipython().__class__.__name__ in ("ZMQInteractiveShell", "Shell")  # type: ignore
    except NameError:
        return False


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    p.add_argument("--data-dir",   default="/kaggle/input/music-features")
    p.add_argument("--out-dir",    default="/kaggle/working")
    p.add_argument("--epochs",     type=int,   default=60)
    p.add_argument("--batch-size", type=int,   default=64)
    p.add_argument("--lr",         type=float, default=1e-4) # Base LR (more stable)
    p.add_argument("--weight-decay", type=float, default=5e-3)
    p.add_argument("--folds",      type=int,   default=5)
    p.add_argument("--train-fold", type=int,   default=0)
    p.add_argument("--splitter",   type=str,   default="group",
                   choices=["group", "stratified"])
    p.add_argument("--patience",   type=int,   default=10)
    p.add_argument("--min-epochs", type=int,   default=12,
                   help="Do not early-stop before this epoch")
    p.add_argument("--early-stop-metric", type=str, default="acc",
                   choices=["acc", "loss"])
    p.add_argument("--models",     nargs="+",
                   default=["efficientnet", "cnnlstm", "ast"],
                   choices=["efficientnet", "cnnlstm", "ast"])
    p.add_argument("--seed",       type=int,   default=42)
    p.add_argument("--fp16",       action="store_true", default=True)
    p.add_argument("--no-fp16",    dest="fp16", action="store_false")
    p.add_argument("--label-smoothing", type=float, default=0.05)
    p.add_argument("--mixup-prob", type=float, default=0.30)
    p.add_argument("--mixup-alpha", type=float, default=0.20)
    p.add_argument("--use-dp",     action="store_true",
                   help="Enable nn.DataParallel when >=2 GPUs are available")
    p.add_argument("--num-workers", type=int, default=2,
                   help="DataLoader workers (set 0 to avoid worker overhead/stalls)")
    p.add_argument("--prefetch-factor", type=int, default=2,
                   help="DataLoader prefetch_factor (only used when num_workers > 0)")
    p.add_argument("--pin-memory", action="store_true", default=True)
    p.add_argument("--no-pin-memory", dest="pin_memory", action="store_false")
    p.add_argument("--persistent-workers", action="store_true", default=True)
    p.add_argument("--no-persistent-workers", dest="persistent_workers", action="store_false")
    if _is_notebook():
        return p.parse_args([])
    return p.parse_args()


# ═══════════════════════════════════════════════════════════════════════════
# DATASET
# ═══════════════════════════════════════════════════════════════════════════

class MusicDataset(Dataset):
    """
    mode='mel'      → (1, F, T)   cho EfficientNet / AST
    mode='combined' → (F, T)      cho CNN-BiLSTM
    """
    def __init__(self, X: np.ndarray, y: np.ndarray,
                 mode: str = "mel", augment: bool = False):
        # Keep input dtype stable; main() already casts X to float32.
        # Avoid double-copy to reduce memory + CPU overhead.
        if X.dtype != np.float32:
            X = X.astype(np.float32)
        if y.dtype != np.int64:
            y = y.astype(np.int64)
        self.X       = torch.from_numpy(X)
        self.y       = torch.from_numpy(y)
        self.mode    = mode
        self.augment = augment

    def __len__(self) -> int:
        return len(self.y)

    def __getitem__(self, idx):
        # Clone only when we apply augmentation (in-place ops).
        x = self.X[idx]
        y = self.y[idx]
        if self.augment:
            x = x.clone()
            x = self._augment(x)
            
        if self.mode == "mel":
            x = x.unsqueeze(0)   # (1, 128, 1293)
        return x, y

    def _augment(self, x: torch.Tensor) -> torch.Tensor:
        T = x.shape[-1]
        F = x.shape[0] if x.dim() == 2 else x.shape[-2]
        # Noise
        if torch.rand(1) < 0.3:
            x = x + torch.randn_like(x) * 0.02
        # Time masking ×2
        for _ in range(2):
            if torch.rand(1) < 0.5:
                t = int(T * 0.08)
                s = torch.randint(0, max(1, T - t), (1,)).item()
                x[..., s:s + t] = 0.0
        # Freq masking ×2
        for _ in range(2):
            if torch.rand(1) < 0.5:
                f = int(F * 0.10)
                s = torch.randint(0, max(1, F - f), (1,)).item()
                if x.dim() == 2:
                    x[s:s + f, :] = 0.0
                else:
                    x[s:s + f, ...] = 0.0
        # Random gain
        if torch.rand(1) < 0.3:
            x = x * (0.8 + torch.rand(1).item() * 0.4)
        return x


def mixup_batch(x, y, alpha=0.4):
    lam  = float(np.random.beta(alpha, alpha))
    perm = torch.randperm(x.size(0), device=x.device)
    return lam * x + (1 - lam) * x[perm], y, y[perm], lam


def mixup_criterion(crit, pred, ya, yb, lam):
    return lam * crit(pred, ya) + (1 - lam) * crit(pred, yb)


# ═══════════════════════════════════════════════════════════════════════════
# MODEL 1: EfficientNet-B3 (Full Resolution)
# ═══════════════════════════════════════════════════════════════════════════

class EfficientNetMel(nn.Module):
    """
    EfficientNet-B3. Trả lại Full Resolution 128x1293 cho max Accuracy.
    """
    def __init__(self, n_classes: int, dropout: float = 0.4):
        super().__init__()
        backbone        = tv_models.efficientnet_b3(
            weights=tv_models.EfficientNet_B3_Weights.IMAGENET1K_V1)
        self.backbone   = backbone.features
        self.pool       = nn.AdaptiveAvgPool2d(1)
        self.classifier = nn.Sequential(
            nn.Dropout(dropout),
            nn.Linear(1536, 512), 
            nn.BatchNorm1d(512),
            nn.GELU(),
            nn.Dropout(dropout / 2),
            nn.Linear(512, n_classes),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = x.repeat(1, 3, 1, 1)  # 1-channel → 3-channel
        x = self.backbone(x)
        return self.classifier(self.pool(x).flatten(1))


# ═══════════════════════════════════════════════════════════════════════════
# MODEL 2: CNN-BiLSTM 
# ═══════════════════════════════════════════════════════════════════════════

class MultiScaleConv(nn.Module):
    def __init__(self, in_ch, out_ch):
        super().__init__()
        mid = out_ch // 3
        rem = out_ch - 2 * mid
        self.b3 = nn.Sequential(nn.Conv1d(in_ch, mid, 3, padding=1), nn.BatchNorm1d(mid),  nn.GELU())
        self.b5 = nn.Sequential(nn.Conv1d(in_ch, mid, 5, padding=2), nn.BatchNorm1d(mid),  nn.GELU())
        self.b7 = nn.Sequential(nn.Conv1d(in_ch, rem, 7, padding=3), nn.BatchNorm1d(rem),  nn.GELU())

    def forward(self, x):
        return torch.cat([self.b3(x), self.b5(x), self.b7(x)], 1)

class AttentionPool(nn.Module):
    def __init__(self, dim):
        super().__init__()
        self.w = nn.Linear(dim, 1)

    def forward(self, x):
        return (x * torch.softmax(self.w(x), 1)).sum(1)

class CNNBiLSTMV2(nn.Module):
    def __init__(self, n_classes, in_ch=193, dropout=0.35):
        super().__init__()
        self.cnn = nn.Sequential(
            MultiScaleConv(in_ch, 256), nn.MaxPool1d(2), nn.Dropout(0.2),
            nn.Conv1d(256, 256, 3, padding=1), nn.BatchNorm1d(256), nn.GELU(),
            nn.MaxPool1d(2), nn.Dropout(0.2),
            nn.Conv1d(256, 192, 3, padding=1), nn.BatchNorm1d(192), nn.GELU(),
            nn.MaxPool1d(2), nn.Dropout(0.2),
            nn.Conv1d(192, 128, 3, padding=1), nn.BatchNorm1d(128), nn.GELU(),
            nn.MaxPool1d(2),
        )
        self.lstm = nn.LSTM(128, 256, num_layers=2, bidirectional=True,
                            batch_first=True, dropout=dropout)
        self.attn = AttentionPool(512)
        self.head = nn.Sequential(
            nn.Dropout(dropout), nn.Linear(512, 256), nn.GELU(),
            nn.Dropout(dropout / 2), nn.Linear(256, n_classes),
        )

    def forward(self, x):
        x, _ = self.lstm(self.cnn(x).permute(0, 2, 1))
        return self.head(self.attn(x))


# ═══════════════════════════════════════════════════════════════════════════
# MODEL 3: AST-lite v2 
# ═══════════════════════════════════════════════════════════════════════════

class PatchEmbed(nn.Module):
    def __init__(self, img_h, img_w, ph, pw, dim):
        super().__init__()
        self.pad_w = (pw - img_w % pw) % pw
        self.n     = (img_h // ph) * ((img_w + self.pad_w) // pw)
        self.proj  = nn.Conv2d(1, dim, (ph, pw), stride=(ph, pw))

    def forward(self, x):
        if self.pad_w:
            x = F.pad(x, (0, self.pad_w))
        x = self.proj(x)
        return x.flatten(2).transpose(1, 2)

class TFBlock(nn.Module):
    def __init__(self, dim, heads, mlp_r=4.0, drop=0.1):
        super().__init__()
        self.n1  = nn.LayerNorm(dim)
        self.att = nn.MultiheadAttention(dim, heads, dropout=drop, batch_first=True)
        self.n2  = nn.LayerNorm(dim)
        md = int(dim * mlp_r)
        self.ff  = nn.Sequential(
            nn.Linear(dim, md), nn.GELU(), nn.Dropout(drop),
            nn.Linear(md, dim), nn.Dropout(drop),
        )

    def forward(self, x):
        xn = self.n1(x)
        a, _ = self.att(xn, xn, xn)
        x = x + a
        return x + self.ff(self.n2(x))

class ASTLiteV2(nn.Module):
    def __init__(self, n_classes, img_h=128, img_w=1293,
                 ph=16, pw=16, dim=256, depth=8, heads=8, drop=0.15):
        super().__init__()
        self.pe   = PatchEmbed(img_h, img_w, ph, pw, dim)
        self.cls  = nn.Parameter(torch.zeros(1, 1, dim))
        self.pos  = nn.Parameter(torch.zeros(1, self.pe.n + 1, dim))
        nn.init.trunc_normal_(self.cls, std=0.02)
        nn.init.trunc_normal_(self.pos, std=0.02)
        self.drop = nn.Dropout(drop)
        self.blks = nn.Sequential(*[TFBlock(dim, heads, drop=drop) for _ in range(depth)])
        self.norm = nn.LayerNorm(dim)
        self.head = nn.Sequential(nn.Dropout(drop), nn.Linear(dim, n_classes))

    def forward(self, x):
        B = x.size(0)
        x = self.pe(x)
        x = torch.cat([self.cls.expand(B, -1, -1), x], 1)
        x = self.drop(x + self.pos[:, :x.size(1)])
        x = self.norm(self.blks(x))
        return self.head(x[:, 0])


# ═══════════════════════════════════════════════════════════════════════════
# TRAINING ENGINE
# ═══════════════════════════════════════════════════════════════════════════

class EarlyStopping:
    def __init__(self, patience=10, mode="max", min_epochs=0):
        self.patience = patience
        self.mode = mode
        self.min_epochs = min_epochs
        self.best = None
        self.cnt = 0

    def __call__(self, value: float, epoch: int) -> bool:
        score = value if self.mode == "max" else -value
        if self.best is None or score > self.best + 1e-4:
            self.best = score
            self.cnt  = 0
        else:
            self.cnt += 1
        if epoch < self.min_epochs:
            return False
        return self.cnt >= self.patience


def train_epoch(model, loader, opt, crit, device, scaler, mixup_prob=0.30, mixup_alpha=0.20):
    model.train()
    tot_loss = tot_correct = tot_n = 0
    for x, y in loader:
        x = x.to(device, non_blocking=True)
        y = y.to(device, non_blocking=True)
        if float(torch.rand(1)) < mixup_prob:
            x, ya, yb, lam = mixup_batch(x, y, alpha=mixup_alpha)
        else:
            ya, yb, lam = y, y, 1.0

        opt.zero_grad(set_to_none=True)
        with autocast("cuda", enabled=scaler is not None):
            out  = model(x)
            loss = mixup_criterion(crit, out, ya, yb, lam) if lam < 1 else crit(out, ya)

        if not torch.isfinite(loss):
            continue

        if scaler:
            scaler.scale(loss).backward()
            scaler.unscale_(opt)
            nn.utils.clip_grad_norm_(model.parameters(), 1.0)
            scaler.step(opt); scaler.update()
        else:
            loss.backward()
            nn.utils.clip_grad_norm_(model.parameters(), 1.0)
            opt.step()

        tot_loss    += loss.item() * x.size(0)
        tot_correct += (out.argmax(1) == ya).sum().item()
        tot_n       += x.size(0)
    return tot_loss / tot_n, tot_correct / tot_n


@torch.no_grad()
def val_epoch(model, loader, crit, device, use_amp=False):
    model.eval()
    tot_loss = tot_correct = tot_n = 0
    preds_all, tgt_all = [], []
    for x, y in loader:
        x = x.to(device, non_blocking=True)
        y = y.to(device, non_blocking=True)
        with autocast("cuda", enabled=use_amp):
            out  = model(x)
            loss = crit(out, y)
        if not torch.isfinite(loss):
            continue
        p = out.argmax(1)
        tot_loss    += loss.item() * x.size(0)
        tot_correct += (p == y).sum().item()
        tot_n       += x.size(0)
        preds_all.extend(p.cpu().numpy())
        tgt_all.extend(y.cpu().numpy())
    if tot_n == 0:
        return float("inf"), 0.0, np.array([], dtype=np.int64), np.array([], dtype=np.int64)
    return tot_loss / tot_n, tot_correct / tot_n, np.array(preds_all), np.array(tgt_all)


def train_model(name, model, tr_loader, val_loader,
                args, device, n_classes, class_names, out_dir):
    logger.info("\n%s\n  TRAINING: %s\n%s", "═"*62, name.upper(), "═"*62)

    if args.use_dp and torch.cuda.device_count() > 1:
        logger.info("DataParallel: %d GPU", torch.cuda.device_count())
        model = nn.DataParallel(model)
    elif torch.cuda.device_count() > 1:
        logger.info("Multiple GPUs detected but DataParallel is OFF (use --use-dp to enable).")
    model = model.to(device)

    opt  = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=args.weight_decay)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(
        opt, T_max=args.epochs, eta_min=1e-6,
    )

    crit   = nn.CrossEntropyLoss(label_smoothing=args.label_smoothing)
    scaler = GradScaler("cuda") if args.fp16 and device.type == "cuda" else None

    es_mode = "max" if args.early_stop_metric == "acc" else "min"
    es        = EarlyStopping(patience=args.patience, mode=es_mode, min_epochs=args.min_epochs)
    best_acc  = 0.0
    best_p = best_t = None
    save_path = out_dir / f"best_{name}.pth"
    history   = {"train_loss": [], "train_acc": [], "val_loss": [], "val_acc": []}

    for ep in range(1, args.epochs + 1):
        t0 = time.time()
        tr_loss, tr_acc             = train_epoch(
            model, tr_loader, opt, crit, device, scaler,
            mixup_prob=args.mixup_prob, mixup_alpha=args.mixup_alpha,
        )
        vl_loss, vl_acc, preds, tgt = val_epoch(model, val_loader, crit, device, use_amp=(args.fp16 and device.type == "cuda"))
        scheduler.step()

        history["train_loss"].append(tr_loss)
        history["train_acc"].append(tr_acc)
        history["val_loss"].append(vl_loss)
        history["val_acc"].append(vl_acc)

        lr_now = opt.param_groups[0]["lr"]
        logger.info("Epoch %3d/%d | train=%.4f/%.3f | val=%.4f/%.3f | lr=%.2e | %.1fs",
                    ep, args.epochs, tr_loss, tr_acc, vl_loss, vl_acc, lr_now,
                    time.time() - t0)

        if preds.size > 0 and vl_acc > best_acc:
            best_acc = vl_acc
            best_p   = preds.copy()
            best_t   = tgt.copy()
            state    = model.module.state_dict() if hasattr(model, "module") else model.state_dict()
            torch.save(state, save_path)
            logger.info("  ✅ val_acc=%.4f → %s", best_acc, save_path.name)

        stop_value = vl_acc if args.early_stop_metric == "acc" else vl_loss
        if es(stop_value, ep):
            logger.info("  ⏹ Early stopping epoch %d", ep)
            break

    labels = list(range(n_classes))
    if best_p is None or best_t is None or len(best_t) == 0:
        report = {}
        logger.warning("No valid validation predictions were collected for %s.", name)
    else:
        report = classification_report(best_t, best_p,
                                       labels=labels, target_names=class_names,
                                       output_dict=True, zero_division=0)
        logger.info("\n%s", classification_report(best_t, best_p,
                                                  labels=labels, target_names=class_names,
                                                  zero_division=0))
    return {"model_name": name, "best_val_acc": best_acc,
            "history": history, "report": report, "save_path": str(save_path)}


# ═══════════════════════════════════════════════════════════════════════════
# ENSEMBLE
# ═══════════════════════════════════════════════════════════════════════════

@torch.no_grad()
def ensemble_predict(models_info, device, n_classes):
    all_probs = None
    total_w   = sum(m["weight"] for m in models_info)
    for info in models_info:
        m = info["model"].to(device).eval()
        w = info["weight"] / total_w
        probs = np.concatenate([
            torch.softmax(m(x.to(device)), 1).cpu().numpy()
            for x, _ in info["loader"]
        ])
        all_probs = w * probs if all_probs is None else all_probs + w * probs
    return all_probs.argmax(1)


# ═══════════════════════════════════════════════════════════════════════════
# MAIN
# ═══════════════════════════════════════════════════════════════════════════

def main() -> None:
    args    = parse_args()
    device  = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    torch.manual_seed(args.seed); np.random.seed(args.seed)
    if device.type == "cuda":
        torch.backends.cudnn.benchmark = True

    logger.info("v5 | Device=%s | GPU=%d | AMP=%s | Epochs=%d",
                device, torch.cuda.device_count(), args.fp16, args.epochs)

    data_dir = Path(args.data_dir)
    if not (data_dir / "features_mel.npz").exists():
        import subprocess as _sp
        r = _sp.run(["find", "/kaggle/input", "-name", "features_mel.npz", "-type", "f"],
                    capture_output=True, text=True, timeout=30)
        found = [l.strip() for l in r.stdout.splitlines() if l.strip()]
        if found:
            data_dir = Path(found[0]).parent
        else:
            logger.error("features_mel.npz not found!"); return

    logger.info("Loading from %s", data_dir)
    mel  = np.load(data_dir / "features_mel.npz",      allow_pickle=True)
    comb = np.load(data_dir / "features_combined.npz", allow_pickle=True)
    lbl  = np.load(data_dir / "labels.npz",            allow_pickle=True)

    X_mel  = mel["X"].astype(np.float32)
    X_comb = comb["X"].astype(np.float32)
    y      = lbl["y"].astype(np.int64)
    groups = lbl["groups"].astype(np.int64) if "groups" in lbl.files else np.arange(len(y), dtype=np.int64)
    cnames = [str(c) for c in lbl["class_names"]]
    nc     = len(cnames)
    _, _, T = X_mel.shape
    logger.info("N=%d  T=%d  n_classes=%d  n_groups=%d", len(y), T, nc, len(np.unique(groups)))

    if args.splitter == "group":
        uniq_groups = len(np.unique(groups))
        if uniq_groups < args.folds:
            logger.warning(
                "Not enough unique groups for GroupKFold: groups=%d < folds=%d. Fallback to stratified.",
                uniq_groups, args.folds
            )
            skf = StratifiedKFold(n_splits=args.folds, shuffle=True, random_state=args.seed)
            splits = list(skf.split(X_mel, y))
        else:
            gkf = GroupKFold(n_splits=args.folds)
            splits = list(gkf.split(X_mel, y, groups=groups))
    else:
        skf = StratifiedKFold(n_splits=args.folds, shuffle=True, random_state=args.seed)
        splits = list(skf.split(X_mel, y))

    tr_idx, val_idx = splits[args.train_fold]
    logger.info("Splitter=%s | Fold %d: train=%d  val=%d", args.splitter, args.train_fold, len(tr_idx), len(val_idx))

    # ── TỐI ƯU TỐC ĐỘ (nhưng bỏ resize để giữ full thông tin) ─────────
    # On Windows, dataloader workers use spawn → copy dataset memory per worker.
    # For large NPZ-loaded tensors, this can blow up RAM. Keep 0 workers by default.
    is_windows = sys.platform.startswith("win")
    if is_windows:
        n_workers = 0
    else:
        n_workers = max(0, args.num_workers)

    kw_base = {
        "num_workers": n_workers,
        "pin_memory": (args.pin_memory and device.type == "cuda"),
    }
    if n_workers > 0:
        kw_base["persistent_workers"] = bool(args.persistent_workers)
        kw_base["prefetch_factor"] = max(1, args.prefetch_factor)

    def make_dl(X, mode, bs=None):
        bs = bs or args.batch_size
        # V5: KHÔNG DÙNG Sampler nữa, shuffle=True
        tr = DataLoader(MusicDataset(X[tr_idx], y[tr_idx], mode, augment=True),
                        batch_size=bs, shuffle=True, **kw_base)
        vl = DataLoader(MusicDataset(X[val_idx], y[val_idx], mode, augment=False),
                        batch_size=bs, shuffle=False, **kw_base)
        return tr, vl

    mel_tr,  mel_vl  = make_dl(X_mel,  "mel")           # EfficientNet
    comb_tr, comb_vl = make_dl(X_comb, "combined")      # CNN-LSTM
    ast_tr,  ast_vl  = make_dl(X_mel,  "mel")           # AST

    cfgs = {
        "efficientnet": {"model": EfficientNetMel(nc),
                          "tr": mel_tr,  "vl": mel_vl},
        "cnnlstm":      {"model": CNNBiLSTMV2(nc),
                          "tr": comb_tr, "vl": comb_vl},
        "ast":          {"model": ASTLiteV2(nc, img_w=T),
                          "tr": ast_tr,  "vl": ast_vl},
    }

    for nm, c in cfgs.items():
        if nm in args.models:
            n_p = sum(p.numel() for p in c["model"].parameters()) / 1e6
            logger.info("%-16s: %.2f M params", nm, n_p)

    results = {}
    for nm in args.models:
        c      = cfgs[nm]
        result = train_model(nm, c["model"], c["tr"], c["vl"],
                             args, device, nc, cnames, out_dir)
        results[nm] = result

    if len(args.models) > 1:
        logger.info("\n%s\n  ENSEMBLE (Soft Voting)\n%s", "═"*62, "═"*62)
        ens_in = []
        for nm in args.models:
            c  = cfgs[nm]
            m  = c["model"]
            st = torch.load(out_dir / f"best_{nm}.pth", map_location=device)
            if any(k.startswith("module.") for k in st):
                st = {k[7:]: v for k, v in st.items()}
            m.load_state_dict(st)
            acc = results[nm]["best_val_acc"]
            ens_in.append({"model": m, "loader": c["vl"], "weight": acc ** 2})

        all_t   = y[val_idx]
        ens_p   = ensemble_predict(ens_in, device, nc)
        ens_acc = (ens_p == all_t).mean()
        logger.info("Ensemble val_acc: %.4f", ens_acc)
        logger.info("\n%s", classification_report(
            all_t, ens_p, labels=list(range(nc)), target_names=cnames, zero_division=0))
        results["ensemble"] = {
            "val_acc": float(ens_acc),
            "weights": {n: float(e["weight"]) for n, e in zip(args.models, ens_in)},
        }

    def cvt(o):
        if isinstance(o, np.ndarray):  return o.tolist()
        if isinstance(o, np.integer):  return int(o)
        if isinstance(o, np.floating): return float(o)
        raise TypeError

    rp = out_dir / "training_results.json"
    with open(rp, "w") as f:
        json.dump(results, f, indent=2, default=cvt)

    print("\n" + "═"*64)
    print("  RESULTS (v5)")
    print("═"*64)
    for nm in args.models:
        r = results[nm]
        print(f"  {nm:<16}  val_acc={r['best_val_acc']:.4f}")
    if "ensemble" in results:
        print(f"  {'ensemble':<16}  val_acc={results['ensemble']['val_acc']:.4f}")
    print(f"\n  📄 {rp}\n" + "═"*64)

if __name__ == "__main__":
    main()
