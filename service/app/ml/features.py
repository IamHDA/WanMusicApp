import io
import logging
import numpy as np
import librosa

from .config import SAMPLE_RATE, N_MELS, N_MFCC, N_FFT, HOP_LENGTH, SEGMENT_SECS, FIXED_FRAMES

logger = logging.getLogger(__name__)

def _fix_frames(arr: np.ndarray, fixed_frames: int) -> np.ndarray:
    t = arr.shape[1]
    if t >= fixed_frames:
        return arr[:, :fixed_frames]
    return np.pad(arr, ((0, 0), (0, fixed_frames - t)), mode="constant")

def _norm(arr: np.ndarray) -> np.ndarray:
    mu = arr.mean(axis=1, keepdims=True)
    sd = arr.std(axis=1, keepdims=True) + 1e-8
    return (arr - mu) / sd

def extract_features_from_wave(y: np.ndarray) -> dict | None:
    try:
        if y.size == 0:
            return None
        peak = np.max(np.abs(y))
        if peak < 1e-6:
            return None
        y = y / peak

        mel     = librosa.feature.melspectrogram(y=y, sr=SAMPLE_RATE, n_fft=N_FFT, hop_length=HOP_LENGTH, n_mels=N_MELS, fmax=8000)
        mel_db  = librosa.power_to_db(mel, ref=np.max)
        mfcc    = librosa.feature.mfcc(y=y, sr=SAMPLE_RATE, n_mfcc=N_MFCC, n_fft=N_FFT, hop_length=HOP_LENGTH)
        chroma  = librosa.feature.chroma_stft(y=y, sr=SAMPLE_RATE, n_fft=N_FFT, hop_length=HOP_LENGTH)
        contrast= librosa.feature.spectral_contrast(y=y, sr=SAMPLE_RATE, n_fft=N_FFT, hop_length=HOP_LENGTH)
        y_harm  = librosa.effects.harmonic(y)
        tonnetz = librosa.feature.tonnetz(y=y_harm, sr=SAMPLE_RATE)

        mel_f     = _fix_frames(mel_db,   FIXED_FRAMES)
        mfcc_f    = _fix_frames(mfcc,     FIXED_FRAMES)
        chroma_f  = _fix_frames(chroma,   FIXED_FRAMES)
        cont_f    = _fix_frames(contrast, FIXED_FRAMES)
        tonn_f    = _fix_frames(tonnetz,  FIXED_FRAMES)

        mel_norm   = _norm(mel_f).astype(np.float32)
        mfcc_norm  = _norm(mfcc_f).astype(np.float32)
        chr_norm   = _norm(chroma_f).astype(np.float32)
        cont_norm  = _norm(cont_f).astype(np.float32)
        tonn_norm  = _norm(tonn_f).astype(np.float32)

        combined = np.concatenate([mel_norm, mfcc_norm, chr_norm, cont_norm, tonn_norm], axis=0)
        # RMS energy helps down-weight weak/noisy segments during ensemble aggregation.
        rms_energy = float(np.sqrt(np.mean(np.square(y.astype(np.float64)))))
        return {"mel": mel_norm, "combined": combined, "energy": rms_energy}
    except Exception as e:
        logger.warning("Feature extraction error: %s", e)
        return None

def extract_segments_from_audio(audio_bytes: bytes, max_segments: int = 5) -> list[dict]:
    buf = io.BytesIO(audio_bytes)
    try:
        y, _ = librosa.load(buf, sr=SAMPLE_RATE, mono=True, res_type="kaiser_fast")
    except Exception:
        buf.seek(0)
        y, _ = librosa.load(buf, sr=SAMPLE_RATE, mono=True)

    seg_samples = int(SAMPLE_RATE * SEGMENT_SECS)
    hop_samples = seg_samples  # non-overlapping

    if len(y) < seg_samples:
        y_padded = np.pad(y, (0, seg_samples - len(y)))
        feat = extract_features_from_wave(y_padded)
        return [feat] if feat else []

    starts = list(range(0, len(y) - seg_samples + 1, hop_samples))

    if len(starts) > max_segments:
        idx = np.linspace(0, len(starts) - 1, num=max_segments, dtype=int)
        starts = [starts[i] for i in idx]

    segments: list[dict] = []
    for start in starts:
        seg = y[start : start + seg_samples]
        feat = extract_features_from_wave(seg)
        if feat:
            segments.append(feat)

    return segments
