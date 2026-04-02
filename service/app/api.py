import asyncio
import hashlib
import logging
import threading
import time
from pathlib import Path
from typing import Any

import numpy as np
from fastapi import APIRouter, Depends, File, HTTPException, Query, Request, UploadFile
from fastapi.concurrency import run_in_threadpool
from pydantic import BaseModel

from .ml.features import extract_segments_from_audio
from .ml.manager import manager
from .security import enforce_api_key, enforce_rate_limit, metrics, metrics_lock
from .settings import settings

logger = logging.getLogger(__name__)

router = APIRouter()
predict_semaphore = asyncio.Semaphore(settings.max_concurrent_predict)
ALLOWED_AUDIO_EXT = {".wav", ".mp3", ".flac", ".ogg", ".m4a", ".aac"}


class LabelScore(BaseModel):
    label: str
    score: float


class PredictResponse(BaseModel):
    predicted_label: str
    top_labels: list[LabelScore]
    n_segments: int
    elapsed_ms: float
    model_version: str


class PredictItem(BaseModel):
    filename: str
    ok: bool
    result: PredictResponse | None = None
    error: str | None = None


class BatchPredictResponse(BaseModel):
    total: int
    success: int
    failed: int
    items: list[PredictItem]


class HealthResponse(BaseModel):
    status: str
    models: list[str]
    n_classes: int
    device: str
    model_version: str


class _PredictionCache:
    def __init__(self, enabled: bool, max_size: int, ttl_sec: int):
        self.enabled = enabled
        self.max_size = max_size
        self.ttl_sec = ttl_sec
        self._store: dict[str, tuple[float, dict[str, Any]]] = {}
        self._order: list[str] = []
        self._lock = threading.Lock()

    def get(self, key: str) -> dict[str, Any] | None:
        if not self.enabled:
            return None
        now = time.monotonic()
        with self._lock:
            item = self._store.get(key)
            if item is None:
                return None
            expires_at, value = item
            if expires_at < now:
                self._store.pop(key, None)
                if key in self._order:
                    self._order.remove(key)
                return None
            if key in self._order:
                self._order.remove(key)
            self._order.append(key)
            return value

    def set(self, key: str, value: dict[str, Any]) -> None:
        if not self.enabled:
            return
        expires_at = time.monotonic() + self.ttl_sec
        with self._lock:
            if key in self._store and key in self._order:
                self._order.remove(key)
            self._store[key] = (expires_at, value)
            self._order.append(key)
            while len(self._order) > self.max_size:
                old = self._order.pop(0)
                self._store.pop(old, None)


predict_cache = _PredictionCache(
    enabled=settings.enable_predict_cache,
    max_size=settings.predict_cache_size,
    ttl_sec=settings.predict_cache_ttl_sec,
)


def _check_ready() -> None:
    if not manager.ready:
        raise HTTPException(status_code=503, detail="Models not loaded")


def _validate_upload_meta(file: UploadFile) -> None:
    content_type = (file.content_type or "").lower()
    suffix = Path(file.filename or "").suffix.lower()
    if content_type and not content_type.startswith("audio/") and content_type != "application/octet-stream":
        raise HTTPException(status_code=415, detail="Unsupported content type")
    if suffix and suffix not in ALLOWED_AUDIO_EXT:
        raise HTTPException(status_code=415, detail=f"Unsupported file extension: {suffix}")


async def _read_upload_limited(file: UploadFile, max_bytes: int) -> bytes:
    chunk_size = 1024 * 1024
    chunks: list[bytes] = []
    total = 0
    while True:
        chunk = await file.read(chunk_size)
        if not chunk:
            break
        total += len(chunk)
        if total > max_bytes:
            raise HTTPException(status_code=413, detail=f"File too large (> {settings.max_upload_mb}MB)")
        chunks.append(chunk)
    return b"".join(chunks)


async def _predict_from_file(file: UploadFile, top_k: int, min_confidence: float) -> PredictResponse:
    _validate_upload_meta(file)
    audio_bytes = await _read_upload_limited(file, settings.max_upload_mb * 1024 * 1024)
    if not audio_bytes:
        raise HTTPException(status_code=400, detail="Empty file")

    t0 = time.perf_counter()
    audio_hash = hashlib.sha256(audio_bytes).hexdigest()
    cache_key = f"{manager.model_version}:{settings.max_segments}:{top_k}:{min_confidence:.4f}:{audio_hash}"
    cached = predict_cache.get(cache_key)
    if cached is not None:
        elapsed_ms = (time.perf_counter() - t0) * 1000
        with metrics_lock:
            metrics.predict_requests += 1
            metrics.total_segments += int(cached["n_segments"])
            metrics.total_elapsed_ms += elapsed_ms
            metrics.cache_hits += 1
        return PredictResponse(
            predicted_label=str(cached["predicted_label"]),
            top_labels=[LabelScore(label=str(x["label"]), score=float(x["score"])) for x in cached["top_labels"]],
            n_segments=int(cached["n_segments"]),
            elapsed_ms=round(elapsed_ms, 1),
            model_version=str(cached["model_version"]),
        )

    with metrics_lock:
        metrics.cache_misses += 1

    try:
        segments = await run_in_threadpool(extract_segments_from_audio, audio_bytes, settings.max_segments)
    except HTTPException:
        raise
    except Exception:
        logger.exception("Feature extraction failed")
        raise HTTPException(status_code=422, detail="Cannot decode audio")

    if not segments:
        raise HTTPException(status_code=422, detail="No valid audio segments extracted")

    try:
        probs = await run_in_threadpool(manager.predict_proba, segments)
    except Exception:
        logger.exception("Inference failed")
        raise HTTPException(status_code=500, detail="Inference failure")

    top_k = min(top_k, manager.n_classes)
    top_idx = np.argsort(probs)[::-1][:top_k]
    top_labels = [
        LabelScore(label=manager.class_names[i], score=float(round(probs[i], 4)))
        for i in top_idx
    ]

    predicted_label = top_labels[0].label if top_labels else settings.unknown_label
    if top_labels and top_labels[0].score < min_confidence:
        predicted_label = settings.unknown_label

    elapsed_ms = (time.perf_counter() - t0) * 1000
    with metrics_lock:
        metrics.predict_requests += 1
        metrics.total_segments += len(segments)
        metrics.total_elapsed_ms += elapsed_ms

    if top_labels:
        logger.info(
            "Predict segments=%d top1=%s(%.3f) label=%s %.0fms",
            len(segments),
            top_labels[0].label,
            top_labels[0].score,
            predicted_label,
            elapsed_ms,
        )

    response = PredictResponse(
        predicted_label=predicted_label,
        top_labels=top_labels,
        n_segments=len(segments),
        elapsed_ms=round(elapsed_ms, 1),
        model_version=manager.model_version,
    )
    predict_cache.set(
        cache_key,
        {
            "predicted_label": response.predicted_label,
            "top_labels": [{"label": x.label, "score": x.score} for x in response.top_labels],
            "n_segments": response.n_segments,
            "model_version": response.model_version,
        },
    )
    return response


