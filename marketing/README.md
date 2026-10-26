# Marketing assets: 2.0 "The Remnant Constellation"

Listing copy and artwork for CurseForge and Modrinth. Nothing here ships in the mod jars.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full project description (Markdown; also works on Modrinth) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short project summary (199 characters) |
| [`../patchnotes/2.0.0.md`](../patchnotes/2.0.0.md) | File changelog for 2.0.0 (the upload script uses it by default) |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/Elementals/main/marketing/images/...`, so they work once this folder is on `main`. To host on CurseForge instead, upload the images to the project gallery and replace each URL.

The project title on both sites should become **Remnants | Skills and Powers**. The description links Remnants | RPG Player Skills by its Modrinth project, whose listing is currently titled "Remnants | RPG Stats, Skills & Progression".

## Adding a Minecraft version

The supported versions are listed in exactly two places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600 | Description header: the constellation, "2.0 · The Remnant Constellation", loader pills and versions |
| `images/icon.png` | 512×512 | Project avatar: the mod logo in a gold ring with the six school colours |
| `images/card-constellation.png` | 1600×900 | The whole tree; 522 nodes, 54 notables, 30 abilities, 12 keystones |
| `images/card-schools.png` | 1600×900 | Origin choice on the inner ring, with each school's first ability and ultimate |
| `images/card-abilities.png` | 1600×900 | Meteor, Tempest, Maelstrom and Verdant Rebirth |
| `images/card-combat.png` | 1600×900 | Tempest into Chain Lightning, with a close-up of the ability bar |
| `images/card-planning.png` | 1600×900 | Path preview to Windborne, then the staged path with the stat diff |
| `images/card-keystones.png` | 1600×900 | The Glass Cannon tooltip |
| `images/card-share.png` | 1600×900 | The Share panel over a build |
| `images/card-rpg.png` | 1600×900 | An RPG Player Skills level-up granting a skill point, and the RPG Level panel |
| `images/card-progression.png` | 1600×900 | A Pyromancy / Stormcalling build and a Fire Golem |
| `images/card-config.png` | 1600×900 | Real excerpts of the generated `general.json` and `abilities.json` |
| `images/header-*.png` | 1400×120, transparent | 14 section headings |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/gifs/casting.gif` | 640×360, 8 s | Tempest, Chain Lightning, Meteor and Inferno Wave cast from the ability bar |
| `images/gifs/tree-planning.gif` | 640×360, 10 s | Hover, path preview, stage, stat diff and Apply in the tree screen |
| `images/screenshots/*.jpg` | 1920×1080 | 26 captures for the gallery and the cards |

### How the screenshots were captured

Every screenshot and both GIFs are unedited captures of the **1.21.1 Fabric development client** (`./gradlew :fabric:runClient`, Remnants | Skills and Powers 2.0.0, default configuration) connected to a dedicated server running the release jar, Fabric API, **Remnants | RPG Player Skills 5.0.0** and JAuml. Software rendering at 1920×1080, GUI scale 3.

- **Where:** a cherry grove at ground level in seed `remnants`, with the tall grass around the spot cleared, and the Nether Wastes for the Fire Golem. Clear weather, time fixed with `/time set`.
- **Targets:** Vindicators summoned with `NoAI` and Resistance so they stand still and survive. The ability shots were cast with `/powers cast` (the same code path as the keys); the HUD shots and `casting.gif` were cast with the R, Z, B and H keys.
- **Camera:** ability shots in third person (F5) or first person, with the HUD hidden (F1) except `combat-hud*.jpg`, `rpg-level-up.jpg` and `casting.gif`. Some ultimates were captured with `/tick rate` slowed so a single frame shows the effect.
- **Tree screens:** a build granted with `/powers grant` and RPG level 24 from `/ras add level`, then edited by hand. `rpg-level-up.jpg` is a real `/ras add level` at RPG level 27.

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots above and the bundled fonts:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs              # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- `source/backdrop.jpg` is the blurred card background, a 1280×720 copy of `screenshots/tree-overview.jpg`.
- `source/logo.png` is the mod logo (`common/src/main/resources/logo.png`).
- `source/fonts/` holds Cinzel and EB Garamond with their SIL Open Font License 1.1 texts.

Card crops are pixel regions of the 1920×1080 captures (`region()` in `render.cjs`), so new captures may need their crops adjusted.

## Wording

Call the mod **Remnants | Skills and Powers** and the companion **Remnants | RPG Player Skills**. Describe the tree as "Path of Exile-style" only in the short summary, and never use another game's name, logo or art in images or titles. Every number in the copy and on the cards is a 2.0.0 default from the generated config, a recipe or loot table, or the code (`docs/abilities.md`, `docs/keystones.md` and `docs/stats.md` are generated from it). Update the copy when those defaults change.
