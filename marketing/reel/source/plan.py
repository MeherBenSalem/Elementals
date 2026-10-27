"""Single source of truth for the v2 reel: voiceover placement, shots, time maps and every overlay/audio cue.

Everything sits on a 128 BPM grid anchored on the drop ("522 NODES"). The reel is exactly 80 beats long,
so the beat grid continues across the loop seam. Writes timeline.json, edl.json and cues.json.
"""
import json

E = "/tmp/claude-0/reel2/edit"
BPM = 128.0
BEAT = 60.0 / BPM
DROP = 3.326
DURATION = 80 * BEAT  # 37.5 s


def b(k):
    return round(DROP + k * BEAT, 4)


# ------------------------------------------------------------------ voiceover
VO_START = [0.15, 2.123, DROP, 5.432, 6.722, 8.54, 14.16, 15.56, 17.27, 18.85, 21.17, 23.62, 24.95, 28.70, 31.97, 34.36]
lines = json.load(open(f"{E}/lines.json"))

# ------------------------------------------------------------------ shots
# map: [[local_out_time, src_game_time], ...]; "linear" for UI clips (static between changes), "smooth" (monotone
# cubic) for gameplay so speed ramps ease in and out. cam: [[abs_t, scale, cx, cy], ...], a constant drift unless
# camEase: the loop ending eases out and the hook eases in, so the camera is at rest on both sides of the seam.
MONTAGE = [
    ("inferno_wave", "Inferno Wave", "Pyromancy", "#ff6a2a", "fire"),
    ("glacial_prison", "Glacial Prison", "Tidecalling", "#4f8dff", "ice"),
    ("radiant_nova", "Radiant Nova", "Verdance", "#6ee07a", "radiant"),
    ("eclipse", "Eclipse", "Umbral Arts", "#b26bff", "void"),
    ("thunderclap", "Thunderclap", "Stormcalling", "#6fe7ff", "thunder"),
    ("tremor", "Tremor", "Earthshaping", "#c9a05a", "earth"),
]


def ev(pairs, start, end, src0, src1):
    """Linear map that lands each source event (an instant UI change) exactly on its output time."""
    pts = [[0.0, src0]]
    for out_t, src_t in pairs:
        pts.append([round(out_t - start - 0.001, 4), round(src_t - 0.004, 4)])
        pts.append([round(out_t - start, 4), round(src_t + 0.002, 4)])
    pts.append([round(end - start, 4), src1])
    return pts


shots = []
# UI shots get no punch-in: on a still screen its settle would briefly cancel the camera drift.


def shot(name, src, start, end, mp, interp, cam, punch=0.0, shake=(), **kw):
    s = {"name": name, "src": src, "start": round(start, 4), "end": round(end, 4), "map": mp, "interp": interp,
         "cam": cam, "punch": punch, "shake": [list(x) for x in shake]}
    s.update(kw)
    shots.append(s)
    return s


IMPACT_HOOK = 0.40
shot("hook_meteor", "meteor", 0.0, 2.123, [[0, 5.05], [IMPACT_HOOK, 5.45], [2.123, 6.742]], "smooth",
     [[0.0, 1.15, 540, 840], [2.123, 1.26, 540, 800]], shake=[(IMPACT_HOOK, 26, 0.7)], camEase="in")

TREE_OPEN = 2.123 + (0.613 - 0.40)  # the screen pops open
ZOOM0, ZOOM1 = DROP, DROP + 1.4
shot("tree", "tree_open", 2.123, 5.432,
     [[0, 0.40], [TREE_OPEN - 2.123, 0.613], [ZOOM0 - 2.123 - 0.001, 1.29], [ZOOM0 - 2.123, 1.293],
      [ZOOM1 - 2.123, 2.42], [5.432 - 2.123, 3.0]], "linear",
     [[2.123, 1.10, 540, 960], [3.30, 1.0, 540, 960], [5.432, 1.05, 540, 960]],
     punch=0.06, zoomwarp={"t0": ZOOM0, "t1": ZOOM1, "track": "idx/tree_zoom.json", "ease": 2.5})

ORIGIN_EVENTS = [0.420, 0.980, 1.520, 2.073, 2.647, 3.187]
ORIGIN_TIMES = [b(5 + k) for k in range(6)]
shot("origins", "origins", 5.432, b(11), ev(zip(ORIGIN_TIMES, ORIGIN_EVENTS), 5.432, b(11), 0.20, 3.6), "linear",
     [[5.432, 1.08, 540, 955], [b(11), 1.15, 540, 955]])

