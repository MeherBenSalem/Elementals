# Stats

Stat ids for the `stats` field of a node. Percent stats are whole percents (`12` means +12%).
Vanilla stats are applied as attribute modifiers, so other mods see them. The cap is the most a
build can reach, however many nodes grant the stat.

| Id | Shown as | Kind | Cap | Applied as |
| --- | --- | --- | --- | --- |
| `max_health` | +X Max Health | flat | none | vanilla attribute |
| `armor` | +X Armor | flat | none | vanilla attribute |
| `armor_toughness` | +X Armor Toughness | flat | none | vanilla attribute |
| `attack_damage` | +X Melee Damage | percent | none | vanilla attribute |
| `attack_speed` | +X Attack Speed | percent | none | vanilla attribute |
| `movement_speed` | +X Movement Speed | percent | none | vanilla attribute |
| `knockback_resistance` | +X Knockback Resistance | percent | 100% | vanilla attribute |
| `luck` | +X Luck | flat | none | vanilla attribute |
| `ability_power` | +X Ability Damage | percent | none | Remnants |
| `fire_damage` | +X Fire Damage | percent | none | Remnants |
| `frost_damage` | +X Frost Damage | percent | none | Remnants |
| `storm_damage` | +X Storm Damage | percent | none | Remnants |
| `earth_damage` | +X Earth Damage | percent | none | Remnants |
| `void_damage` | +X Void Damage | percent | none | Remnants |
| `life_power` | +X Life Power (healing and life damage) | percent | none | Remnants |
| `crit_chance` | +X Ability Critical Chance | percent | 75% | Remnants |
| `crit_damage` | +X Ability Critical Damage | percent | none | Remnants |
| `cooldown_reduction` | +X Cooldown Reduction | percent | 50% | Remnants |
| `area` | +X Area of Effect | percent | 150% | Remnants |
| `duration` | +X Effect Duration | percent | 200% | Remnants |
| `lifesteal` | +X of Ability Damage Leeched as Life | percent | 50% | Remnants |
| `max_essence` | +X Max Essence | flat | none | Remnants |
| `essence_regen` | +X Essence Regeneration | percent | none | Remnants |
| `essence_on_kill` | +X Essence on Kill | flat | none | Remnants |
| `health_regen` | +X Health per Second | decimal | none | Remnants |
| `damage_reduction` | +X Damage Taken Reduction | percent | 60% | Remnants |
| `dodge` | +X Chance to Evade Attacks | percent | 40% | Remnants |
| `thorns` | +X of Melee Damage Taken Reflected | percent | 200% | Remnants |
| `fall_reduction` | +X Fall Damage Reduction | percent | 100% | Remnants |
| `burn_on_hit` | +X Chance to Ignite on Melee Hit | percent | 100% | Remnants |
| `chill_on_hit` | +X Chance to Chill on Melee Hit | percent | 100% | Remnants |
| `power_xp` | +X Power Experience Gained | percent | none | Remnants |
