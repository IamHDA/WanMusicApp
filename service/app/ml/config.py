from pathlib import Path

# Paths
ROOT_DIR = Path(__file__).parent.parent.parent.parent
MODELS_DIR = ROOT_DIR / "trained_models"
LABELS_FILE = MODELS_DIR / "labels.npz"

# Fallback Labels
LABELS_FALLBACK = [
    "acoustic", "ballad", "bolero", "cai_luong", "cheo",
    "chill", "dan_ca", "edm", "hiphop", "house",
    "indie", "instrumental", "lofi", "nhac_cach_mang", "nhac_dan_toc",
    "nhac_hoc_bai", "nhac_phim_ost", "nhac_thieu_nhi", "nhac_thu_gian",
    "nhac_vang", "nhac_xua", "quan_ho", "rap", "remix",
    "rnb", "rock", "techno", "trap", "tru_tinh", "vpop"
]

# Audio Config
SAMPLE_RATE = 22_050
N_MELS = 128
N_MFCC = 40
N_FFT = 2048
HOP_LENGTH = 512
SEGMENT_SECS = 3.0
import math
FIXED_FRAMES = int(math.ceil(SAMPLE_RATE * SEGMENT_SECS / HOP_LENGTH)) + 1