for k, (clip, *_rest) in enumerate(MONTAGE):
    a, z = b(11 + 2 * k), b(13 + 2 * k)
    shot(f"mont_{clip}", clip, a, z, [[0, 0.30], [z - a, 0.30 + (z - a)]], "linear",
         [[a, 1.20, 540, 880], [z, 1.28, 540, 880]], punch=0.06)

shot("tempest", "tempest", b(23), b(26), [[0, 0.55], [b(26) - b(23), 0.55 + 1.25 * (b(26) - b(23))]], "linear",
     [[b(23), 1.28, 540, 770], [b(26), 1.20, 540, 790]], punch=0.06, shake=[(b(23) + 0.02, 10, 0.35)])
shot("maelstrom", "maelstrom", b(26), b(29), [[0, 0.50], [b(29) - b(26), 0.50 + 1.5 * (b(29) - b(26))]], "linear",
     [[b(26), 1.28, 540, 860], [b(29), 1.40, 540, 860]], punch=0.06)
IMPACT_BIG = b(30)
shot("meteor_big", "meteor", b(29), b(33),
     [[0, 5.45 - (IMPACT_BIG - b(29))], [IMPACT_BIG - b(29), 5.45], [b(33) - b(29), 5.45 + 0.8 * (b(33) - IMPACT_BIG)]],
     "smooth", [[b(29), 1.55, 540, 640], [b(33), 1.75, 540, 640]], punch=0.06, shake=[(IMPACT_BIG, 34, 0.8)])

KEYSTONE_TIP = 19.12
shot("keystone", "keystone", b(33), b(38), ev([(KEYSTONE_TIP, 0.72)], b(33), b(38), 0.50, 2.6), "linear",
     [[b(33), 1.40, 560, 1130], [b(38), 1.56, 560, 1120]])

PLAN_STAGE, PLAN_STATS, PLAN_APPLY = 21.70, 22.25, 22.95
shot("plan", "plan", b(38), b(43),
     ev([(PLAN_STAGE, 2.333), (PLAN_STATS, 3.147), (PLAN_APPLY, 5.04)], b(38), b(43), 0.69, 5.5), "linear",
     [[b(38), 1.0, 540, 960], [b(43), 1.05, 540, 960]])

SHARE_OPEN, SHARE_JUMP, SHARE_COPY = 23.68, 24.315, 24.38
share_map = ev([(SHARE_OPEN, 0.693)], b(43), SHARE_JUMP, 0.46, 1.40)
share_map += [[round(SHARE_JUMP - b(43) + 0.001, 4), 1.96], [round(SHARE_COPY - b(43) - 0.001, 4), 2.07],
              [round(SHARE_COPY - b(43), 4), 2.085], [round(b(46) - b(43), 4), 2.5]]
shot("share", "share", b(43), b(46), share_map, "linear",
     [[b(43), 1.22, 540, 930], [b(46), 1.30, 540, 930]])

RPG_BANNER = b(46) + (0.75 - 0.45)
shot("rpg", "rpg", b(46), b(54), [[0, 0.45], [b(54) - b(46), 0.45 + (b(54) - b(46))]], "linear",
     [[b(46), 1.0, 540, 960], [b(54), 1.12, 540, 880]], punch=0.05)

COMBO = [[0, 2.0], [0.6, 2.62], [0.95, 4.45], [1.45, 4.98], [b(61) - b(54), 6.8]]
shot("combo", "combo", b(54), b(61), COMBO, "smooth",
     [[b(54), 1.12, 540, 820], [b(61), 1.20, 540, 800]], punch=0.05)

TITLE0, TITLE1 = b(61), b(66)
shot("title", "tree_open", TITLE0, TITLE1, [[0, 2.5], [TITLE1 - TITLE0, 4.3]], "linear",
     [[TITLE0, 1.0, 540, 960], [TITLE1, 1.1, 540, 1000]], shake=[(TITLE0 + 0.04, 14, 0.5)])

# Ends on the frame the hook starts from, with the hook's starting camera: the loop has no visible seam.
shot("cta_loop", "meteor", TITLE1, DURATION, [[0, 5.05 - (DURATION - TITLE1)], [DURATION - TITLE1, 5.05]], "linear",
     [[TITLE1, 1.0, 540, 960], [DURATION, 1.15, 540, 840]], camEase="out")

