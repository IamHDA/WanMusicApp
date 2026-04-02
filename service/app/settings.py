import os
from dataclasses import dataclass


def _get_bool(name: str, default: bool) -> bool:
    raw = os.getenv(name)
    if raw is None:
        return default
    return raw.strip().lower() in {"1", "true", "yes", "on"}


def _get_int(name: str, default: int, minimum: int | None = None) -> int:
    raw = os.getenv(name)
    if raw is None:
        value = default
    else:
        try:
            value = int(raw)
        except ValueError:
            value = default
    if minimum is not None:
        value = max(minimum, value)
    return value


def _get_float(name: str, default: float, minimum: float | None = None) -> float:
    raw = os.getenv(name)
    if raw is None:
        value = default
    else:
        try:
            value = float(raw)
        except ValueError:
            value = default
    if minimum is not None:
        value = max(minimum, value)
    return value


def _get_list(name: str, default: list[str]) -> list[str]:
    raw = os.getenv(name)
    if raw is None:
        return default
    return [item.strip() for item in raw.split(",") if item.strip()]


@dataclass(frozen=True)
class Settings:
    log_level: str
    allowed_origins: list[str]
    require_api_key: bool
    api_keys: set[str]
    max_upload_mb: int
    default_top_k: int
    max_top_k: int
    max_segments: int
    max_concurrent_predict: int
    max_batch_files: int
    rate_limit_per_minute: int
    unknown_label: str
    min_confidence_default: float
    torch_num_threads: int
    torch_interop_threads: int
    serialize_inference: bool
    enable_predict_cache: bool
    predict_cache_size: int
    predict_cache_ttl_sec: int
    gzip_min_size: int


def load_settings() -> Settings:
    origins = _get_list("ALLOWED_ORIGINS", ["*"])
    api_keys = set(_get_list("API_KEYS", []))
    return Settings(
        log_level=os.getenv("LOG_LEVEL", "INFO").upper(),
        allowed_origins=origins,
        require_api_key=_get_bool("REQUIRE_API_KEY", False),
        api_keys=api_keys,
        max_upload_mb=_get_int("MAX_UPLOAD_MB", 50, minimum=1),
        default_top_k=_get_int("DEFAULT_TOP_K", 7, minimum=1),
        max_top_k=_get_int("MAX_TOP_K", 10, minimum=1),
        max_segments=_get_int("MAX_SEGMENTS", 5, minimum=1),
        max_concurrent_predict=_get_int("MAX_CONCURRENT_PREDICT", 2, minimum=1),
        max_batch_files=_get_int("MAX_BATCH_FILES", 20, minimum=1),
        rate_limit_per_minute=_get_int("RATE_LIMIT_PER_MINUTE", 120, minimum=0),
        unknown_label=os.getenv("UNKNOWN_LABEL", "unknown"),
        min_confidence_default=_get_float("MIN_CONFIDENCE_DEFAULT", 0.0, minimum=0.0),
        torch_num_threads=_get_int("TORCH_NUM_THREADS", 0, minimum=0),
        torch_interop_threads=_get_int("TORCH_INTEROP_THREADS", 0, minimum=0),
        serialize_inference=_get_bool("SERIALIZE_INFERENCE", False),
        enable_predict_cache=_get_bool("ENABLE_PREDICT_CACHE", True),
        predict_cache_size=_get_int("PREDICT_CACHE_SIZE", 512, minimum=1),
        predict_cache_ttl_sec=_get_int("PREDICT_CACHE_TTL_SEC", 900, minimum=1),
        gzip_min_size=_get_int("GZIP_MIN_SIZE", 500, minimum=1),
    )


settings = load_settings()
