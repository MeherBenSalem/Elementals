"""Music bed and sound effects for the v2 reel, timed entirely from cues.json.

Changes from v1: no whoosh on every cut. The montage cuts land on the beat and each one gets the sound of
the spell on screen (fire, ice, light, void, thunder, earth); only three soft whooshes remain, at section
changes. The piece is 80 beats long so the groove continues across the loop seam.
"""
import json
import sys

import numpy as np
import soundfile as sf

sys.path.insert(0, "/tmp/claude-0/reel2/edit")
from synth import (SR, bass808, blip, chime, clap, earth, fireburst, hat, ice, impact, kick, pad, place, pluck,  # noqa: E402
                   radiant, riser, shimmer, swirl, thunder, void, whoosh, zap)

E = "/tmp/claude-0/reel2/edit"
C = json.load(open(f"{E}/cues.json"))
EV = C["events"]
DURATION = C["duration"]
N = int(round(DURATION * SR))
BEAT = C["beat"]
DROP = C["drop"]
TITLE, CTA = C["title"]
MONT0 = EV["montage"]
MONT1 = C["labels"][6]["t0"]  # tempest: end of the six-school run
IMPACT_BIG = EV["impact_big"]

music = np.zeros(N)

# D minor: i - VI - III - VII  (Dm, Bb, F, C)
CHORDS = [[62, 65, 69], [58, 62, 65], [57, 60, 65], [60, 64, 67]]
ROOTS = [38, 34, 41, 36]
ARP = [[74, 77, 81, 77], [70, 74, 77, 74], [69, 72, 77, 72], [72, 76, 79, 76]]

# Intro: dark pad under the hook, riser into the drop.
place(music, pad([50, 57, 62], DROP + 0.4, 900, 0.5), 0.0)
place(music, riser(DROP - 0.9, 0.55), 0.9)

k_first = int(np.ceil((0 - DROP) / BEAT))
for k in range(k_first, int((DURATION - DROP) / BEAT) + 1):
    bt = DROP + k * BEAT
    if bt < DROP - 1e-3 or bt > DURATION - 1e-3:
        continue
    beat_no = k
    bar, pos = beat_no // 4, beat_no % 4
    chord = bar % 4
    in_title = TITLE - 0.05 <= bt < CTA - 0.02
    in_montage = MONT0 - 0.01 <= bt < MONT1 - 0.01
    in_cta = bt >= CTA - 0.02
    if not in_title:
        if pos in (0, 2) or in_montage:
            place(music, kick(0.95 if pos == 0 else 0.8), bt)
        if pos == 2 or (in_montage and pos == 0 and bar % 2 == 1):
            place(music, clap(0.85 if not in_cta else 0.6), bt)
        for h in range(2):
            place(music, hat(False, 0.9 if h == 0 else 0.55), bt + h * BEAT / 2)
        if in_montage:
            place(music, hat(True, 0.45), bt + BEAT / 2)
        if pos == 3 and bar % 2 == 1:
            for r in range(4):  # hat roll into the next bar
                place(music, hat(False, 0.5 + r * 0.1), bt + BEAT / 2 + r * BEAT / 8)
        for s in range(4):  # 16th-note arp
            place(music, pluck(ARP[chord][(pos * 4 + s) % 4] + (12 if s == 3 and pos == 3 else 0), 0.2, 0.75),
                  bt + s * BEAT / 4)
    if pos == 0:
        place(music, bass808(ROOTS[chord], BEAT * 4 - 0.02, 1.0), bt)
        place(music, pad(CHORDS[chord], BEAT * 4, 2200 if not in_title else 1200, 0.32), bt)


def gap(at, length):
    """Short drop-out right before a big moment."""
    i0, i1 = int((at - length) * SR), int(at * SR)
    fade = np.linspace(1, 0.12, max(1, int(0.03 * SR)))
    music[i0:i0 + len(fade)] *= fade
    music[i0 + len(fade):i1] *= 0.12


gap(DROP, 0.18)
gap(IMPACT_BIG - 0.02, 0.14)
place(music, riser(1.5, 0.3, 500, 5000), MONT0 - 1.5)  # into the montage
place(music, riser(2.0, 0.45), TITLE - 2.0)
place(music, riser(DURATION - (CTA + 1.0), 0.35, 400, 5000), CTA + 1.0)  # leads back into the loop

fi, fo = int(0.04 * SR), int(0.12 * SR)
music[:fi] *= np.linspace(0, 1, fi)
music[-fo:] *= np.linspace(1, 0, fo)

# ---------------------------------------------------------------- sound effects
sfx = np.zeros(N)
place(sfx, impact(1.0), EV["impact_hook"])
place(sfx, blip(1300, 0.08, 1.0), EV["tree_open"])
place(sfx, impact(0.35, 1.4), DROP)
for k, t in enumerate(EV["origins"]):
    place(sfx, blip(1500 + k * 110, 0.05, 0.7), t)
for t, up in EV["whooshes"]:
    place(sfx, whoosh(0.34, 0.55, up=up), t)

ABILITY = {"fire": lambda: fireburst(0.9), "ice": lambda: ice(1.0), "radiant": lambda: radiant(0.9),
           "void": lambda: void(0.9), "thunder": lambda: thunder(0.8), "earth": lambda: earth(0.95)}
for lab in C["labels"]:
    if lab["sfx"] in ABILITY:
        place(sfx, ABILITY[lab["sfx"]](), lab["t0"] + 0.09)  # spells go off 0.1 s after each cut
for k, t in enumerate(EV["tempest_strikes"]):
    place(sfx, thunder(0.9 if k == 0 else 0.6), t + 0.02)
place(sfx, swirl(1.3, 1.0), C["labels"][7]["t0"] + 0.05)
place(sfx, impact(1.0), IMPACT_BIG)
place(sfx, shimmer(1.4, 1.0), EV["keystone_tip"])
place(sfx, blip(900, 0.09, 1.0), EV["plan"][0])
place(sfx, blip(1250, 0.06, 0.8), EV["plan"][1])
place(sfx, chime(79, 0.6), EV["plan"][2])
place(sfx, blip(1100, 0.07, 0.8), EV["share_open"])
place(sfx, chime(84, 0.8), EV["share_copy"])
place(sfx, chime(76, 1.0), EV["rpg_banner"])
place(sfx, zap(0.9), EV["combo_chain"] + 0.04)
place(sfx, impact(0.6, 1.6), EV["combo_meteor"])
place(sfx, fireburst(0.6), EV["combo_inferno"])
place(sfx, impact(1.0, 2.6), TITLE)
place(sfx, shimmer(1.8, 1.2), TITLE + 0.1)


def norm(x, peak):
    return x / max(1e-9, np.abs(x).max()) * peak


sf.write(f"{E}/music.wav", norm(music, 0.9).astype(np.float32), SR)
sf.write(f"{E}/sfx.wav", norm(sfx, 0.9).astype(np.float32), SR)
print("music/sfx written", DURATION)