# ------------------------------------------------------------------ cues for overlay + audio
def inv(shot_name, src_t):
    """Output time at which a shot's map reaches a source time (maps are monotone)."""
    s = next(x for x in shots if x["name"] == shot_name)
    m = s["map"]
    for (o0, s0), (o1, s1) in zip(m, m[1:]):
        if s0 <= src_t <= s1 and s1 > s0:
            return round(s["start"] + o0 + (o1 - o0) * (src_t - s0) / (s1 - s0), 4)
    raise ValueError(src_t)


labels = []
for k, (clip, name, school, color, sfx) in enumerate(MONTAGE):
    labels.append({"t0": b(11 + 2 * k), "t1": b(13 + 2 * k), "name": name, "school": school, "color": color, "sfx": sfx})
labels += [
    {"t0": b(23), "t1": b(26), "name": "Tempest", "school": "Stormcalling", "color": "#6fe7ff", "sfx": "storm"},
    {"t0": b(26), "t1": b(29), "name": "Maelstrom", "school": "Tidecalling", "color": "#4f8dff", "sfx": "swirl"},
    {"t0": b(29), "t1": b(33), "name": "Meteor", "school": "Pyromancy", "color": "#ff6a2a", "sfx": "none"},
]

cues = {
    "duration": DURATION, "bpm": BPM, "drop": DROP, "beat": BEAT,
    "flashes": [[IMPACT_HOOK, 0.55], [DROP, 0.35], [IMPACT_BIG, 0.55], [TITLE0, 0.7]],
    "brand": [[3.4, b(46) - 0.05], [b(54) + 0.05, TITLE0 - 0.07]],  # off for the RPG level-up toast
    "s522": [DROP, 5.38, 0.95],
    "s30": [b(11), b(13) - 0.04, 0.45],
    "labels": labels,
    "capTop": [{"t0": 5.40, "t1": b(11), "top": 300}, {"t0": b(38), "t1": b(43), "top": 560}],
    "capBig": [0.0, 2.2],
    "loaders": {"after": 28.5, "bg": [b(54), TITLE0], "end": TITLE0},
    "title": [TITLE0, TITLE1],
    "stickers": {"copied": [SHARE_COPY, b(46) - 0.03], "rpgpill": ["levels", 24.9, b(54) - 0.03]},
    "plane": [TITLE1 + 0.05, DURATION - 0.3],
    "events": {
        "impact_hook": IMPACT_HOOK, "impact_big": IMPACT_BIG, "tree_open": round(TREE_OPEN, 4), "zoom": [ZOOM0, ZOOM1],
        "origins": ORIGIN_TIMES, "montage": b(11), "keystone_tip": KEYSTONE_TIP,
        "plan": [PLAN_STAGE, PLAN_STATS, PLAN_APPLY], "share_open": SHARE_OPEN, "share_copy": SHARE_COPY,
        "rpg_banner": round(RPG_BANNER, 4), "title": TITLE0, "cta": TITLE1,
        "tempest_strikes": [inv("tempest", 0.55), inv("tempest", 1.21)],
        "combo_chain": inv("combo", 2.01), "combo_meteor": None, "combo_inferno": None,
        "whooshes": [[5.432 - 0.18, True], [b(33) - 0.18, True], [TITLE1 - 0.1, False]],
    },
}

# combo uses a smooth map; find its event times numerically
from scipy.interpolate import PchipInterpolator
import numpy as np
cs = next(x for x in shots if x["name"] == "combo")
xs, ys = zip(*cs["map"])
f = PchipInterpolator(xs, ys)
grid = np.linspace(0, xs[-1], 4000)
vals = f(grid)
for key, src_t in (("combo_meteor", 4.80), ("combo_inferno", 5.55)):
    cues["events"][key] = round(cs["start"] + float(grid[np.searchsorted(vals, src_t)]), 4)
shake_t = cues["events"]["combo_meteor"]
cs["shake"].append([shake_t, 16, 0.5])

timeline = []
for i, (line, st) in enumerate(zip(lines, VO_START)):
    timeline.append({"i": i, "start": st, "cap": line["cap"]})

json.dump({"duration": DURATION, "lines": timeline}, open(f"{E}/timeline_plan.json", "w"), indent=1)
json.dump({"fps": 30, "size": [1080, 1920], "shots": shots}, open(f"{E}/edl.json", "w"), indent=1)
json.dump(cues, open(f"{E}/cues.json", "w"), indent=1)
for s in shots:
    print(f'{s["start"]:7.3f}-{s["end"]:7.3f} {s["end"] - s["start"]:5.2f}s  {s["name"]:20s} {s["src"]}')
print("events", json.dumps(cues["events"]))
