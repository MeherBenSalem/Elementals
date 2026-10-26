"""Renders the base video (footage only, no text) from edl.json: speed changes, eased camera moves,
punch-in cuts, impact shakes and a light grade. Frames are piped straight into ffmpeg."""
import json, math, subprocess
import cv2
import numpy as np

R = "/tmp/claude-0/reel"
E = json.load(open(f"{R}/edl.json"))
FPS = E["fps"]
W, H = E["size"]
DUR = json.load(open(f"{R}/timeline.json"))["duration"]
NF = int(round(DUR * FPS))


def ease(x):
    x = min(1.0, max(0.0, x))
    return x * x * (3 - 2 * x)


def ease_out(x):
    x = min(1.0, max(0.0, x))
    return 1 - (1 - x) ** 3


class Source:
    def __init__(self, path):
        self.cap = cv2.VideoCapture(path)
        self.idx = -1
        self.frame = None
        self.n = int(self.cap.get(cv2.CAP_PROP_FRAME_COUNT))

    def get(self, t):
        want = min(self.n - 1, max(0, int(round(t * FPS))))
        if want < self.idx or want > self.idx + 60:
            self.cap.set(cv2.CAP_PROP_POS_FRAMES, want)
            self.idx = want - 1
        while self.idx < want:
            ok, fr = self.cap.read()
            if not ok:
                break
            self.frame = fr
            self.idx += 1
        return self.frame


def cam_at(shot, t):
    keys = shot["cam"]
    if t <= keys[0][0]:
        k = keys[0]
        return k[1], k[2], k[3]
    for a, b in zip(keys, keys[1:]):
        if a[0] <= t <= b[0]:
            u = ease((t - a[0]) / (b[0] - a[0]))
            return tuple(a[i] + (b[i] - a[i]) * u for i in (1, 2, 3))
    k = keys[-1]
    return k[1], k[2], k[3]


def grade(img):
    f = img.astype(np.float32) / 255.0
    f = (f - 0.5) * 1.07 + 0.5 + 0.01
    gray = f.mean(axis=2, keepdims=True)
    f = gray + (f - gray) * 1.14
    return np.clip(f * 255.0, 0, 255).astype(np.uint8)


def main():
    sources = {}
    ff = subprocess.Popen(["ffmpeg", "-loglevel", "error", "-y", "-f", "rawvideo", "-pix_fmt", "bgr24", "-s", f"{W}x{H}",
                           "-r", str(FPS), "-i", "-", "-c:v", "libx264", "-preset", "medium", "-crf", "12",
                           "-pix_fmt", "yuv420p", f"{R}/base.mp4"], stdin=subprocess.PIPE)
    shots = E["shots"]
    for f in range(NF):
        t = f / FPS
        shot = next((s for s in shots if s["start"] <= t < s["end"]), shots[-1])
        src = sources.setdefault(shot["src"], Source(f"{R}/clips/{shot['src']}"))
        local = t - shot["start"]
        frame = src.get(shot["src_start"] + local * shot["speed"])
        s, cx, cy = cam_at(shot, t)
        if shot.get("punch", True) and local < 0.22:
            s *= 1 + 0.10 * (1 - ease_out(local / 0.22))
        dx = dy = 0.0
        for at, amp, dur in shot.get("shake", []):
            if at <= t < at + dur:
                k = (1 - (t - at) / dur) ** 2
                dx += amp * k * math.sin(t * 91.0) / s
                dy += amp * k * math.cos(t * 77.0) / s
        m = np.array([[s, 0, W / 2 - s * (cx + dx)], [0, s, H / 2 - s * (cy + dy)]], dtype=np.float32)
        out = cv2.warpAffine(frame, m, (W, H), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_REFLECT)
        ff.stdin.write(grade(out).tobytes())
        if f % 90 == 0:
            print(f"frame {f}/{NF} {shot['name']}", flush=True)
    ff.stdin.close()
    ff.wait()
    print("base.mp4 done")


main()
