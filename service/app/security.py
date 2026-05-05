import threading
import time
from collections import deque
from dataclasses import dataclass

from fastapi import Header, HTTPException, Request, status

from .settings import settings


def _client_id(request: Request) -> str:
    forwarded = request.headers.get("x-forwarded-for")
    if forwarded:
        return forwarded.split(",")[0].strip()
    if request.client and request.client.host:
        return request.client.host
    return "unknown"


class InMemoryRateLimiter:
    def __init__(self, limit_per_minute: int):
        self.limit = limit_per_minute
        self._hits: dict[str, deque[float]] = {}
        self._lock = threading.Lock()

    def allow(self, key: str) -> bool:
        if self.limit <= 0:
            return True
        now = time.monotonic()
        cutoff = now - 60.0
        with self._lock:
            q = self._hits.setdefault(key, deque())
            while q and q[0] < cutoff:
                q.popleft()
            if len(q) >= self.limit:
                return False
            q.append(now)
            return True


rate_limiter = InMemoryRateLimiter(settings.rate_limit_per_minute)


def enforce_api_key(x_api_key: str | None = Header(default=None, alias="x-api-key")) -> None:
    if not settings.require_api_key:
        return
    if not x_api_key or x_api_key not in settings.api_keys:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Unauthorized",
        )


def enforce_rate_limit(request: Request) -> None:
    cid = _client_id(request)
    if not rate_limiter.allow(cid):
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many requests",
        )


@dataclass
class Metrics:
    total_requests: int = 0
    predict_requests: int = 0
    predict_errors: int = 0
    total_segments: int = 0
    total_elapsed_ms: float = 0.0
    cache_hits: int = 0
    cache_misses: int = 0


metrics = Metrics()
metrics_lock = threading.Lock()
