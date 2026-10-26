# Configuration

Files live in `config/remnants/skills_and_powers/` on the server (or in the singleplayer game folder).
Missing options are added with their defaults on start. Run `/powers reload` after editing: every
online player is resynced, including the tree and ability numbers shown in tooltips.

## general.json

| Section | Key | Default | Meaning |
| --- | --- | --- | --- |
| progression | `starting_points` | 2 | Skill points every player starts with |
| | `points_per_level` | 1 | Points per Power level |
| | `max_level` | 100 | Power level cap |
| | `xp_base`, `xp_per_level` | 40, 12 | XP for level L→L+1 is `xp_base + xp_per_level × L` |
| | `xp_per_mob_max_health` | 1.0 | Power XP per point of a killed mob's max health |
| | `xp_boss_multiplier` | 3.0 | Extra multiplier for mobs with 100+ max health |
| | `xp_per_ability_cast` | 1 | Power XP for each successful cast |
| rpg_player_skills | `mode` | `auto` | `auto`, `replace`, `add` or `off`. See [the integration page](rpg-player-skills.md) |
| | `skill_points_per_rpg_level` | 1 | Skill points per RPG level |
| | `ability_damage_percent_per_rpg_level` | 0.5 | Ability damage bonus per RPG level |
| respec | `refund_cost_xp_levels_per_node` | 1 | Vanilla XP levels per refunded node (0 = free) |
| | `full_reset_cost_xp_levels` | 10 | Vanilla XP levels for **Reset Tree** |
| essence | `base_max_essence` | 100 | Essence before tree bonuses |
| | `base_regen_per_second` | 4 | Essence regeneration before bonuses |
| combat | `abilities_hurt_players` | false | PvP for abilities. Teams are always respected |
| | `abilities_hurt_tamed_animals` | false | Whether abilities can hit your own pets |
| | `max_cooldown_reduction_percent` | 50 | Cap on Cooldown Reduction |
| | `global_cooldown_ticks` | 6 | Minimum time between any two casts |
| tree | `use_custom_tree` | false | Load `skill_tree.json` instead of the built-in tree |
| items | `remnant_tome_skill_points` | 1 | Points per Remnant Tome |
| | `remnant_tome_max_uses` | 20 | Tomes each player can read |
| | `explosive_orb_book_required_level` | 0 | Level needed to read the Explosive Orb spell book |
| | `ember_shield_book_required_level` | 10 | Same for Ember Shield |
| | `inferno_wave_book_required_level` | 20 | Same for Inferno Wave |
| mobs | `blaze_fire_rune_drop_chance` | 0.2 | Fire Rune drop chance from Blazes |
| | `drowned_water_rune_drop_chance` | 0.15 | Water Rune drop chance from Drowned and Guardians killed by a player |

## abilities.json

One object per ability. `enabled: false` removes the ability from casting (the node still exists and
its tooltip says it is disabled). Every number in the [ability list](abilities.md) can be changed:
`cost` (Essence), `cooldown` (seconds), `damage`, `radius`, `range`, `duration` (seconds) and the
ability-specific values.

```json
"meteor": {
  "enabled": true,
  "cost": 60.0,
  "cooldown": 30.0,
  "damage": 22.0,
  "radius": 5.0,
  "range": 32.0,
  "delay": 1.2,
  "burn": 5.0
}
```

## skill_tree.json

See [the skill tree page](skill-tree.md).

## Upgrading from Elementals 1.x

- The mod id is still `elementals`, so items, the Fire Golem and worlds carry over.
- A player who chose Fire or Water in 1.x gets the Pyromancy or Tidecalling origin for free on first
  join, plus 3 bonus points per old elemental level.
- Spell cooldowns and Inferno Wave damage from `config/elementals/fire_spells.properties` are copied
  into `abilities.json` the first time it is created.
- Player data is stored in `<world>/remnants_powers/<uuid>.json` on every loader.
