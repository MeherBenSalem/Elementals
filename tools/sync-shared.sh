#!/usr/bin/env bash
# Copies the version-independent sources from the 1.21.1 reference root to the other version roots.
# Version-specific files (Compat, StatApplier, items, entities, GuiCanvas, the screen wrapper, keys,
# renderers and the loader modules) are maintained per version and are never overwritten here.
set -euo pipefail
cd "$(dirname "$0")/.."
SRC=1.21.1/common/src/main/java/com/naizo/elementals
SHARED=(
  core
  Elementals.java
  platform/ElementalsPlatform.java
  net/Net.java
  integration/RasBridge.java
  power/PowerManager.java
  power/PowerStorage.java
  power/CombatHooks.java
  ability/Ability.java
  ability/AbilityContext.java
  ability/AbilityRunner.java
  ability/Fx.java
  ability/Targets.java
  ability/Zones.java
  ability/PyromancyAbilities.java
  ability/StormAbilities.java
  ability/UmbralAbilities.java
  ability/TideAbilities.java
  ability/EarthAbilities.java
  ability/VerdanceAbilities.java
  command/PowersCommands.java
  registry/ModItems.java
  registry/ModContent.java
  registry/ModEntities.java
  registry/ModEffects.java
  item/SpellBookItem.java
  item/RemnantTomeItem.java
  item/TomeOfUnbindingItem.java
  client/Canvas.java
  client/TextField.java
  client/TreeView.java
  client/PowerHud.java
  client/ClientPowers.java
)
for root in "$@"; do
  DST=$root/common/src/main/java/com/naizo/elementals
  for path in "${SHARED[@]}"; do
    mkdir -p "$(dirname "$DST/$path")"
    if [ -d "$SRC/$path" ]; then
      rm -rf "${DST:?}/$path"
      cp -r "$SRC/$path" "$DST/$path"
    else
      cp "$SRC/$path" "$DST/$path"
    fi
  done
  mkdir -p "$root/common/src/test/java/com/naizo/elementals/core"
  cp 1.21.1/common/src/test/java/com/naizo/elementals/core/*.java "$root/common/src/test/java/com/naizo/elementals/core/"
  cp 1.21.1/common/src/main/resources/assets/elementals/lang/en_us.json "$root/common/src/main/resources/assets/elementals/lang/en_us.json" 2>/dev/null || true
  echo "synced shared sources into $root"
done
