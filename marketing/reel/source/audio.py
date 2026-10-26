"""Original music bed and sound effects for the reel, synthesized from scratch (no samples, no licensing).

Writes music.wav and sfx.wav at 48 kHz, both exactly DURATION long. Every hit lands on the edit's
timeline (timeline.json): the drop on "522 NODES", impacts on the meteor shots and the title.
"""
import json
import numpy as np
import soundfile as sf
from scipy.signal import butter, sosfilt

SR = 48000
T = json.load(open("/tmp/claude-0/reel/timeline.json"))
DURATION = T["duration"]
N = int(DURATION * SR)
rng = np.random.default_rng(7)

BPM = 128.0
BEAT = 60.0 / BPM
DROP = 3.326  # "522 NODES"
GRID0 = DROP - 7 * BEAT  # a downbeat lands exactly on the drop


def t_axis(n):
    return np.arange(n) / SR


def lp(x, f, order=2):
    return sosfilt(butter(order, f, "low", fs=SR, output="sos"), x)


def hp(x, f, order=2):
    return sosfilt(butter(order, f, "high", fs=SR, output="sos"), x)


def bp(x, lo, hi, order=2):
    return sosfilt(butter(order, [lo, hi], "band", fs=SR, output="sos"), x)


def place(buf, sig, at, gain=1.0):
    i = int(round(at * SR))
    if i >= len(buf):
        return
    if i < 0:
        sig = sig[-i:]
        i = 0
    n = min(len(sig), len(buf) - i)
    buf[i:i + n] += sig[:n] * gain


def env_adsr(n, a=0.005, d=0.1, s=0.6, r=0.1):
    t = t_axis(n)
    dur = n / SR
    e = np.ones(n)
    e = np.where(t < a, t / max(a, 1e-6), e)
    e = np.where((t >= a) & (t < a + d), 1 - (1 - s) * (t - a) / max(d, 1e-6), e)
    e = np.where((t >= a + d) & (t < dur - r), s, e)
    e = np.where(t >= dur - r, s * np.clip((dur - t) / max(r, 1e-6), 0, 1), e)
    return e


def saw(freq, n, detune=0.0):
    t = t_axis(n)
    ph = (freq * (1 + detune)) * t
    return 2 * (ph - np.floor(ph + 0.5))


def note(m):
    return 440.0 * 2 ** ((m - 69) / 12)


# ---------------------------------------------------------------- instruments
def kick(gain=1.0):
    n = int(0.45 * SR)
    t = t_axis(n)
    f = 48 + 110 * np.exp(-t * 32)
    ph = 2 * np.pi * np.cumsum(f) / SR
    body = np.sin(ph) * np.exp(-t * 7.5)
    click = hp(rng.standard_normal(n), 2500) * np.exp(-t * 180) * 0.25
    return np.tanh((body + click) * 1.6) * gain


def clap(gain=1.0):
    n = int(0.3 * SR)
    t = t_axis(n)
    noise = bp(rng.standard_normal(n), 900, 5000)
    e = np.zeros(n)
    for k, off in enumerate([0, 0.011, 0.022]):
        e += np.where(t >= off, np.exp(-(t - off) * (90 if k < 2 else 22)), 0)
    return noise * e * 0.6 * gain


def hat(open_=False, gain=1.0):
    n = int((0.22 if open_ else 0.05) * SR)
    t = t_axis(n)
    x = hp(rng.standard_normal(n), 7000, 4)
    return x * np.exp(-t * (16 if open_ else 95)) * 0.32 * gain


def bass808(midi, dur, gain=1.0):
    n = int(dur * SR)
    t = t_axis(n)
    f = note(midi) * (1 + 0.6 * np.exp(-t * 40))
    ph = 2 * np.pi * np.cumsum(f) / SR
    x = np.sin(ph) + 0.12 * np.sin(2 * ph)
    return np.tanh(x * 1.8) * env_adsr(n, 0.004, 0.25, 0.75, 0.06) * 0.55 * gain


def pad(midis, dur, cutoff=1800, gain=1.0):
    n = int(dur * SR)
    x = np.zeros(n)
    for m in midis:
        for d in (-0.006, 0.0, 0.007):
            x += saw(note(m), n, d)
    x = lp(x / (3 * len(midis)), cutoff, 2)
    return x * env_adsr(n, 0.25, 0.3, 0.8, 0.35) * gain


def pluck(midi, dur=0.22, gain=1.0):
    n = int(dur * SR)
    t = t_axis(n)
    x = saw(note(midi), n) * 0.6 + saw(note(midi + 12), n, 0.003) * 0.3
    cutoff_env = 400 + 5200 * np.exp(-t * 18)
    # cheap time-varying low-pass: blend two filters by the envelope
    lo, hi = lp(x, 600), lp(x, 5600)
    k = (cutoff_env - 400) / 5200
    return (lo * (1 - k) + hi * k) * np.exp(-t * 11) * 0.42 * gain


