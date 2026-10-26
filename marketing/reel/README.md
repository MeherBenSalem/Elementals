# Instagram Reel: "I added a skill tree to Minecraft"

| File | Use |
| --- | --- |
| `remnants-skills-and-powers-reel.mp4` | The Reel: 1080×1920, 30 fps, 30.4 s, H.264 + AAC, -14 LUFS. Not tracked in git (36 MB). Rebuild it with the steps below |
| `cover.jpg` | Cover image. The text sits inside the centre 3:4 area, so it survives the profile-grid crop |
| `caption.txt` | Post caption with hashtags |

## Structure

The edit follows short-form retention practice: the payoff first, one idea per shot, burned-in captions for sound-off viewers, and an ending that loops seamlessly into the start.

| Time | Shot | Voiceover |
| --- | --- | --- |
| 0.0–2.1 | Meteor impact, flash and shake | "Minecraft doesn't have a skill tree…" |
| 2.1–5.4 | Tree screen opens and zooms out to the whole constellation, 0→522 counter | "So I made one. 522 nodes." |
| 5.4–8.4 | Hovering all six origins | "Six schools of magic. Start anywhere. Go anywhere." |
| 8.4–12.6 | Six-shot ability montage, Tempest, Maelstrom, Meteor | "30 abilities. Lightning storms. Whirlpools. Meteors!" |
| 12.6–14.6 | Glass Cannon keystone | "12 keystones that break the rules." |
| 14.6–18.7 | Path preview, staging, stat diff, Apply, then the build code | "Plan your whole build… and share it with one code." |
| 18.7–22.5 | Real RPG Player Skills level-up and the skill point it grants | "Got RPG Player Skills? Your levels turn into skill points." |
| 22.5–24.6 | Live four-spell combo from the hotbar, loader pills | "Fabric, Forge and NeoForge." |
| 24.6–27.0 | Title card | "It's called Remnants: Skills and Powers." |
| 27.0–30.4 | Back on the meteor stage, which loops into the first frame | "Send this to the friend who always plays mage." |

## How it was made

- **Footage:** unedited captures of the 1.21.1 Fabric development client at 1080×1920 (a portrait virtual display) on a dedicated server running the release jar, RPG Player Skills 5.0.0 and JAuml. Ability shots were recorded at `/tick rate 5` and sped back up in the edit, so particles stay smooth on software rendering. Targets are `NoAI` Vindicators with Resistance. The only world edits: grass cleared and a few blocks levelled in front of the cherry trees.
- **Voice:** [Kokoro-82M](https://huggingface.co/hexgrad/Kokoro-82M) (Apache-2.0), voice `af_heart`, one line at a time so each cut lands on its phrase (`source/tts.py`, `source/build_vo.py`). Captions are timed per word from a Whisper transcript of the voiceover (`source/align.py`, `source/captions.py`).
- **Music and sound effects:** original, synthesized in `source/audio.py` on a 128 BPM grid with the drop on "522 nodes". No samples or licensed tracks, so there are no copyright claims. The music is sidechained under the voice in the final mix.
- **Edit:** `source/compose.py` (cuts, speed ramps, eased camera moves, punch-ins, impact shakes, grade) plus `source/overlay.html` rendered frame by frame with Playwright (`source/render_overlay.cjs`) for captions, counters, stickers, loader pills and the title card.
- **Fonts:** Montserrat and Anton (SIL OFL 1.1, in `source/fonts/`) and Cinzel from `marketing/source/fonts/`.

## Posting checklist

- Upload the MP4 as a Reel and set `cover.jpg` as the cover. Keep the original audio; adding a trending track on top would bury the voiceover.
- Paste `caption.txt`. Put the Modrinth link in your bio before posting, since the caption points there.
- Add alt text, for example: "Minecraft gameplay showing a giant skill tree screen and spells like meteors and lightning storms."
- Pin a first comment asking which school people would pick. Replies in the first hour count as engagement.
- Post when your audience is most active (Insights → Most active times), and reply to early comments quickly.
- Cross-post the same file to TikTok and YouTube Shorts.
