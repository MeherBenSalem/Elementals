import json, sys
import numpy as np, soundfile as sf
voice = sys.argv[1]
gaps = [0.22, 0.3, 0.35, 0.08, 0.3, 0.12, 0.1, 0.1, 0.45, 0.3, 0.05, 0.35, 0.3, 0.25, 0.3, 0.0]
lead, tail = 0.15, 0.9
lines = json.load(open("/tmp/claude-0/reel/lines.json"))
parts, t, timeline, sr = [], lead, [], 24000
parts.append(np.zeros(int(lead * sr), dtype=np.float32))
for i, line in enumerate(lines):
    a, sr = sf.read(f"/tmp/claude-0/reel/vo/{voice}_{i:02d}.wav", dtype="float32")
    timeline.append({"i": i, "start": round(t, 3), "end": round(t + len(a) / sr, 3), "cap": line["cap"]})
    parts.append(a); t += len(a) / sr
    parts.append(np.zeros(int(gaps[i] * sr), dtype=np.float32)); t += gaps[i]
parts.append(np.zeros(int(tail * sr), dtype=np.float32)); t += tail
audio = np.concatenate(parts)
audio = audio / max(1e-6, np.abs(audio).max()) * 0.89
sf.write("/tmp/claude-0/reel/vo.wav", audio, sr)
json.dump({"duration": round(t, 3), "lines": timeline}, open("/tmp/claude-0/reel/timeline.json", "w"), indent=1)
print("duration", round(t, 2))
