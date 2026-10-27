# Instagram Reel: "I added a skill tree to Minecraft"

| File | Use |
| --- | --- |
| `remnants-skills-and-powers-reel.mp4` | The Reel: 1080×1920, 30 fps, 37.5 s, H.264 + AAC, -14 LUFS. Not tracked in git. Rebuild it with the steps below |
| `cover.jpg` | Cover image. The text sits inside the centre 3:4 area, so it survives the profile-grid crop |
| `caption.txt` | Post caption with hashtags |

## Structure

The edit follows short-form retention practice: the payoff first, one idea per shot, burned-in captions for sound-off viewers, and an ending that loops seamlessly into the start. Every cut, voice line, label and sound sits on a 128 BPM grid, and the Reel is exactly 80 beats long, so the beat carries on across the loop.

| Time | Shot | Voiceover |
| --- | --- | --- |
| 0.0–2.1 | Meteor impact, flash and shake, slowing to 0.75× after the hit | "Minecraft doesn't have a skill tree…" |
| 2.1–5.4 | Tree screen opens, then one continuous zoom out to the whole constellation on the drop, 0→522 counter | "So I made one. 522 nodes." |
| 5.4–8.5 | Hovering all six origins, one per beat | "Six schools of magic. Start anywhere. Go anywhere." |
| 8.5–14.1 | One spell per school, 2 beats (0.94 s) each, with its name and school: Inferno Wave, Glacial Prison, Radiant Nova, Eclipse, Thunderclap, Tremor | "30 abilities." |
| 14.1–18.8 | Tempest, Maelstrom (3 beats each), Meteor (4 beats, impact on the beat, slow motion) | "Lightning storms. Whirlpools. Meteors!" |
| 18.8–21.1 | Glass Cannon keystone | "12 keystones that break the rules." |
| 21.1–24.9 | Path preview, staging, stat summary and Apply, each on its word, then the build code | "Plan your whole build… and share it with one code." |
| 24.9–28.6 | Real RPG Player Skills level-up and the skill point it grants | "Got RPG Player Skills? Your levels turn into skill points." |
| 28.6–31.9 | Live hotbar combo: Chain Lightning, Meteor, Inferno Wave, with loader pills | "Fabric, Forge and NeoForge." |
| 31.9–34.3 | Title card | "It's called Remnants: Skills and Powers." |
| 34.3–37.5 | Back on the meteor stage, ending on the Reel's first frame | "Send this to the friend who always plays mage." |

## How it was made

- **Footage:** unedited captures of the 1.21.1 Fabric development client at 1080×1920 (a portrait virtual display) on a dedicated server running the release jar, RPG Player Skills 5.0.0 and JAuml. Both game processes ran on a clock slowed 10× with libfaketime (`source/capture/`), so the software renderer still produced about 50 frames per second of game time, and the screen was grabbed often enough to keep every one of them. Targets are `NoAI` Vindicators with Resistance. The only world edits: grass cleared and a few blocks levelled in front of the cherry trees.
- **Smooth playback:** `source/prep.py` indexes which captured frames are new renders and their game time. `source/compose.py` asks for a game time per output frame and cross-fades the two renders around it, so no frame repeats or skips at any speed, and fast speed ramps get a 180° shutter blur. The tree zoom pulses in game (each key press eases separately), so `source/zoomtrack.py` measured its zoom level frame by frame and the edit follows one smooth curve instead. `source/verify.py` checks the result for repeated frames and uneven motion.
- **Voice:** [Kokoro-82M](https://huggingface.co/hexgrad/Kokoro-82M) (Apache-2.0), voice `af_heart`, one line at a time so each line can be placed on its beat (`source/tts.py`, `source/build_vo.py`). Captions are timed per word from a Whisper transcript of the voiceover (`source/align.py`, `source/captions.py`).
- **Music and sound effects:** original, synthesized in `source/synth.py` and `source/audio.py`, with the drop on "522 nodes". No samples or licensed tracks, so there are no copyright claims. Each montage cut gets the sound of the spell on screen (fire, ice, light, void, thunder, earth); only three soft whooshes mark section changes. The music is sidechained under the voice in the final mix (`source/final.sh`).
- **Edit:** `source/plan.py` is the single source of truth (beat grid, shots, time maps, cues). `source/overlay.html` is rendered frame by frame with Playwright (`source/render_overlay.cjs`) for captions, counters, spell labels, stickers, loader pills and the title card.
- **Fonts:** Montserrat and Anton (SIL OFL 1.1, in `source/fonts/`) and Cinzel from `marketing/source/fonts/`.

## Posting checklist

- Upload the MP4 as a Reel and set `cover.jpg` as the cover. Keep the original audio; adding a trending track on top would bury the voiceover.
- Paste `caption.txt`. Put the Modrinth link in your bio before posting, since the caption points there.
- Add alt text, for example: "Minecraft gameplay showing a giant skill tree screen and spells like meteors and lightning storms."
- Pin a first comment asking which school people would pick. Replies in the first hour count as engagement.
- Post when your audience is most active (Insights → Most active times), and reply to early comments quickly.
- Cross-post the same file to TikTok and YouTube Shorts.
