import os
import time
import argparse
import logging
from pathlib import Path
from concurrent.futures import ProcessPoolExecutor, as_completed

import numpy as np
import pandas as pd
import librosa
from tqdm import tqdm
from sklearn.preprocessing import LabelEncoder

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)

DEFAULT_CSV = "dataset_clean.csv"
DEFAULT_OUT_DIR = "kaggle_data"
SAMPLE_RATE = 22_050
N_MELS = 128
N_MFCC = 40
N_FFT = 2048
HOP_LENGTH = 512
SEGMENT_SECONDS = 3.0
SEGMENT_HOP_SECONDS = 3.0
MAX_WORKERS = max(1, min(4, (os.cpu_count() or 2) - 1))


def frames_for_duration(seconds: float, sr: int, hop_length: int) -> int:
    return int(np.ceil(sr * seconds / hop_length)) + 1


FIXED_FRAMES: int = frames_for_duration(SEGMENT_SECONDS, SAMPLE_RATE, HOP_LENGTH)


def _fix_frames(arr: np.ndarray, fixed_frames: int) -> np.ndarray:
    t = arr.shape[1]
    if t >= fixed_frames:
        return arr[:, :fixed_frames]
    pad = fixed_frames - t
    return np.pad(arr, ((0, 0), (0, pad)), mode="constant")


def _norm(arr: np.ndarray) -> np.ndarray:
    mu = arr.mean(axis=1, keepdims=True)
    sd = arr.std(axis=1, keepdims=True) + 1e-8
    return (arr - mu) / sd


def extract_features_from_wave(
    y: np.ndarray,
    sr: int = SAMPLE_RATE,
    n_mels: int = N_MELS,
    n_mfcc: int = N_MFCC,
    n_fft: int = N_FFT,
    hop_length: int = HOP_LENGTH,
    fixed_frames: int = FIXED_FRAMES,
) -> dict | None:
    try:
        if y.size == 0:
            return None

        peak = np.max(np.abs(y))
        if peak < 1e-6:
            return None
        y = y / peak

        mel = librosa.feature.melspectrogram(
            y=y, sr=sr, n_fft=n_fft, hop_length=hop_length, n_mels=n_mels, fmax=8000
        )
        mel_db = librosa.power_to_db(mel, ref=np.max)
        mfcc = librosa.feature.mfcc(
            y=y, sr=sr, n_mfcc=n_mfcc, n_fft=n_fft, hop_length=hop_length
        )
        chroma = librosa.feature.chroma_stft(
            y=y, sr=sr, n_fft=n_fft, hop_length=hop_length
        )
        contrast = librosa.feature.spectral_contrast(
            y=y, sr=sr, n_fft=n_fft, hop_length=hop_length
        )
        y_harmonic = librosa.effects.harmonic(y)
        tonnetz = librosa.feature.tonnetz(y=y_harmonic, sr=sr)

        mel_f = _fix_frames(mel_db, fixed_frames)
        mfcc_f = _fix_frames(mfcc, fixed_frames)
        chroma_f = _fix_frames(chroma, fixed_frames)
        contrast_f = _fix_frames(contrast, fixed_frames)
        tonnetz_f = _fix_frames(tonnetz, fixed_frames)

        mel_norm = _norm(mel_f)
        mfcc_norm = _norm(mfcc_f)
        chroma_norm = _norm(chroma_f)
        contrast_norm = _norm(contrast_f)
        tonnetz_norm = _norm(tonnetz_f)

        combined = np.concatenate(
            [mel_norm, mfcc_norm, chroma_norm, contrast_norm, tonnetz_norm], axis=0
        )
        return {
            "mel": mel_norm.astype(np.float16),
            "combined": combined.astype(np.float16),
        }
    except Exception:
        return None


