#!/bin/bash
# rec.sh <name> <ability|-> <near|far|far2|none> <game_seconds> [cast_at_game_s] -- game runs at 0.1x, so waits are x10
K=/tmp/claude-0/bin/mk.sh; S=10
name=$1; ability=$2; form=$3; secs=$4; at=${5:-0.3}; mob=${MOB:-minecraft:vindicator}
$K cmd "tp @e[type=!minecraft:player] ~ -200 ~" "kill @e[type=!minecraft:player]"
case $form in
  near) P=("^1.6 ^ ^3.4" "^-2.4 ^ ^3.0" "^3.0 ^ ^1.4" "^-3.2 ^ ^1.0" "^0.4 ^ ^5");;
  far2) P=("^ ^ ^8" "^2.4 ^ ^9" "^-2.4 ^ ^8.8" "^1 ^ ^10.5");;
  far) P=("^ ^ ^5.5" "^1.9 ^ ^6.6" "^-1.9 ^ ^6.4" "^0.7 ^ ^8");;
  *) P=();;
esac
for p in "${P[@]}"; do $K cmd "execute as Remnant at @s rotated ~ 0 run summon $mob $p {NoAI:1b,PersistenceRequired:1b,Silent:1b}"; done
[ ${#P[@]} -gt 0 ] && $K cmd "execute as @e[type=$mob] at @s run tp @s ~ ~ ~ facing entity Remnant" "effect give @e[type=$mob] minecraft:resistance infinite 4 true"
$K cmd "powers cooldowns Remnant"
sleep 12
/tmp/claude-0/reel2/cap.sh $name $(echo "$secs * $S" | bc) &
CAP=$!
sleep $(echo "$at * $S" | bc)
[ "$ability" != "-" ] && /tmp/claude-0/bin/srv.sh /tmp/claude-0/prod/mkt-1.21.1 cmd "powers cast Remnant $ability"
wait $CAP
echo /tmp/claude-0/reel2/raw/$name.mkv
