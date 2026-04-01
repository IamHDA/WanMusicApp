from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List

import numpy as np
import torch
import torch.nn.functional as F

from app.audio import AudioPreprocessor
from app.config import settings
from app.models.model_definitions import build_model

RESERVED_REPORT_KEYS = {'accuracy', 'macro avg', 'weighted avg'}


@dataclass
class LoadedModel:
    name: str
    model: torch.nn.Module


class MusicTaggerService:
    def __init__(self) -> None:
        self.device = torch.device('cpu')
        self.preprocessor = AudioPreprocessor(
            sample_rate=settings.sample_rate,
            n_mels=settings.n_mels,
            n_fft=settings.n_fft,
            hop_length=settings.hop_length,
            segment_seconds=settings.segment_seconds,
        )
        self.labels, self.results = self._load_labels_and_results(Path(settings.training_results_path))
        self.models: List[LoadedModel] = self._load_models()
        self.ensemble_weights = self._load_ensemble_weights()

    def predict_from_bytes(self, audio_bytes: bytes, top_k: int | None = None) -> Dict[str, object]:
        log_mel = self.preprocessor.to_log_mel(audio_bytes)
        with torch.inference_mode():
            x = torch.from_numpy(log_mel).unsqueeze(0).unsqueeze(0).to(self.device)
            logits_list = [loaded.model(x) for loaded in self.models]
            probs = self._merge_probabilities(logits_list)[0].detach().cpu().numpy()
        k = min(top_k or settings.top_k, len(self.labels))
        idx = np.argsort(probs)[::-1][:k]

        top_items = [{'label': self.labels[i], 'score': float(probs[i])} for i in idx]
        return {
            'predicted_label': top_items[0]['label'],
            'confidence': top_items[0]['score'],
            'top_k': top_items,
        }

    def _merge_probabilities(self, logits_list: List[torch.Tensor]) -> torch.Tensor:
        if len(logits_list) == 1:
            return F.softmax(logits_list[0], dim=-1)

        weights = []
        for loaded in self.models:
            w = self.ensemble_weights.get(loaded.name)
            if w is None:
                w = 1.0
            weights.append(float(w))

        total_weight = sum(weights)
        if total_weight <= 0:
            weights = [1.0] * len(self.models)
            total_weight = float(len(self.models))

        merged = None
        for logits, weight in zip(logits_list, weights):
            probs = F.softmax(logits, dim=-1)
            weighted_probs = probs * (weight / total_weight)
            merged = weighted_probs if merged is None else merged + weighted_probs
        assert merged is not None
        return merged

    def _load_models(self) -> List[LoadedModel]:
        if settings.enable_ensemble:
            requested = ['efficientnet', 'cnnlstm', 'ast']
            models = []
            for name in requested:
                path = Path('trained_models') / f'best_{name}.pth'
                models.append(LoadedModel(name=name, model=self._load_single_model(name, path)))
            return models

        return [LoadedModel(name=settings.model_name, model=self._load_single_model(settings.model_name, Path(settings.model_path)))]

    def _load_single_model(self, model_name: str, model_path: Path) -> torch.nn.Module:
        if not model_path.exists():
            raise FileNotFoundError(f'Checkpoint not found: {model_path}')

        model = build_model(model_name=model_name, num_classes=len(self.labels))
        state = torch.load(model_path, map_location=self.device)
        missing, unexpected = model.load_state_dict(state, strict=False)

        if missing:
            raise ValueError(
                f'Model {model_name} has {len(missing)} missing keys on load_state_dict. '
                'Map the architecture correctly in build_model.'
            )
        if unexpected:
            raise ValueError(
                f'Model {model_name} has {len(unexpected)} unexpected keys on load_state_dict. '
                'Map the architecture correctly in build_model.'
            )

        model.eval()
        model.to(self.device)
        return model

    def _load_labels_and_results(self, path: Path) -> tuple[List[str], Dict[str, object]]:
        if not path.exists():
            raise FileNotFoundError(f'File not found: {path}')

        data = json.loads(path.read_text(encoding='utf-8'))
        report = data.get('efficientnet', {}).get('report', {})
        labels = [k for k in report.keys() if k not in RESERVED_REPORT_KEYS]

        if not labels:
            raise ValueError('Cannot parse labels from training_results.json')

        return labels, data

    def _load_ensemble_weights(self) -> Dict[str, float]:
        weights = self.results.get('ensemble', {}).get('weights', {})
        if not isinstance(weights, dict):
            return {}
        return {str(k): float(v) for k, v in weights.items()}