def extract_features(
    file_path: str,
    sr: int = SAMPLE_RATE,
    n_mels: int = N_MELS,
    n_mfcc: int = N_MFCC,
    n_fft: int = N_FFT,
    hop_length: int = HOP_LENGTH,
    fixed_frames: int = FIXED_FRAMES,
) -> dict | None:
    """
    Backward-compatible API used by local_test_ui.py.
    Extract one feature sample from the full input file.
    """
    try:
        try:
            y, _ = librosa.load(file_path, sr=sr, mono=True, res_type="soxr_hq")
        except Exception:
            y, _ = librosa.load(file_path, sr=sr, mono=True)
        return extract_features_from_wave(
            y=y,
            sr=sr,
            n_mels=n_mels,
            n_mfcc=n_mfcc,
            n_fft=n_fft,
            hop_length=hop_length,
            fixed_frames=fixed_frames,
        )
    except Exception:
        return None


def _worker_extract_segments(args: tuple) -> tuple:
    idx, file_path, seg_seconds, seg_hop_seconds, sr, n_mels, hop_length = args
    fixed_frames = frames_for_duration(seg_seconds, sr, hop_length)
    segment_samples = int(round(seg_seconds * sr))
    hop_samples = int(round(seg_hop_seconds * sr))
    out = []

    try:
        try:
            y, _ = librosa.load(file_path, sr=sr, mono=True, res_type="soxr_hq")
        except Exception:
            y, _ = librosa.load(file_path, sr=sr, mono=True)
    except Exception:
        return idx, out

    if len(y) < segment_samples:
        return idx, out

    max_start = len(y) - segment_samples
    starts = list(range(0, max_start + 1, hop_samples))
    for seg_id, start in enumerate(starts):
        seg = y[start : start + segment_samples]
        feat = extract_features_from_wave(
            y=seg,
            sr=sr,
            n_mels=n_mels,
            hop_length=hop_length,
            fixed_frames=fixed_frames,
        )
        if feat is None:
            continue
        out.append(
            {
                "segment_id": seg_id,
                "start_sec": float(start / sr),
                "mel": feat["mel"],
                "combined": feat["combined"],
            }
        )
    return idx, out


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Preprocess music dataset for Kaggle upload (segment-level)",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    p.add_argument("--csv", default=DEFAULT_CSV, help="Path to dataset CSV file")
    p.add_argument("--out-dir", default=DEFAULT_OUT_DIR, help="Output directory")
    p.add_argument("--sr", type=int, default=SAMPLE_RATE, help="Sample rate (Hz)")
    p.add_argument("--n-mels", type=int, default=N_MELS, help="Number of mel bands")
    p.add_argument("--hop-length", type=int, default=HOP_LENGTH, help="STFT hop length")
    p.add_argument("--segment-seconds", type=float, default=SEGMENT_SECONDS, help="Segment length")
    p.add_argument("--segment-hop-seconds", type=float, default=SEGMENT_HOP_SECONDS, help="Hop between segments")
    p.add_argument("--workers", type=int, default=MAX_WORKERS, help="Number of workers")
    p.add_argument("--dry-run", action="store_true", help="Process only first 20 clips per label")
    return p.parse_args()


