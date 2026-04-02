import torch
import torch.nn as nn
import torch.nn.functional as F
import torchvision.models as tv_models
from .config import FIXED_FRAMES

# ── EfficientNet-B3 ──────────────────────────────────────────────────────────
class EfficientNetMel(nn.Module):
    def __init__(self, n_classes: int, dropout: float = 0.4):
        super().__init__()
        backbone      = tv_models.efficientnet_b3(weights=None)
        self.backbone = backbone.features
        self.pool     = nn.AdaptiveAvgPool2d(1)
        self.classifier = nn.Sequential(
            nn.Dropout(dropout),
            nn.Linear(1536, 512),
            nn.BatchNorm1d(512),
            nn.GELU(),
            nn.Dropout(dropout / 2),
            nn.Linear(512, n_classes),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = x.repeat(1, 3, 1, 1)
        x = self.backbone(x)
        return self.classifier(self.pool(x).flatten(1))


# ── CNN-BiLSTM ───────────────────────────────────────────────────────────────
class MultiScaleConv(nn.Module):
    def __init__(self, in_ch, out_ch):
        super().__init__()
        mid = out_ch // 3
        rem = out_ch - 2 * mid
        self.b3 = nn.Sequential(nn.Conv1d(in_ch, mid, 3, padding=1), nn.BatchNorm1d(mid), nn.GELU())
        self.b5 = nn.Sequential(nn.Conv1d(in_ch, mid, 5, padding=2), nn.BatchNorm1d(mid), nn.GELU())
        self.b7 = nn.Sequential(nn.Conv1d(in_ch, rem, 7, padding=3), nn.BatchNorm1d(rem), nn.GELU())

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


# ── AST-lite v2 ──────────────────────────────────────────────────────────────
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
        md       = int(dim * mlp_r)
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
    def __init__(self, n_classes, img_h=128, img_w=FIXED_FRAMES,
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
