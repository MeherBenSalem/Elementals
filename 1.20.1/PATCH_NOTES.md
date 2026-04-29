# Elementals Migration Notes

Elementals is now a normal Minecraft 1.20.1 MultiLoader Gradle project.

The cleanup keeps the main gameplay content: elemental seeds, fire runes, spell books, signature abilities, player elemental progression, the HUD indicator, Fire Golem registration, recipes, textures, models, lang entries, and data resources.

The project is now organized for normal development:

- `common` contains shared gameplay logic and resources.
- `fabric` contains Fabric startup, registration, networking, keybind, and HUD hooks.
- `forge` contains Forge startup, registration, networking, capability data, keybind, and HUD hooks.

Generated workspace files, stale metadata, and placeholder template IDs were removed. Configuration values are created by Elementals itself under the normal config folder.

Manual in-game testing is still recommended for spell balance, Fire Golem spawning, HUD cooldown display, and player data persistence after changing worlds or relogging.
