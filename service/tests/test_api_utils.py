import io
import asyncio

import pytest
from starlette.datastructures import UploadFile

from app.api import _read_upload_limited, _validate_upload_meta
from app.security import InMemoryRateLimiter


def test_validate_upload_meta_accepts_audio_wav():
    f = UploadFile(filename="song.wav", file=io.BytesIO(b"abc"), headers={"content-type": "audio/wav"})
    _validate_upload_meta(f)


def test_validate_upload_meta_rejects_extension():
    f = UploadFile(filename="song.txt", file=io.BytesIO(b"abc"), headers={"content-type": "audio/wav"})
    with pytest.raises(Exception):
        _validate_upload_meta(f)


def test_read_upload_limited_blocks_large_file():
    f = UploadFile(filename="song.wav", file=io.BytesIO(b"x" * 10), headers={"content-type": "audio/wav"})
    with pytest.raises(Exception):
        asyncio.run(_read_upload_limited(f, max_bytes=5))


def test_rate_limiter_blocks_after_limit():
    limiter = InMemoryRateLimiter(limit_per_minute=2)
    assert limiter.allow("a")
    assert limiter.allow("a")
    assert not limiter.allow("a")
