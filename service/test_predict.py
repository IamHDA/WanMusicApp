import sys
import time
from pathlib import Path

import requests

API_URL = "http://localhost:8765/predict"

if len(sys.argv) > 1:
    files_to_test = [(sys.argv[1], "?")]
else:
    dataset = Path("dataset")
    files_to_test = []
    if dataset.exists():
        for genre_dir in sorted(dataset.iterdir()):
            if not genre_dir.is_dir():
                continue
            wavs = list(genre_dir.glob("*.wav"))[:1]
            if wavs:
                files_to_test.append((str(wavs[0]), genre_dir.name))
    files_to_test = files_to_test[:6]

print("=" * 65)
print("  MUSIC LABEL SERVICE - TEST")
print("=" * 65)

correct = 0
total = 0

for path, expected in files_to_test:
    print(f"\nFile     : {path}")
    print(f"Expected : {expected}")
    t0 = time.time()
    try:
        with open(path, "rb") as f:
            r = requests.post(API_URL, files={"file": f}, timeout=180)
        elapsed_total = time.time() - t0

        if not r.ok:
            print(f"HTTP {r.status_code}: {r.text[:200]}")
            total += 1
            continue

        data = r.json()
        top = data["top_labels"]
        predicted = data.get("predicted_label", top[0]["label"] if top else "unknown")

        print(f"Segments : {data['n_segments']} x 3s")
        print(f"Time     : {data['elapsed_ms']:.0f}ms (round-trip {elapsed_total * 1000:.0f}ms)")
        print(f"Predicted: {predicted}")
        print("Top predictions:")

        for i, lbl in enumerate(top, 1):
            bar = "#" * int(lbl["score"] * 35)
            pct = lbl["score"] * 100
            marker = " <- TOP-1" if i == 1 else ""
            print(f" {i:>2}. {lbl['label']:<22} {pct:5.1f}%  {bar}{marker}")

        if expected != "?" and predicted == expected:
            correct += 1
            print("Result   : correct")
        elif expected != "?":
            print(f"Result   : incorrect (expected {expected})")

        total += 1

    except requests.exceptions.Timeout:
        print("Timeout - inference took too long")
        total += 1
    except Exception as exc:
        print(f"Error: {exc}")
        total += 1

print("\n" + "=" * 65)
if total > 0 and files_to_test and files_to_test[0][1] != "?":
    print(f"Accuracy: {correct}/{total} = {correct / total * 100:.0f}%")
print("Done")
print("=" * 65)
