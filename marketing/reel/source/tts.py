import json, sys
import numpy as np, soundfile as sf
from kokoro_onnx import Kokoro
M = "/tmp/claude-0/-home-user-Elementals/3b676e2b-c649-5d55-a750-c5b258d7a571/scratchpad/ttsmodels"
k = Kokoro(f"{M}/kokoro-v1.0.onnx", f"{M}/voices-v1.0.bin")
voice = sys.argv[1] if len(sys.argv) > 1 else "af_heart"
speed = float(sys.argv[2]) if len(sys.argv) > 2 else 1.1
lines = json.load(open("/tmp/claude-0/reel2/edit/lines.json"))
out = []
for i, line in enumerate(lines):
    audio, sr = k.create(line["say"], voice=voice, speed=speed, lang="en-us")
    # trim leading/trailing silence
    a = np.abs(audio); thr = 0.01
    nz = np.where(a > thr)[0]
    audio = audio[max(0, nz[0] - int(0.02 * sr)): nz[-1] + int(0.05 * sr)]
    sf.write(f"/tmp/claude-0/reel2/edit/vo/{voice}_{i:02d}.wav", audio, sr)
    out.append({"i": i, "dur": len(audio) / sr})
    print(i, round(len(audio) / sr, 2), line["say"])
json.dump(out, open(f"/tmp/claude-0/reel2/edit/vo/{voice}_durs.json", "w"))