def riser(dur, gain=1.0, f0=300, f1=6000):
    n = int(dur * SR)
    t = t_axis(n)
    noise = rng.standard_normal(n)
    out = np.zeros(n)
    steps = 24
    for s in range(steps):
        a, b = int(s * n / steps), int((s + 1) * n / steps)
        fc = f0 * (f1 / f0) ** (s / (steps - 1))
        out[a:b] = bp(noise[a:b], fc * 0.7, min(fc * 1.4, SR / 2 - 100))
    tone_f = 110 * (8 ** (t / dur))
    tone = np.sin(2 * np.pi * np.cumsum(tone_f) / SR) * 0.15
    e = (t / dur) ** 2.2
    return (out * 0.5 + tone) * e * gain


def impact(gain=1.0, length=2.2):
    n = int(length * SR)
    t = t_axis(n)
    f = 38 + 70 * np.exp(-t * 9)
    sub = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t * 2.2)
    crack = lp(rng.standard_normal(n), 3500) * np.exp(-t * 14) * 0.7
    tail = lp(rng.standard_normal(n), 900) * np.exp(-t * 2.8) * 0.25
    return np.tanh((sub * 1.3 + crack + tail) * 1.4) * gain


def whoosh(dur=0.35, gain=1.0, up=True):
    n = int(dur * SR)
    t = t_axis(n)
    noise = rng.standard_normal(n)
    out = np.zeros(n)
    steps = 12
    for s in range(steps):
        a, b = int(s * n / steps), int((s + 1) * n / steps)
        frac = s / (steps - 1)
        fc = 400 * (12 ** (frac if up else 1 - frac))
        out[a:b] = bp(noise[a:b], fc * 0.6, min(fc * 1.6, SR / 2 - 100))
    e = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 1.5
    return out * e * 0.35 * gain


def blip(freq=1600, dur=0.07, gain=1.0):
    n = int(dur * SR)
    t = t_axis(n)
    return np.sin(2 * np.pi * freq * t) * np.exp(-t * 60) * 0.25 * gain


def chime(base=72, gain=1.0):
    n = int(1.2 * SR)
    out = np.zeros(n)
    for k, m in enumerate([base, base + 4, base + 7, base + 12]):
        seg = np.zeros(n)
        i = int(k * 0.06 * SR)
        tt = t_axis(n - i)
        seg[i:] = (np.sin(2 * np.pi * note(m) * tt) + 0.3 * np.sin(2 * np.pi * note(m) * 2.01 * tt)) * np.exp(-tt * 3.2)
        out += seg
    return out * 0.16 * gain


def shimmer(dur=1.0, gain=1.0):
    n = int(dur * SR)
    t = t_axis(n)
    out = np.zeros(n)
    for m in (84, 88, 91, 96):
        out += np.sin(2 * np.pi * note(m) * t * (1 + 0.002 * np.sin(2 * np.pi * 6 * t)))
    return out * np.exp(-t * 2.5) * (1 - np.exp(-t * 30)) * 0.06 * gain


def thunder(gain=1.0):
    n = int(1.4 * SR)
    t = t_axis(n)
    crack = hp(rng.standard_normal(n), 1500) * np.exp(-t * 30) * 0.8
    rumble = lp(rng.standard_normal(n), 220, 4) * np.exp(-t * 2.4) * 2.2
    return np.tanh((crack + rumble) * 1.2) * 0.5 * gain


def swirl(dur=0.9, gain=1.0):
    n = int(dur * SR)
    t = t_axis(n)
    noise = rng.standard_normal(n)
    lfo = 0.5 + 0.5 * np.sin(2 * np.pi * 7 * t)
    lo, hi = bp(noise, 300, 900), bp(noise, 900, 2600)
    return (lo * lfo + hi * (1 - lfo)) * np.sin(np.pi * t / dur) * 0.32 * gain


# ---------------------------------------------------------------- music
music = np.zeros(N)
LINES = {l["i"]: l for l in T["lines"]}
TITLE = LINES[14]["start"]
CTA = LINES[15]["start"]
METEOR_HOOK = 0.85
METEOR_BIG = 11.67

# D minor: i - VI - III - VII  (Dm, Bb, F, C)
CHORDS = [[62, 65, 69], [58, 62, 65], [57, 60, 65], [60, 64, 67]]
ROOTS = [38, 34, 41, 36]
ARP = [[74, 77, 81, 77], [70, 74, 77, 74], [69, 72, 77, 72], [72, 76, 79, 76]]

beat_times = [GRID0 + k * BEAT for k in range(-8, int(DURATION / BEAT) + 4)]

