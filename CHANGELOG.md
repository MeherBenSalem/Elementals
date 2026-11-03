# Changelog

## 2.0.1

- Fixed the skill tree ignoring left clicks on Minecraft 26.3 (Fabric and NeoForge): 26.3 reports
  mouse buttons with SDL numbers, which the tree screen now translates ([#1](https://github.com/MeherBenSalem/Elementals/issues/1))

See [patchnotes/2.0.1.md](patchnotes/2.0.1.md).

## 2.0.0

- Renamed to **Remnants | Skills and Powers** (mod id unchanged: `elementals`)
- 522-node passive skill tree with 6 schools, 30 abilities, 12 keystones and 32 stats
- New tree screen: path preview, staged Apply/Undo, plans, search, minimap, stat diff, build codes
- New HUD with four cast slots (R / Z / B / H) and Essence; tree opens with I
- Power levels, Remnant Tome, per-node refunds, full reset and Tome of Unbinding
- Remnants | RPG Player Skills integration (auto/replace/add/off)
- Server-driven tree and ability config with `/powers reload`; `/powers` admin commands
- Added 26.3 (Fabric + NeoForge); all eight targets share one core and one regression suite
- Fire Golem reworked; 1.x players migrate automatically

See [patchnotes/2.0.0.md](patchnotes/2.0.0.md).

## 1.4.0

- MultiLoader release: Fabric + Forge on 1.20.1; Fabric + NeoForge on 1.21.1 and 26.2
- Relicensed under Apache License 2.0
- Added 26.2 support (Fabric + NeoForge)
- Removed template and migration clutter; standardized Apache OSS repo layout
