# Using Remnants | RPG Player Skills

Remnants | Skills and Powers works on its own, and links up automatically when
**Remnants | RPG Player Skills** (`rpg_attribute_system`) is installed on the server. Neither mod
requires the other.

| | Skills and Powers alone | With RPG Player Skills |
| --- | --- | --- |
| Skill points | From Power levels (mob kills and casting) | From RPG levels: 1 point per RPG level by default |
| Level shown in the tree | Power Level and XP bar | RPG Level |
| Ability damage | Tree stats | Tree stats + 0.5% per RPG level |
| Node requirements | `power_level` | `ras_level` too |

## How it works

- Every second the server reads each player's RPG level through RPG Player Skills' public API
  (`tn.nightbeam.ras.api.RasApi#getLevel`). New levels are turned into skill points once, and the
  player sees "+N skill points from RPG level X".
- Levels earned before installing Skills and Powers count too. A player who is already RPG level 30
  starts with their points.
- RPG Player Skills attributes and Skills and Powers nodes stack: both use vanilla attribute
  modifiers. Ability damage also grows with your RPG level.
- Default keys don't overlap: RPG Player Skills uses **K**, Skills and Powers uses **O** and
  **R / G / Z / H**. The Skills and Powers ability bar sits right of the hotbar, clear of the RPG HUD.

## Modes

Set `rpg_player_skills.mode` in `general.json`:

| Mode | Behavior |
| --- | --- |
| `auto` (default) | `replace` when RPG Player Skills is installed, otherwise `off` |
| `replace` | Skill points come only from RPG levels. Power XP is turned off |
| `add` | Both: Power levels and RPG levels give points |
| `off` | Ignore RPG Player Skills |

## Requiring RPG levels on nodes

In a custom tree, add `"requires": {"ras_level": 25}` to a node. Without RPG Player Skills installed,
the requirement is ignored.

Nodes can also run RPG Player Skills commands when allocated, for example granting an attribute
point:

```json
"on_allocate": ["ras add attributes 2 1 {player}"]
```