# Hook: dark pad and drone under the meteor, riser into the drop.
place(music, pad([50, 57, 62], DROP + 0.4, 900, 0.5), 0.0)
place(music, riser(DROP - 0.9, 0.55), 0.9)
place(music, impact(0.9), METEOR_HOOK)

for k, bt in enumerate(beat_times):
    if bt < DROP - 1e-3 or bt > DURATION:
        continue
    beat_no = int(round((bt - DROP) / BEAT))
    bar = beat_no // 4
    pos = beat_no % 4
    chord = bar % 4
    in_title = TITLE - 0.05 <= bt < CTA - 0.02
    in_montage = 8.40 <= bt < 9.50
    # breakdown for the title card: pad and bass only
    if not in_title:
        if pos in (0, 2) or (in_montage):
            place(music, kick(0.95 if pos == 0 else 0.8), bt)
        if pos == 2 and not in_montage:
            place(music, clap(0.85), bt)
        for h in range(2):
            place(music, hat(False, 0.9 if h == 0 else 0.55), bt + h * BEAT / 2)
        if pos == 3 and bar % 2 == 1:
            for r in range(4):  # hat roll into the next bar
                place(music, hat(False, 0.5 + r * 0.1), bt + BEAT / 2 + r * BEAT / 8)
        # arp: 16th notes
        for s in range(4):
            place(music, pluck(ARP[chord][(pos * 4 + s) % 4] + (12 if s == 3 and pos == 3 else 0), 0.2, 0.75), bt + s * BEAT / 4)
    if pos == 0:
        place(music, bass808(ROOTS[chord], BEAT * (4 if not in_title else 4) - 0.02, 1.0), bt)
        place(music, pad(CHORDS[chord], BEAT * 4, 2200 if not in_title else 1200, 0.32), bt)

# drop-outs right before the big moments
def gap(at, length):
    i0, i1 = int((at - length) * SR), int(at * SR)
    fade = np.linspace(1, 0.12, max(1, int(0.03 * SR)))
    music[i0:i0 + len(fade)] *= fade
    music[i0 + len(fade):i1] *= 0.12

gap(DROP, 0.18)
gap(METEOR_BIG - 0.16, 0.12)
place(music, riser(2.0, 0.45), TITLE - 2.0)
place(music, impact(1.0, 2.6), TITLE)
place(music, impact(0.85), METEOR_BIG)
place(music, riser(DURATION - (CTA + 1.0), 0.35, 400, 5000), CTA + 1.0)  # leads back into the loop

# soft fade in/out so the loop seam is clean
fi, fo = int(0.04 * SR), int(0.12 * SR)
music[:fi] *= np.linspace(0, 1, fi)
music[-fo:] *= np.linspace(1, 0, fo)

# ---------------------------------------------------------------- sound effects
sfx = np.zeros(N)
SHOTS = json.load(open("/tmp/claude-0/reel/edl.json"))["shots"]
for s in SHOTS[1:]:
    if s.get("whoosh", True):
        place(sfx, whoosh(0.32, 0.8, up=True), s["start"] - 0.2)
place(sfx, blip(1300, 0.08, 1.0), 2.30)            # tree screen opens
for k in range(6):                                 # origin hovers
    place(sfx, blip(1700 + k * 120, 0.05, 0.6), 5.55 + k * 0.45)
for k in range(6):                                 # montage hits
    place(sfx, whoosh(0.16, 0.6, up=k % 2 == 0), 8.40 + k * 0.1833 - 0.05)
place(sfx, thunder(1.0), 9.62)
place(sfx, thunder(0.6), 10.1)
place(sfx, swirl(0.9, 1.0), 10.62)
place(sfx, shimmer(1.4, 1.0), 12.75)
place(sfx, blip(1100, 0.06, 0.9), 15.45)           # path hover
place(sfx, blip(900, 0.09, 1.0), 15.85)            # click to stage
place(sfx, blip(1500, 0.06, 0.8), 16.95)           # apply
place(sfx, chime(84, 0.8), 18.10)                  # code copied
place(sfx, chime(76, 1.0), 19.40)                  # RPG level up
place(sfx, whoosh(0.3, 0.7), 22.60)
place(sfx, whoosh(0.3, 0.7), 23.10)
place(sfx, whoosh(0.3, 0.7), 23.60)
place(sfx, shimmer(1.8, 1.2), TITLE + 0.1)
place(sfx, whoosh(0.45, 0.7, up=False), CTA - 0.1)


def norm(x, peak):
    return x / max(1e-9, np.abs(x).max()) * peak


sf.write("/tmp/claude-0/reel/music.wav", norm(music, 0.9).astype(np.float32), SR)
sf.write("/tmp/claude-0/reel/sfx.wav", norm(sfx, 0.9).astype(np.float32), SR)
print("music/sfx written", DURATION)
