import logging
import json
import threading
from contextlib import nullcontext
import torch
import torch.nn as nn
import numpy as np

from .config import LABELS_FILE, LABELS_FALLBACK, MODELS_DIR
from .architectures import EfficientNetMel, CNNBiLSTMV2, ASTLiteV2
from ..settings import settings

logger = logging.getLogger(__name__)

class ModelManager:
    def __init__(self):
        self.device: torch.device = torch.device("cpu")
        self.class_names: list[str] = []
        self.n_classes: int = 0
        self.models: dict[str, nn.Module] = {}
        self.weights: dict[str, float] = {}   
        self.model_version: str = "unknown"
        self.ready: bool = False
        self._infer_lock = threading.Lock()
        self._serialize_inference = settings.serialize_inference

    def load(self) -> None:
        if settings.torch_num_threads > 0:
            torch.set_num_threads(settings.torch_num_threads)
        if settings.torch_interop_threads > 0:
            torch.set_num_interop_threads(settings.torch_interop_threads)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        logger.info(
            "Device: %s | torch_threads=%d interop=%d serialize_inference=%s",
            self.device,
            torch.get_num_threads(),
            torch.get_num_interop_threads(),
            self._serialize_inference,
        )

        if LABELS_FILE.exists():
            lbl = np.load(LABELS_FILE, allow_pickle=True)
            self.class_names = [str(c) for c in lbl["class_names"]]
        else:
            self.class_names = LABELS_FALLBACK
            logger.warning("labels.npz not found → dùng fallback labels (%d classes)", len(self.class_names))
        
        self.n_classes = len(self.class_names)
        logger.info("n_classes = %d", self.n_classes)

        model_cfgs = {
            "efficientnet": EfficientNetMel(self.n_classes),
            "cnnlstm"     : CNNBiLSTMV2(self.n_classes),
            "ast"         : ASTLiteV2(self.n_classes),
        }

        loaded_any = False
        for name, model in model_cfgs.items():
            path = MODELS_DIR / f"best_{name}.pth"
            if not path.exists():
                logger.warning("Không tìm thấy %s → bỏ qua", path)
                continue
            try:
                state = torch.load(path, map_location=self.device, weights_only=True)
                if any(k.startswith("module.") for k in state):
                    state = {k[7:]: v for k, v in state.items()}
                model.load_state_dict(state)
                model.eval().to(self.device)
                self.models[name] = model
                self.weights[name] = 1.0   
                loaded_any = True
                logger.info("✅ Loaded: %s", name)
            except Exception as e:
                logger.error("❌ Load %s failed: %s", name, e)

        results_path = MODELS_DIR / "training_results.json"
        if results_path.exists():
            try:
                with open(results_path) as f:
                    results = json.load(f)
                self.model_version = str(results.get("model_version", "training_results.json"))
                for name in list(self.models.keys()):
                    if name in results:
                        acc = results[name].get("best_val_acc", 1.0)
                        self.weights[name] = float(acc) ** 2   
                logger.info("Ensemble weights: %s", {k: f"{v:.4f}" for k, v in self.weights.items()})
            except Exception as e:
                logger.warning("Không đọc được training_results.json: %s", e)

        if not loaded_any:
            raise RuntimeError("Không load được bất kỳ model nào! Kiểm tra thư mục trained_models/")

        self.ready = True
        logger.info("ModelManager ready — %d model(s) loaded", len(self.models))

    @torch.inference_mode()
    def predict_proba(self, segments: list[dict]) -> np.ndarray:
        if not segments:
            raise ValueError("segments must not be empty")

        mel_batch  = np.stack([s["mel"]      for s in segments], axis=0)   
        comb_batch = np.stack([s["combined"] for s in segments], axis=0)  

        mel_tensor  = torch.from_numpy(mel_batch).unsqueeze(1).float().to(self.device)  
        comb_tensor = torch.from_numpy(comb_batch).float().to(self.device)               

        total_w   = sum(self.weights.values())
        if total_w <= 0:
            raise RuntimeError("invalid ensemble weights")
        probs_sum = None

        model_inputs = {
            "efficientnet": mel_tensor,
            "cnnlstm"     : comb_tensor,
            "ast"          : mel_tensor,
        }

        infer_context = self._infer_lock if self._serialize_inference else nullcontext()
        with infer_context:
            for name, model in self.models.items():
                w = self.weights[name] / total_w
                logits = model(model_inputs[name])           
                probs  = torch.softmax(logits, dim=-1).cpu().numpy()   
                probs_sum = w * probs if probs_sum is None else probs_sum + w * probs

        energies = np.array([float(s.get("energy", 1.0)) for s in segments], dtype=np.float64)
        energies = np.maximum(energies, 1e-6)
        energy_sum = energies.sum()
        if energy_sum <= 0:
            return probs_sum.mean(axis=0)
        weighted = (probs_sum * (energies / energy_sum)[:, None]).sum(axis=0)
        return weighted

# Singleton instance
manager = ModelManager()
