"""Measures the tree screen's zoom level in every rendered frame of tree_open (zoom is about the screen
centre), so the edit can retime the stepped keyboard zoom into one continuous move."""
import json
import cv2
import numpy as np

J = json.load(open("idx/tree_open.json"))
keep = {i: t for i, t in zip(J["idx"], J["t"]) if 0.64 <= t <= 2.6}
cap = cv2.VideoCapture("../raw/tree_open.mkv")
frames, n = [], 0
while True:
    ok, f = cap.read()
    if not ok:
        break
    if n in keep:
        g = cv2.resize(cv2.cvtColor(f, cv2.COLOR_BGR2GRAY), (540, 960), interpolation=cv2.INTER_AREA).astype(np.float32)
        frames.append((keep[n], cv2.GaussianBlur(g, (0, 0), 1.2)))
    n += 1
cx, cy = 270, 480
ys, xs = slice(80, 760), slice(30, 510)   # skip the header bar and the bottom panels


def err(a, b, s):
    m = np.array([[s, 0, cx - s * cx], [0, s, cy - s * cy]], np.float32)
    w = cv2.warpAffine(a, m, (540, 960), flags=cv2.INTER_LINEAR)
    return float(np.mean((w[ys, xs] - b[ys, xs]) ** 2))


logz, out = 0.0, [(frames[0][0], 0.0)]
for (ta, a), (tb, b) in zip(frames, frames[1:]):
    grid = np.linspace(0.80, 1.0, 201)
    e = [err(a, b, s) for s in grid]
    k = int(np.argmin(e))
    lo, hi = grid[max(0, k - 2)], grid[min(len(grid) - 1, k + 2)]
    fine = np.linspace(lo, hi, 41)
    ef = [err(a, b, s) for s in fine]
    s = float(fine[int(np.argmin(ef))])
    logz += np.log(s)
    out.append((tb, logz))
json.dump(out, open("idx/tree_zoom.json", "w"))
for t, l in out:
    print(f"{t:.3f} {np.exp(l):.4f}", end=" | ")
