# Market Analysis: Remnants | Skills and Powers

Competitive research for the rebrand of **The Elementals - RPG** (current listing) into
**Remnants | Skills and Powers**: a large passive skill web plus elemental active powers
(fire, frost/water, storm, earth, void/shadow, life/nature) on keybinds, a mana-like resource,
and data-driven, customizable trees. Target platforms: 1.20.1 (Fabric + Forge), 1.21.1
(Fabric + NeoForge), 26.2 and 26.3 (Fabric + NeoForge).

- **Research date:** 2026-10-26.
- **Download counts:** Modrinth numbers come from the public API (`https://api.modrinth.com/v2/project/<slug>`).
  CurseForge numbers come from the CFWidget mirror (`https://api.cfwidget.com/minecraft/mc-mods/<slug>`).
  Both were fetched on 2026-10-26. CFWidget can lag CurseForge by a little. Numbers are rounded in the prose and exact in the tables.
- **Versions and loaders** come from each project's file and version lists, not from the description text, unless noted otherwise.
- **Complaints** come from GitHub issue trackers, mod descriptions and FAQs, and from add-on mods that exist only to patch a gap. An add-on's download count is used as a demand signal.
  CurseForge comment threads load through JavaScript and could not be read, so CurseForge comments are **not** covered.
- Anything that could not be checked against a primary source is marked **unverified**.

---

## TL;DR

1. **26.x is wide open.** Several big names have no 26.x build: Iron's Spells (70.8M CurseForge downloads), Ars Nouveau, Origins, Simply Skills, Daripher's Passive Skill Trees, Mine and Slash and LevelZ.
   The 26.x options are:
   - Pufferfish's Skills, a framework that needs separate content.
   - The RPG Series stack: Spell Engine, Wizards and Skill Tree, which pull in a chain of 4 to 8 required mods.
   - Project MMO (NeoForge only) and Bending (Fabric only).
   - A small, unofficial Fabric port of Passive Skill Trees (about 3.8k downloads).

   No 26.x mod combines a large passive web, elemental active powers and mana in **one dependency-free jar on both Fabric and NeoForge**.
2. **There is a large audience for a passive web.** Daripher's Passive Skill Trees has about 10.9M CurseForge downloads, yet it is still a Forge-only beta for 1.19.2 and 1.20.1. Its issue tracker asks for Fabric and NeoForge 1.21.1 ports.
3. **The same complaints recur across mods:**
   - Long chains of required dependencies.
   - Frameworks that ship empty.
   - No player-facing respec.
   - Rigid point caps.
   - Limited tree-graph features: one-way edges, multi-rank nodes, mid-tree exclusive choices.
   - Unofficial ports that break on dedicated servers.
   - Too few active-ability keybinds.
   - No way to share or save builds.
4. **Brand notes.** The old name "Elementals" collides with saperate's *Elementals*, a bending mod with about 132k Modrinth downloads, so the rebrand is justified.
   The new name sits next to the studio's own **Remnants | RPG Stats, Skills & Progression**, which also has "Skills" in its title. The two descriptions should state the difference clearly (see section 4).

---

## 1. Comparison table

### 1a. Reach, platforms and dependencies

"26.x" means a published build for 26.1, 26.2 or 26.3. **Bold** marks a 26.2 or 26.3 build.

