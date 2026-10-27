#!/bin/bash
export DISPLAY=:2
S="/tmp/claude-0/bin/srv.sh /tmp/claude-0/prod/mkt-1.21.1"
c() { for x in "$@"; do $S cmd "$x"; sleep 0.3; done; }
c "tp @e[type=!minecraft:player] ~ -200 ~" "kill @e[type=!minecraft:player]" "effect clear Remnant" \
  "effect give Remnant minecraft:resistance infinite 4 true" "effect give Remnant minecraft:saturation infinite 1 true" \
  "tp Remnant -1398.5 110 -3188.5 160 10"
sleep 3
for p in "^ ^ ^6" "^2 ^ ^7" "^-2 ^ ^6.8" "^0.6 ^ ^8.5"; do
  c "execute as Remnant at @s rotated ~ 0 run summon minecraft:vindicator $p {NoAI:1b,PersistenceRequired:1b,Silent:1b}"
done
c "execute as @e[type=minecraft:vindicator] at @s run tp @s ~ ~ ~ facing entity Remnant" \
  "effect give @e[type=minecraft:vindicator] minecraft:resistance infinite 4 true" "powers cooldowns Remnant"
sleep 14
/tmp/claude-0/reel2/cap.sh combo 85 &
sleep 6; xdotool key z
sleep 15; xdotool key b
sleep 13; xdotool key r
sleep 19; xdotool key h
wait