@router.get("/", tags=["system"])
def root() -> dict[str, str]:
    return {"message": "Music Label Classification API. See /docs for details."}


@router.get("/health", response_model=HealthResponse, tags=["system"])
def health() -> HealthResponse:
    _check_ready()
    return HealthResponse(
        status="ok",
        models=list(manager.models.keys()),
        n_classes=manager.n_classes,
        device=str(manager.device),
        model_version=manager.model_version,
    )


@router.get("/labels", tags=["system"])
def get_labels() -> dict[str, Any]:
    _check_ready()
    return {"labels": manager.class_names, "total": len(manager.class_names)}


@router.get("/metrics", tags=["system"])
def get_metrics() -> dict[str, float | int]:
    with metrics_lock:
        avg_latency = (metrics.total_elapsed_ms / metrics.predict_requests) if metrics.predict_requests else 0.0
        avg_segments = (metrics.total_segments / metrics.predict_requests) if metrics.predict_requests else 0.0
        cache_total = metrics.cache_hits + metrics.cache_misses
        cache_hit_rate = (metrics.cache_hits / cache_total) if cache_total else 0.0
        return {
            "total_requests": metrics.total_requests,
            "predict_requests": metrics.predict_requests,
            "predict_errors": metrics.predict_errors,
            "avg_latency_ms": round(avg_latency, 2),
            "avg_segments": round(avg_segments, 2),
            "cache_hits": metrics.cache_hits,
            "cache_misses": metrics.cache_misses,
            "cache_hit_rate": round(cache_hit_rate, 4),
        }


@router.post("/predict", response_model=PredictResponse, tags=["inference"])
async def predict(
    request: Request,
    file: UploadFile = File(..., description="Audio file"),
    top_k: int = Query(default=settings.default_top_k, ge=1, le=settings.max_top_k),
    min_confidence: float = Query(default=settings.min_confidence_default, ge=0.0, le=1.0),
    _auth: None = Depends(enforce_api_key),
):
    del _auth
    _check_ready()
    enforce_rate_limit(request)

    async with predict_semaphore:
        try:
            return await _predict_from_file(file, top_k=top_k, min_confidence=min_confidence)
        except HTTPException:
            with metrics_lock:
                metrics.predict_errors += 1
            raise
        except Exception:
            with metrics_lock:
                metrics.predict_errors += 1
            logger.exception("Unexpected failure")
            raise HTTPException(status_code=500, detail="Internal error")


@router.post("/predict/batch", response_model=BatchPredictResponse, tags=["inference"])
async def predict_batch(
    request: Request,
    files: list[UploadFile] = File(..., description="Audio files"),
    top_k: int = Query(default=settings.default_top_k, ge=1, le=settings.max_top_k),
    min_confidence: float = Query(default=settings.min_confidence_default, ge=0.0, le=1.0),
    _auth: None = Depends(enforce_api_key),
):
    del _auth
    _check_ready()
    enforce_rate_limit(request)

    if not files:
        raise HTTPException(status_code=400, detail="No files submitted")
    if len(files) > settings.max_batch_files:
        raise HTTPException(status_code=413, detail=f"Too many files (max {settings.max_batch_files})")

    async def _process_one(file: UploadFile) -> PredictItem:
        async with predict_semaphore:
            try:
                result = await _predict_from_file(file, top_k=top_k, min_confidence=min_confidence)
                return PredictItem(filename=file.filename or "unknown", ok=True, result=result)
            except HTTPException as exc:
                with metrics_lock:
                    metrics.predict_errors += 1
                return PredictItem(filename=file.filename or "unknown", ok=False, error=str(exc.detail))
            except Exception:
                logger.exception("Unexpected batch item failure")
                with metrics_lock:
                    metrics.predict_errors += 1
                return PredictItem(filename=file.filename or "unknown", ok=False, error="Internal error")

    tasks = [asyncio.create_task(_process_one(file)) for file in files]
    items = await asyncio.gather(*tasks)
    success = sum(1 for item in items if item.ok)
    failed = len(items) - success

    return BatchPredictResponse(total=len(files), success=success, failed=failed, items=items)