| Mod (author) | Loaders | MC versions (from file lists) | 26.x? | Downloads CF / MR | Required dependencies |
|---|---|---|---|---|---|
| **Passive Skill Trees** (Daripher) | Forge | 1.19.2, 1.20.1. The latest file is `1.20.1-BETA-0.7.6e` (2026-07-22). | No | 10,920,985 / not on MR | AttributeFix, plus Max Health Fix **or** Improved Damage Framework ([CF](https://www.curseforge.com/minecraft/mc-mods/passive-skill-tree)) |
| Passive Skill Trees **Fabric** (unofficial port, LeaMissyoy) | Fabric | 1.20.1, 1.21.1, 1.21.10, 1.21.11, 26.1.2, **26.2** | Yes (Fabric only) | 3,790 / – | Fabric API, AttributeFix and Cardinal Components, according to a third-party summary (**unverified**) ([CF](https://www.curseforge.com/minecraft/mc-mods/passive-skill-trees-fabric)) |
| **Pufferfish's Skills** (pufmat) | Fabric, Forge, NeoForge | 1.16.5 (CF) and 1.18.2 → **26.3**. The latest is 0.19.2 (2026-10-08). | Yes | 36,112,560 / 4,761,628 | Fabric API only. It needs content to be useful: Default Skill Trees (3.83M / 643k) and Pufferfish's Attributes (25.2M / 3.70M) ([MR](https://modrinth.com/mod/skills)) |
| **Simply Skills** (Sweenus/Timefall) | Fabric | 1.19.2 → 1.20.1. The last update was 2025-03-09. | No | 8,103,250 / 1,927,131 | Pufferfish's Skills, Pufferfish's Attributes, Spell Engine, Spell Power and Cloth Config, plus Spell Engine's own dependencies. The Crusader spec also needs Paladins & Priests ([MR](https://modrinth.com/mod/simply-skills)) |
| **Skill Tree (RPG Series)** (ZsoltMolnarrr/daedelus) | Fabric, Forge, NeoForge | 1.20.1, 1.21.1 → **26.3** | Yes | 1,735,864 / 318,183 | The page lists Pufferfish's Skills, Archers, Paladins & Priests, Rogues & Warriors and Wizards. The 26.3 version metadata lists Pufferfish's Skills, Spell Engine, Ranged Weapon API and Critical Strike ([MR](https://modrinth.com/mod/skill-tree)) |
| **Spell Engine** (library) | Fabric, Forge, NeoForge | 1.19 → **26.3** | Yes | 21,036,776 / 6,478,203 | Cloth Config, Player Animator (Player Animation Library on 26.x) and Spell Power. Fabric also needs Fabric API and Trinkets; NeoForge needs Curios ([MR](https://modrinth.com/mod/spell-engine)) |
| **Wizards (RPG Series)** | Fabric, Forge, NeoForge | 1.19 → **26.3** | Yes | 18,373,344 / 5,711,032 | Spell Engine, Runes, Armor Model API and Structure Pool API, with Better Combat strongly recommended ([MR](https://modrinth.com/mod/wizards)) |
| **Iron's Spells 'n Spellbooks** (iron431) | Forge, NeoForge | Forge 1.18.2–1.20.1, NeoForge 1.21/1.21.1. The latest is 3.16.3 (2026-08-18). | No | 70,811,333 / 6,140,051 | Curios, GeckoLib, Iron's Lib and playerAnimator. These are missing from the Modrinth metadata ([issue #1251](https://github.com/iron431/Irons-Spells-n-Spellbooks/issues/1251)). The FAQ answers port requests with "TLDR: No" ([MR](https://modrinth.com/mod/irons-spells-n-spellbooks)) |
| **Origins** (Apace) | Fabric, Quilt. The Forge 1.20.1 build runs through Sinytra Connector. | 1.16 → 1.21.1. The latest is 1.13.0-pre.4 (2026-10-18). | No | 22,182,447 / 3,141,047. The separate Origins (Forge) port has 1,049,008 MR downloads. | Fabric API. The Forge build needs Connector and Forgified Fabric API ([MR](https://modrinth.com/mod/origins)) |
| **Project MMO** (Caltinor/Harmonised) | Forge, NeoForge | 1.12 → **26.3**; 26.x is NeoForge only | Yes (NeoForge) | 13,296,515 / 144,290 | None ([MR](https://modrinth.com/mod/project-mmo)) |
| **Ars Nouveau** (baileyholl) | Forge, NeoForge | Up to 1.21.1 | No | 116,498,729 / 4,082,228 | None in the Modrinth metadata (**unverified**) ([MR](https://modrinth.com/mod/ars-nouveau)) |
| **Mine and Slash** (RobertSkalko) | Forge | 1.20.1 is the only supported version | No | 4,583,118 / 65,221 | Library of Exile, Curios, Player Animator, The Harvest and Dungeon Realm. The page says it is "Not compatible with other Spell mods" ([MR](https://modrinth.com/mod/mine-and-slash)) |
| **Epic Fight: Skill Tree** (official add-on) | Forge, NeoForge | 1.20.1, 1.21.1 | No | 2,376,330 / 73,951 | Epic Fight ([MR](https://modrinth.com/mod/epic-fight-skill-tree)) |
| **LevelZ** (Globox1997) | Fabric | Up to 1.21.1. The page says "no plans" for Forge. | No | 3,219,295 / 2,001,563 | Fabric API, Cloth Config and LibZ ([MR](https://modrinth.com/mod/levelz)) |
| **Bending** (PrimordialMoros) | Fabric (server-side), Paper/Purpur | 1.18.2 → **26.3** | Yes (Fabric) | CF not checked / 139,024 | Fabric API ([MR](https://modrinth.com/mod/bending)) |
| **Elementals** (saperate), bending | Fabric, NeoForge | 1.20.1–1.21.1 | No | CF not checked / 131,557 | GeckoLib and Common Network, plus Fabric API on Fabric ([MR](https://modrinth.com/mod/elemental)) |
| Archetypes (matje-balovich), a newcomer | Fabric, Forge, NeoForge | 1.20.1 → **26.2** | Yes | – / 2,453 | Player Animation Library and Fabric API ([MR](https://modrinth.com/mod/archetypes)) |
| Talentium | Mod and data pack | 1.20 → **26.2** | Yes | – / 44,722 | None ([MR](https://modrinth.com/mod/talentium)) |
| Super Powers (SVM), data pack | Data pack | 1.20 → **26.2** | Yes | – / 197,478 | None ([MR](https://modrinth.com/mod/svm-powers)) |
| Smaller elemental mods | Various | Avatar: Legend Reforged (NeoForge 1.21.1); Elemental Powers (MCreator, NeoForge 1.21.4–1.21.8); Keepers of the Stones II (Forge/NeoForge 1.19.2–1.21.8); Elemental Wizards (a Spell Engine add-on, 1.20.1–1.21.1) | No | – / 5,938; 2,195; 16,661; 656,875 | Various ([search](https://modrinth.com/mods?q=elemental)) |
| **Vault Hunters 3rd Ed.** (modpack) | Forge | 1.18.2 only | No | 5,937,994 (modpack) / – | It is a modpack ([CF](https://www.curseforge.com/minecraft/modpacks/vault-hunters-1-18-2)) |
| *Ours today:* The Elemental - RPG | Fabric, Forge, NeoForge | 1.20.1, 1.21.1, 26.2 | Yes | 5,490 / 2,788 | – ([CF](https://www.curseforge.com/minecraft/mc-mods/the-elementals-rpg), [MR](https://modrinth.com/mod/the-elementals)) |
| *Sibling:* Remnants \| RPG Stats, Skills & Progression | Fabric, Forge, NeoForge | 1.16.5 → **26.3** | Yes | 444,966 / 94,447 | jauml on 1.20.1/1.21.1, Fabric API on Fabric ([MR](https://modrinth.com/mod/remnants-rpg-stats-skills-progression)) |

### 1b. Features: tree, abilities and UI

Key: ✔ yes · ✖ no · ~ partial or through an add-on · ? not found or **unverified**

| Mod | Default content | Data-driven / editor | Actives vs passives | Resource | Zoom / pan | Search | Path allocation | Respec | Build share / loadouts | Multiplayer / server notes |
|---|---|---|---|---|---|---|---|---|---|---|
| Passive Skill Trees (Daripher) | ✔ Several trees and starting classes ([feed-the-mods](https://www.feed-the-mods.com/passive-skill-tree-mod/)); gems socket into gear | ✔ Data packs plus the `/skilltree editor` in-game editor. The editor writes a data pack into the **game root**, and the user has to copy it into the world ([CF](https://www.curseforge.com/minecraft/mc-mods/passive-skill-tree)) | Passives only | None. Skill points cost XP, and a "buy point" button can exchange XP for points | ✔ Zoomable, scrollable widget group, from the [source](https://github.com/Daripher/Passive-Skill-Tree/blob/1.20.1/src/main/java/daripher/skilltree/client/widget/skill/SkillButtons.java) | ✔ A search box highlights matching nodes ([source](https://github.com/Daripher/Passive-Skill-Tree/blob/1.20.1/src/main/java/daripher/skilltree/client/widget/SkillTreeWidgets.java)) | ~ Nodes are staged, then confirmed or cancelled. Multi-node shortest-path allocation was not found in the source (?) | ~ Amnesia Scroll with a default 20% level penalty; it drops from the Ender Dragon ([ServerConfig](https://github.com/Daripher/Passive-Skill-Tree/blob/1.20.1/src/main/java/daripher/skilltree/config/ServerConfig.java)) | ✖ / ✖. Right-click marks "favorite" nodes | Open issues include "Server config overruled by local config" (#223) and the Fabric port's "No tree found on dedicated server" (#308) ([issues](https://github.com/Daripher/Passive-Skill-Tree/issues)) |
| Pufferfish's Skills | ✖ The core is "just a framework". Default Skill Trees adds 2 trees (combat and mining). | ✔ JSON, a web editor and `/reload`. Rewards can be attributes, commands, scoreboard values, tags, or Origins powers through an add-on ([docs](https://puffish.net/skillsmod/docs/creators/)) | Passives only. Actives come only through commands or add-ons | ✖ | ✔ Zoom and position are remembered since 0.13.0 ([#63](https://github.com/pufmat/skillsmod/issues/63)) | ? | ✖ (?) | ~ Commands only (`/puffish_skills skills reset`). Respecs come from third-party add-ons such as [Respec Skill](https://modrinth.com/mod/respec-skill) and Book of Unlearning | ✖ / ✖ | Configured on the server. A client and server on different loaders can connect. Anti-farming exists per chunk and per entity, but logging out resets the kill limiter ([#61](https://github.com/pufmat/skillsmod/issues/61)) |
| Simply Skills | ✔ A combat tree plus class specialisations | ~ Built on Pufferfish's JSON format | ✔ Unique passive nodes plus a **signature ability** on the `V` key, through Spell Engine | Cooldowns | ✔ (Pufferfish UI) | ? | ✖ | ~ Malevolent Manuscript. Points spent in a specialisation are "gone forever" ([MR](https://modrinth.com/mod/simply-skills)) | ✖ | Capped at 42 points. A no-limit data pack exists with 14k downloads ([MR](https://modrinth.com/mod/simply-skills-no-limit-tree)) |
| Skill Tree (RPG Series) | ✔ 200+ nodes for the RPG Series classes | ~ Pufferfish JSON plus Spell Engine | ✔ Spell modifiers and passive spells; the actives come from the class mods | Runes and cooldowns | ✔ (Pufferfish UI) | ? | ✖ | ✔ Orb of Oblivion, a full reset with refund ([MR](https://modrinth.com/mod/skill-tree)) | ✖ | XP is shared between team members |
| Spell Engine + Wizards | ✔ Arcane, fire and frost tomes | ✔ Spells are JSON with hot reload and network sync | ✔ Actives cast from a spell book while holding a weapon or staff ("equip them and hold a weapon!") | **Runes as ammo**, cooldowns, no mana. Third-party mana add-ons exist ([Mana Attributes](https://modrinth.com/mod/mana-attributes), [Arch-RPGMana](https://modrinth.com/mod/arch-rpgmana)) | n/a | n/a | n/a | n/a | ✖ | Client prediction plus server sync ([MR](https://modrinth.com/mod/spell-engine)) |
| Iron's Spells 'n Spellbooks | ✔ 100+ spells, bosses and structures | ~ Per-spell config; KubeJS add-on with 289k MR downloads | ✔ Spells from scrolls or spellbooks. A keybind or spell-wheel casting flow is general knowledge, **unverified** here | **Mana** | n/a | n/a | n/a | n/a | ✖ | No skill tree. "Iron's Spells Dynamic Skill Trees" (a Pufferfish add-on, 2.31M CF) fills that gap ([CF](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks-dynamic-skill-trees)) |
| Origins | ✔ 9 origins plus Human | ✔ Highly data-driven; large data-pack community | ✔ Active powers on **2 keys** (primary/secondary). The [Extra Keybinds](https://modrinth.com/mod/origins-extra-keybinds-fabric) add-on has 204k downloads | Per power, defined in data packs | n/a | n/a | n/a | Origin choice only | ✖ | No progression tree. "Pufferfish's Skills x Origins" adds one (60k MR) |
| Project MMO | ✔ Skills that level as you play | ✔ Configs and data packs. Config complexity is a recurring issue theme | Mostly passive perks | ✖ | Skill menu, with scroll bugs ([#765](https://github.com/Caltinor/Project-MMO-2.0/issues/765), [#781](https://github.com/Caltinor/Project-MMO-2.0/issues/781)) | Glossary screen | n/a | n/a | ✖ | Multiplayer crash ([#790](https://github.com/Caltinor/Project-MMO-2.0/issues/790)) |
| Ars Nouveau | ✔ | ✔ Glyph-based spell crafting | ✔ Spells | Mana | n/a | n/a | n/a | n/a | Spellbook slots (general knowledge, **unverified**) | – |
| Mine and Slash | ✔ "Huge talent tree", spell masteries, ascendancies | ~ "Highly configurable" | ✔ Spells and passives | Mana (? per wiki) | ? | ? | ? | ? | ✖ (?) | Not compatible with other spell mods ([MR](https://modrinth.com/mod/mine-and-slash)) |
| Epic Fight: Skill Tree | ✔ Skill tree pages | ? | ✔ Epic Fight skills | XP converts into ability points | Pages | ? | Unlocks follow tree order | ? | ✖ | Official add-on ([MR](https://modrinth.com/mod/epic-fight-skill-tree)) |
| Bending | ✔ 4 elements, 60+ abilities | ✔ Configs and an API | ✔ Actives **bound to hotbar slots** with commands, plus passives and sequences | Cooldowns | n/a | n/a | n/a | n/a | ~ Ability "Presets" ([wiki](https://github.com/PrimordialMoros/Bending/wiki)) | Server-side on Fabric, with a plugin version for Paper |
| Elementals (saperate) | ✔ 4 elements plus Lightning, Blood and Metal; 12+ abilities each | ? | ✔ Keybinds that change with context (sneak alters the cast) | ? | ? | ? | Points are earned by using abilities ([wiki](https://github.com/saperate/Elementals/wiki/Getting-started)) | ? | ✖ | – |
| Archetypes | ✔ 3 classes × 3 trees | ? | ✔ Actives and passives | **Mana pool** | ? | ? | ? | ✔ Potion of Amnesia I/II, which costs levels | ✖ | "Server-authoritative" ([MR](https://modrinth.com/mod/archetypes)) |
| Vault Hunters 3 | ✔ | Configs are loadable through the VH API | ✔ Abilities, talents and expertises | ? | ? | ? | ? | ✔ Skill Altar | ✔ **Builds/loadouts** at the Skill Altar; the QOL Hunters add-on raises the cap to 10 ([CF](https://www.curseforge.com/minecraft/mc-mods/qol-hunters)) | Modpack only, 1.18.2 |

### 1c. The 26.2 / 26.3 landscape

| Need | What exists on 26.x | Gap |
|---|---|---|
| Large passive web | Pufferfish's Skills plus content packs (Default Skill Trees, Stronger Skill Tree, Adventurer Skills); an unofficial Passive Skill Trees port (Fabric, 26.1.2/26.2, about 3.8k downloads); eXc Ultimate (about 200 nodes, Fabric 26.2, 349 downloads) | No first-party, multi-loader passive web of this scale, with search and path allocation |
| Elemental active powers with mana | Spell Engine + Wizards (runes, not mana; heavy dependency chain); Bending (Fabric only, no tree); Archetypes (26.2, about 2.4k downloads); Super Powers (a data pack) | No dependency-free mod combining tree, actives and mana on Fabric **and** NeoForge |
| Big magic mods | **None**: Iron's, Ars Nouveau and Origins stop at 1.21.1 | Iron's and Ars players who move to 26.x have nothing comparable |

---

## 2. Top recurring player complaints and gaps

Ranked by how often the problem appears across mods and how strong the evidence is.

1. **Dependency chains and missing dependency metadata.**
   - Simply Skills needs 5 or more mods.
   - Wizards needs 4, plus Spell Engine's 3–5.
   - Skill Tree (RPG Series) lists 5.
   - Mine and Slash needs 5 libraries and warns it is incompatible with other spell mods.
   - Passive Skill Trees needs AttributeFix plus a health-fix mod.
   - Iron's dependencies are "not specified anywhere" on Modrinth ([#1251](https://github.com/iron431/Irons-Spells-n-Spellbooks/issues/1251)).

   Players and packmakers pay for this in install friction and version-mismatch crashes.
2. **Loader and version fragmentation, and abandoned or unofficial ports.**
   - Passive Skill Trees issues include "1.21.1? Fabric?" (#305) and "Please update the mod to Neoforge 1.21.1" (#295) ([issues](https://github.com/Daripher/Passive-Skill-Tree/issues)).
   - Its Fabric port breaks when joining a dedicated server directly, and the original author replied "I didn't make the fabric port" ([#308](https://github.com/Daripher/Passive-Skill-Tree/issues/308)).
   - Simply Skills issues ask for a port (#142) and for 1.21.4 (#132) ([issues](https://github.com/Sweenus/SimplySkills/issues)); its last update was March 2025.
   - Iron's FAQ answers porting with "TLDR: No".
   - LevelZ says "no plans to develop a Forge version".
3. **Frameworks that ship empty.**
   - Pufferfish's Skills: "This mod is just a framework. You need to install skill trees separately."
   - Its FAQ covers "No skill trees provided" and "There doesn't seem to be anything here…" ([FAQ](https://puffish.net/skillsmod/docs/players/faq)).
   - The same message appears in Simply Skills issue #135, and Pufferfish has #95 ("No skills despite the default skill tree being installed").
4. **No player-facing, configurable respec.**
   - Pufferfish's Skills offers reset only through admin commands, which led to add-ons such as [Respec Skill](https://modrinth.com/mod/respec-skill) and Book of Unlearning.
   - Passive Skill Trees costs 20% of your levels per Amnesia Scroll by default, and the scroll comes from the Ender Dragon.
   - Simply Skills permanently destroys specialisation points.
   - Requests include "reset skills on death" ([#18](https://github.com/pufmat/skillsmod/issues/18)) and "disable cooldown when storing build" (Simply Skills #145).
5. **Hard caps and too few ways to earn points.**
   - Simply Skills caps you at 42 points; issue #133 asks to raise it, and a no-limit data pack has 14k downloads.
   - Passive Skill Trees earns points from XP only. A "Mob Kill Points" add-on exists (24k MR), and issues ask for "Server Points" (#259), "wisdom scroll instead of xp" (#261) and report "Can't buy skill points" (#225).
6. **Limited tree-graph features for creators.**
   - Pufferfish issue [#20](https://github.com/pufmat/skillsmod/issues/20) asks for prerequisites beyond point cost, **one-way connections**, rules for nodes with multiple parents, **mid-tree exclusive branches**, buying points with XP, and toggleable perks. The maintainer agrees but has no design yet.
   - Issue [#153](https://github.com/pufmat/skillsmod/issues/153) asks for **multi-rank nodes**, citing Vault Hunters.
   - Issue #51 asks for a desktop editor instead of the web page.
7. **Active abilities are bolted on or keybind-limited.**
   - Origins has 2 active keys, and the Extra Keybinds add-on has 204k downloads.
   - Bending makes you bind abilities to hotbar slots with commands ("your amount of slots is finite").
   - Spell Engine spells need you to hold a specific weapon or spell book.
   - Iron's has no tree at all; the Pufferfish bridge "Dynamic Skill Trees" has 2.31M CF downloads.

   Players clearly want **tree progression plus actives** in one system.
8. **Server and multiplayer correctness.**
   - Passive Skill Trees: "Server config overruled by local config" (#223), "Crafting talent not showing up on Server" (#307), and the Fabric port's "No tree found" (#308).
   - Pufferfish: an exploit where logging out resets the anti-farm limiter (#61).
   - Project MMO: a multiplayer crash (#790) and "failed to process synchronized task" (#707).
9. **Missing screen features: stats summary, descriptions, persistent camera.**
   - Pufferfish requests: a stats overview (#17), XP gain display (#28), saved zoom and position (#63, done in 0.13.0), and "Cannot See Skill Descriptions" (#163).
   - An add-on exists just to put a skill-tree button in the inventory ([MR](https://modrinth.com/mod/pufferfish-skills-inventory-button)).
   - Project MMO has scroll bugs at some GUI scales (#765, #781).
10. **Compatibility breakage with popular mods.**
    - Passive Skill Trees: Tinkers' Construct bows (#222, #269, #292), Create projectiles (#309), ArmorPlus (#241).
    - Pufferfish: Epic Fight (#49), Inventorio (#40), Caelus (#45).
    - Iron's: an `@Overwrite` lectern mixin that breaks Map Atlases and Amendments ([#1282](https://github.com/iron431/Irons-Spells-n-Spellbooks/issues)).
11. **Balance at the extremes.**
    - Stronger Skill Tree: "WARNING IF YOU COMPLETE THE TREE YOU WILL BE VERY STRONG".
    - Passive Skill Trees suggests installing difficulty mods if you feel too strong.
    - Simply Skills: "Fan of blades is a bit too good" (#137).
    - Iron's: spells are weaker than a diamond sword ([#466](https://github.com/iron431/Irons-Spells-n-Spellbooks/issues/466)).

    Packs want **tunable numbers without editing every node**.
12. **No build sharing, planning or loadouts.** None of the surveyed tree mods offers player-facing build codes or a planner.
    - Loadouts exist only in Vault Hunters (Skill Altar builds; QOL Hunters raises the cap to 10) and in Bending's presets.
    - Tree "export" features, such as Skill Tree (Fabric) and Wan's Skill Tree, export pack-maker JSON, not player builds.

---

## 3. Prioritized feature list

Each item names the gap it closes, using the numbers from section 2.

### P0: must ship in the rebrand release

1. **No required dependencies.** Fabric API is the only requirement, and only on Fabric; Forge and NeoForge need nothing. (Gaps 1, 2)
   - Shade or jar-in-jar any helper code.
   - Register our own attributes, such as `remnants:max_mana`, `remnants:mana_regen`, `remnants:fire_damage` and `remnants:cooldown_reduction`.
   - Raise vanilla attribute caps (max health, armor) ourselves behind a config flag, so AttributeFix is not required. Defer to AttributeFix when it is present.
2. **Same features on all 6 targets on day one**: 1.20.1 Fabric/Forge, 1.21.1 Fabric/NeoForge, 26.2 and 26.3 Fabric/NeoForge. (Gap 2)
   - Use one feature matrix in the README and the listing.
   - Name files `remnants-skills-<loader>-<mc>-<ver>.jar`, the way the sibling RAS mod does.
3. **Playable with no configuration.** (Gaps 3, 7)
   - Ship a complete default tree: a web of about 250–400 nodes in six elemental regions (fire, frost, storm, earth, void, life).
   - Include a central start, small stat nodes, notables, and 2–3 keystones per element.
   - Ship at least 2–3 active powers per element.
   - Never show an empty screen. If a pack removes every tree, show a "No trees loaded: run `/reload` or check `logs/latest.log`" panel with a link to the docs.
4. **Server-authoritative allocation in three steps: stage, preview, confirm.** (Gap 8)
   - Clicks stage nodes on the client, and the screen shows "current → after" stat deltas.
   - **Enter** or **Confirm** sends one packet with the full allocation.
   - The server checks connectivity to the allocated set, points, prerequisites, exclusivity and caps, then applies everything or nothing.
   - Reuse the RAS 5.0 design ("Confirm is atomic") and its wording.
5. **Data-driven trees that the server sends to clients.** (Gaps 3, 8)
   - Trees, nodes and abilities are data-pack JSON, for example `data/<ns>/remnants/trees/*.json` and `.../abilities/*.json`.
   - The server sends them on login and after `/reload`, checked with a content hash, so clients never need the data pack. This closes the "No tree found on dedicated server" class of bug.
   - Validation errors go to ops in chat with file and line.
6. **Shift-click path allocation.** (Gap 9)
   - Shift-click any unallocated node to stage the cheapest path to it, using BFS or Dijkstra over node cost from the allocated set.
   - Highlight the path and show "+N points" before confirming.
   - Ctrl-click a staged leaf to unstage it.
7. **Search with highlight and jump-to.** (Gap 9)
   - Press `Ctrl+F` or click the search field. Matching covers names, stat lines and tags (`fire`, `crit`, `keystone`), and shows a match count.
   - **Enter** cycles through matches and smoothly pans and zooms the camera to each one.
   - Non-matches are dimmed rather than hidden.
8. **Navigation.** (Gap 9)
   - Zoom toward the cursor, and pan by dragging, with WASD, or with the arrow keys.
   - Remember the camera per tree.
   - Add a "center on my build" button, a mini-overview, and keyboard and controller focus navigation.
   - Keep it at 60 fps with 1,000 or more nodes by batching the line and icon rendering.
9. **Built-in, configurable respec.** (Gap 4) Server config supports:
   - A full reset through an item (craftable or loot) and/or a command.
   - Refunding a single node, which is only allowed when the rest of the build stays connected.
   - A free respec window up to level N.
   - Cost as XP levels, an item, or nothing; a cooldown; and admin `/remnants respec <player> [tree]`.
   - An optional "reset on death".
10. **Dedicated active-ability bar.** (Gap 7)
    - 4 slots by default (configurable up to 8), each with its own rebindable key.
    - A radial quick-select key as an alternative.
    - Abilities **do not require holding a special item**. A config can require a focus or catalyst item for packs that want that.
    - The HUD shows cooldown sweeps and mana cost.
    - The client predicts casts for responsiveness; the server validates and executes.
11. **Mana as attributes**, with any name the theme needs (for example "Essence"). (Gap 7)
    - Max mana, regeneration, regen delay and cost reduction are normal attributes, so tree nodes, gear and other mods can modify them.
    - The HUD bar position is configurable, and it hides when full or out of combat.
    - The two sliders an admin needs most often are global mana-cost and damage multipliers.
12. **Stats summary panel.** It totals every bonus from allocated nodes, with a filter by element, and appears both in the tree screen and on an inventory tab. (Gap 9)

### P1: fast follow, the most distinctive features

13. **Build share codes.** (Gap 12)
    - Format: a compact base64url string `RSP1-<treeId>-<treeHash8>-<bitset of node indices>-<ability bar>`.
    - Add a copy-to-clipboard button and `/remnants build export|import`.
    - An import loads as a **planned (ghost) overlay** and spends no points.
    - If the tree hash differs, show a warning and mark missing nodes.
14. **Planner mode.** Allocate ghost nodes without points to theory-craft. Stat totals preview the plan, and the plan can be committed node by node once you have points. (Gap 12)
15. **Loadouts.** (Gap 12)
    - Players save 3 builds by default (configurable), each with a separate ability-bar preset.
    - Switching is free, costs something, or is allowed only at a "Remnant Altar" block or outside combat.
    - Packs that want commitment can set the switching cooldown or cost.
16. **Richer node graph primitives.** (Gap 6) The JSON schema supports:
    - Multi-rank nodes, for example 0/3.
    - One-way edges, drawn as arrows.
    - Prerequisites written as an any/all of node IDs, element mastery, advancement or level.
    - **Exclusive groups** ("choose 1 of these 3 keystones"); the UI warns before locking a path.
    - "Jewel/socket" nodes that accept an item.
17. **Configurable point sources.** XP levels, mob kills (with per-entity and per-chunk anti-farm that **survives relog**), advancements, items (tomes), commands, and integration with RAS levels (a point every N levels). An optional "buy a point with XP" button. (Gaps 5, 8)
18. **In-game editor that saves into the world.** (Gap 6)
    - Writes to `<world>/datapacks/remnants_custom/` (not the game root) and hot-reloads.
    - Includes undo and redo, grid snap, and duplicate or mirror region.
    - Publish a JSON Schema so packmakers get autocomplete in their editors.
19. **Ability definitions in JSON** made from building blocks such as projectile, beam, AoE, dash/blink, shield, summon, status effect and terrain shaping. Tree nodes can modify abilities ("Fireball +1 projectile", "Frost Nova leaves ice"). Packs can add powers without Java. (Gaps 6, 7)
20. **Global balance knobs.** Global multipliers for node and ability values per element, a cap on total points, and "keystone power" scaling, so packs can tune without editing every node. (Gap 11)

### P2: ecosystem and polish

21. **Compatibility, not hard dependencies.**
    - Expose everything as attributes and tags so Better Combat, Epic Fight, Spell Engine and Iron's (1.20.1/1.21.1) read our numbers.
    - Optional bridges: use Iron's mana when Iron's is present; accept Curios or Trinkets catalysts.
    - Never use `@Overwrite`. (Gap 10)
22. **Events for developers and scripts.** Node allocated, respec, ability cast, mana changed, build imported. A KubeJS plugin can come later. (Gap 6)
23. **Admin tools.** `/remnants inspect <player>` shows build, points and code. Add per-tree permission nodes, a toggle to lock trees for progression gating, and an anti-cheat log for rejected allocation packets. (Gap 8)
24. **Accessibility and localization.** Colorblind-safe element palettes plus element icons (color is never the only cue). Every string goes through translation keys. Layouts adapt to GUI scale, as RAS already does.
25. **Cross-loader client and server** (a Fabric client on a NeoForge server), only if the networking layer allows it cheaply. Pufferfish advertises this, but it is low priority.

---

## 4. Positioning and marketing

### 4a. Name and brand notes

- **The rename is well-founded.** saperate's *Elementals* (a bending mod, 131,557 MR downloads) and its add-ons, such as "Bending Poses (Elementals Addon)", already own the word on Modrinth ([search](https://modrinth.com/mods?q=elementals)).
- **Keep search continuity.** Put "Formerly *The Elementals - RPG*" in the first paragraph for one or two major versions.
  - Keep the existing project pages so current followers carry over: CurseForge `the-elementals-rpg`, Modrinth `the-elementals`.
  - Modrinth lets you change the slug. Whether CurseForge allows a slug change is **unverified**.
- **Separate it from the sibling mod.** *Remnants | RPG Stats, Skills & Progression* (RAS) is about attribute levelling. Use this split in both descriptions:
  - RAS: "Level up and spend attribute points."
  - Skills and Powers: "Chart a passive web and wield elemental powers."
  - Cross-link the two, and advertise the RAS-level → skill-point integration as "Better together, never required."
- **Titles.** Do not put other games' names, or competitors' mod names such as "Passive Skill Trees", in titles or summaries. Describing the genre in the body ("inspired by classic action-RPG passive trees") is fine.

### 4b. Listing summary options (one line)

- "A sprawling passive skill web and elemental powers on hotkeys. One jar, no dependencies. Fabric, Forge and NeoForge, 1.20.1 to 26.3."
- "Chart your path through a constellation of passives, then unleash fire, frost, storm, earth, void and life."
- "Build your elementalist: 300+ passives, keystones that change the rules, and active powers fueled by mana."

### 4c. Angles and wording

| Angle | Why it works (gap) | Wording ideas |
|---|---|---|
| **Ready for 26.2 and 26.3** | Iron's, Ars, Origins, Simply Skills and Passive Skill Trees are absent there (section 1c) | "Ready for 26.3 today, alongside 1.20.1 and 1.21.1." "One feature set on every loader." |
| **Install and play** | Dependency chains and empty frameworks (gaps 1, 3) | "One jar. No libraries. No empty screens." "Works out of the box, with every node configurable." |
| **Passive web plus active powers** | Trees are passive-only; spell mods have no tree (gap 7) | "Passives that shape you. Powers you cast." "Keystones that change the rules, not just +5%." |
| **The planning fantasy** | No search, path allocation or planner (gaps 9, 12) | "Search the web, shift-click a path, preview every stat before you commit." "Plan in ghost mode and commit when you're ready." |
| **Share your build** | No competitor has build codes (gap 12) | "Copy a code, share your build." "Import a friend's build as a plan, then walk it point by point." |
| **Respec without regret** | Harsh or missing respec (gap 4) | "Respec your way: free early, cheap later, or hardcore. Server owners decide." |
| **Server-checked** | Desync and dedicated-server bugs (gap 8) | "Every point is checked by the server, so your build is identical for everyone." (This echoes the RAS 5.0 messaging.) |
| **Pack-maker friendly** | Editor and graph limits (gap 6) | "Trees and powers in plain JSON, synced to every client." "In-game editor that saves into your world." "One-way paths, ranked nodes, exclusive keystones." |
| **Part of the Remnants suite** | Brand continuity (RAS, RPG Mobs, Eclipsed Realm) | "Pairs with Remnants \| RPG Stats for a full progression stack, though neither requires the other." |

### 4d. Description and gallery checklist

- Put a feature matrix (loader × MC version) at the very top. Players look for it first (gap 2).
- Show these as GIFs or short clips:
  1. Shift-click path allocation.
  2. Search jumping to "fire" keystones.
  3. Pasting a share code to see the ghost plan.
  4. Ability bar casts with mana.
  5. Respec.
- Add a "For server owners" box: server authority, anti-farm, respec controls, admin commands.
- Add a "For pack makers" box: JSON paths, schema link, editor, global balance multipliers, events.
- Add a short FAQ:
  - "Do I need other mods?" (No.)
  - "Can I use it with Iron's or Spell Engine?" (Yes, through attributes and optional bridges; claim only what has been tested.)
  - "Can I disable the default tree?" (Yes.)
- Body keywords for search, not for the title: *skill tree, passive tree, talent tree, skill points, abilities, powers, spells, mana, elemental, fire, frost, ice, lightning, storm, earth, void, shadow, nature, life, RPG, class, build, respec*.
- Only make claims that are true at release. In particular, do not advertise share codes, loadouts or cross-loader play before they ship.

---

## Appendix: method notes and unverified items

- **Unverified:**
  - The dependency list for the Passive Skill Trees Fabric port; it comes from a third-party summary.
  - Ars Nouveau's required dependencies; the Modrinth metadata lists none.
  - Iron's casting UX details: spell wheel and keybinds.
  - Mine and Slash's UI and respec features.
  - Vault Hunters ability resource and UI details.
  - Bending and saperate's Elementals download counts on CurseForge (not checked).
  - Whether CurseForge allows slug changes.
  - An unofficial NeoForge 1.21.1 port of Passive Skill Trees, listed on ModpackIndex; that page returned 403 and could not be opened.
- The Passive Skill Trees cap appears as "85 (configurable)" on the CurseForge page, but `ServerConfig.java` on the `1.20.1` branch defaults to 100.
- The download figures are cumulative and differ between platforms. CurseForge counts include launcher auto-installs, so compare mods within one platform rather than across platforms.