def main() -> None:
    args = parse_args()

    csv_path = Path(args.csv)
    if not csv_path.exists():
        logger.error("CSV not found: %s", csv_path)
        logger.error("Run build_dataset.py and denoise_dataset.py first.")
        return

    if args.segment_seconds <= 0 or args.segment_hop_seconds <= 0:
        logger.error("segment-seconds and segment-hop-seconds must be > 0.")
        return

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    fixed_frames = frames_for_duration(args.segment_seconds, args.sr, args.hop_length)

    df = pd.read_csv(csv_path)
    logger.info("Loaded %d clips from %s", len(df), csv_path)

    missing = df["file_path"].apply(lambda p: not Path(p).exists())
    if missing.any():
        n_missing = int(missing.sum())
        logger.warning("Skip %d missing files", n_missing)
        df = df[~missing].reset_index(drop=True)

    if df.empty:
        logger.error("No valid WAV files found.")
        return

    if args.dry_run:
        df = df.groupby("label").head(20).reset_index(drop=True)
        logger.info("DRY-RUN: processing %d clips", len(df))

    le = LabelEncoder()
    le.fit(sorted(df["label"].unique()))
    n_classes = len(le.classes_)
    logger.info("Labels: %d", n_classes)

    tasks = [
        (
            i,
            str(row["file_path"]),
            args.segment_seconds,
            args.segment_hop_seconds,
            args.sr,
            args.n_mels,
            args.hop_length,
        )
        for i, row in df.iterrows()
    ]

    t0 = time.time()
    results_by_idx = {}
    with ProcessPoolExecutor(max_workers=args.workers) as executor:
        futures = {executor.submit(_worker_extract_segments, t): t[0] for t in tasks}
        with tqdm(total=len(futures), desc="Segment extraction", unit="clip", ncols=100) as pbar:
            for future in as_completed(futures):
                idx, segs = future.result()
                results_by_idx[idx] = segs
                pbar.update(1)
    elapsed = time.time() - t0

    mel_list: list[np.ndarray] = []
    comb_list: list[np.ndarray] = []
    y_list: list[int] = []
    group_list: list[int] = []
    meta_rows: list[dict] = []

    valid_files = 0
    for i, row in df.iterrows():
        segs = results_by_idx.get(i, [])
        if not segs:
            continue
        valid_files += 1
        label_id = int(le.transform([row["label"]])[0])
        for s in segs:
            mel_list.append(s["mel"])
            comb_list.append(s["combined"])
            y_list.append(label_id)
            group_list.append(i)
            meta_rows.append(
                {
                    "file_path": row["file_path"],
                    "label": row["label"],
                    "label_id": label_id,
                    "group_id": i,
                    "segment_id": int(s["segment_id"]),
                    "start_sec": float(s["start_sec"]),
                    "segment_seconds": float(args.segment_seconds),
                }
            )

    if not mel_list:
        logger.error("No segment features extracted. Check input audio quality.")
        return

    mel_array = np.stack(mel_list).astype(np.float16, copy=False)
    combined_array = np.stack(comb_list).astype(np.float16, copy=False)
    y = np.array(y_list, dtype=np.int64)
    groups = np.array(group_list, dtype=np.int64)
    meta_df = pd.DataFrame(meta_rows)

    mel_path = out_dir / "features_mel.npz"
    combined_path = out_dir / "features_combined.npz"
    labels_path = out_dir / "labels.npz"
    meta_path = out_dir / "metadata.csv"

    np.savez_compressed(mel_path, X=mel_array, y=y)
    np.savez_compressed(combined_path, X=combined_array, y=y)
    np.savez_compressed(
        labels_path,
        y=y,
        groups=groups,
        class_names=np.array([str(c) for c in le.classes_]),
        segment_seconds=np.array([float(args.segment_seconds)], dtype=np.float32),
        segment_hop_seconds=np.array([float(args.segment_hop_seconds)], dtype=np.float32),
        fixed_frames=np.array([int(fixed_frames)], dtype=np.int32),
    )
    meta_df.to_csv(meta_path, index=False)

    def _size_mb(p: Path) -> float:
        return p.stat().st_size / 1e6

    seg_per_file = len(meta_df) / max(valid_files, 1)
    print("\n" + "=" * 68)
    print("  PREPROCESS COMPLETED - SEGMENT MODE")
    print("=" * 68)
    print(f"  Input clips                : {len(df)}")
    print(f"  Valid clips (>=1 segment)  : {valid_files}")
    print(f"  Total segments             : {len(meta_df)}")
    print(f"  Avg segments / clip        : {seg_per_file:.2f}")
    print(f"  Segment length / hop       : {args.segment_seconds:.2f}s / {args.segment_hop_seconds:.2f}s")
    print(f"  Fixed frames               : {fixed_frames}")
    print(f"  n_classes                  : {n_classes}")
    print(f"  Feature mel shape          : {mel_array.shape}")
    print(f"  Feature combined shape     : {combined_array.shape}")
    print(f"  Elapsed                    : {elapsed:.1f}s")
    print()
    print(f"  features_mel.npz           : {_size_mb(mel_path):.1f} MB")
    print(f"  features_combined.npz      : {_size_mb(combined_path):.1f} MB")
    print(f"  labels.npz                 : {_size_mb(labels_path):.1f} MB")
    print(f"  metadata.csv               : {_size_mb(meta_path):.1f} MB")
    print()
    print(f"  Output dir                 : {out_dir.resolve()}")
    print("=" * 68 + "\n")


if __name__ == "__main__":
    main()
