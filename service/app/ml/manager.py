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

    @staticmethod
    def _strip_module_prefix(state: dict[str, torch.Tensor]) -> dict[str, torch.Tensor]:
        if any(k.startswith("module.") for k in state):
            return {k[7:]: v for k, v in state.items()}
        return state

    @staticmethod
    def _extract_head_out_features(model_name: str, state: dict[str, torch.Tensor]) -> int | None:
        key_map = {
            "efficientnet": ("classifier.5.weight", "classifier.5.bias"),
            "cnnlstm": ("head.4.weight", "head.4.bias"),
            "ast": ("head.1.weight", "head.1.bias"),
        }
        for key in key_map.get(model_name, ()):
            t = state.get(key)
            if t is None:
                continue
            if t.ndim == 2:
                return int(t.shape[0])
            if t.ndim == 1:
                return int(t.shape[0])
        return None

    @staticmethod
    def _read_class_names_from_training_results(results_path) -> list[str]:
        try:
            with open(results_path) as f:
                results = json.load(f)
            # Pick any model report and keep only per-class keys.
            excluded = {"accuracy", "macro avg", "weighted avg", "micro avg", "samples avg"}
            for model_name in ("efficientnet", "cnnlstm", "ast"):
                report = results.get(model_name, {}).get("report", {})
                class_names = [
                    k for k, v in report.items()
                    if k not in excluded and isinstance(v, dict) and "precision" in v and "recall" in v
                ]
                if class_names:
                    return class_names
        except Exception as e:
            logger.warning("Không đọc được class names từ training_results.json: %s", e)
        return []

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

        checkpoint_states: dict[str, dict[str, torch.Tensor]] = {}
        checkpoint_n_classes: dict[str, int] = {}
        for name in ("efficientnet", "cnnlstm", "ast"):
            path = MODELS_DIR / f"best_{name}.pth"
            if not path.exists():
                continue
            try:
                state = torch.load(path, map_location=self.device, weights_only=True)
                state = self._strip_module_prefix(state)
                checkpoint_states[name] = state
                out_dim = self._extract_head_out_features(name, state)
                if out_dim is not None:
                    checkpoint_n_classes[name] = out_dim
            except Exception as e:
                logger.warning("Không đọc được checkpoint %s để suy ra số lớp: %s", path, e)

        inferred_n_classes = None
        if checkpoint_n_classes:
            uniq_dims = sorted(set(checkpoint_n_classes.values()))
            if len(uniq_dims) == 1:
                inferred_n_classes = uniq_dims[0]
            else:
                logger.warning("Checkpoint có số lớp không đồng nhất: %s", checkpoint_n_classes)
                inferred_n_classes = max(set(checkpoint_n_classes.values()), key=list(checkpoint_n_classes.values()).count)
            logger.info("Inferred n_classes from checkpoints: %d", inferred_n_classes)

        results_path = MODELS_DIR / "training_results.json"
        if LABELS_FILE.exists():
            lbl = np.load(LABELS_FILE, allow_pickle=True)
            self.class_names = [str(c) for c in lbl["class_names"]]
        else:
            names_from_results = self._read_class_names_from_training_results(results_path)
            if names_from_results:
                self.class_names = names_from_results
                logger.warning(
                    "labels.npz not found → dùng class names từ training_results.json (%d classes)",
                    len(self.class_names),
                )
            else:
                self.class_names = LABELS_FALLBACK
                logger.warning(
                    "labels.npz not found → dùng fallback labels (%d classes)",
                    len(self.class_names),
                )

        if inferred_n_classes is not None and len(self.class_names) != inferred_n_classes:
            original = len(self.class_names)
            if original > inferred_n_classes:
                self.class_names = self.class_names[:inferred_n_classes]
            else:
                for i in range(original, inferred_n_classes):
                    self.class_names.append(f"class_{i}")
            logger.warning(
                "Điều chỉnh class_names từ %d -> %d để khớp checkpoint",
                original,
                inferred_n_classes,
            )

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
                state = checkpoint_states.get(name)
                if state is None:
                    state = torch.load(path, map_location=self.device, weights_only=True)
                    state = self._strip_module_prefix(state)
                model.load_state_dict(state)
                model.eval().to(self.device)
                self.models[name] = model
                self.weights[name] = 1.0   
                loaded_any = True
                logger.info("✅ Loaded: %s", name)
            except Exception as e:
                logger.error("❌ Load %s failed: %s", name, e)

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
