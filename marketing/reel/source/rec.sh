#!/bin/bash
# rec.sh <name> <ability|-> <near|far|none> <seconds> [cast_delay] -- slow-motion capture at tick rate 5 (play back 4x)
export DISPLAY=:2; K=/tmp/claude-0/bin/mk.sh; O=/tmp/claude-0/reel/clips; mkdir -p $O
name=$1; ability=$2; form=$3; secs=$4; delay=${5:-0.8}; mob=${MOB:-minecraft:vindicator}
$K cmd "tick rate 20" "kill @e[type=!minecraft:player]"
sleep 0.4
case $form in
  near) P=("^1.6 ^ ^3.4" "^-2.4 ^ ^3.0" "^3.0 ^ ^1.4" "^-3.2 ^ ^1.0" "^0.4 ^ ^5");;
  far2) P=("^ ^ ^8" "^2.4 ^ ^9" "^-2.4 ^ ^8.8" "^1 ^ ^10.5");;
  far) P=("^ ^ ^5.5" "^1.9 ^ ^6.6" "^-1.9 ^ ^6.4" "^0.7 ^ ^8");;
  *) P=();;
esac
for p in "${P[@]}"; do $K cmd "execute as Remnant at @s rotated ~ 0 run summon $mob $p {NoAI:1b,PersistenceRequired:1b,Silent:1b}"; done
[ ${#P[@]} -gt 0 ] && $K cmd "execute as @e[type=$mob] at @s run tp @s ~ ~ ~ facing entity Remnant" "effect give @e[type=$mob] minecraft:resistance infinite 4 true"
$K cmd "powers cooldowns Remnant"
sleep 1.2
$K cmd "tick rate 5"
sleep 0.5
ffmpeg -loglevel error -y -f x11grab -video_size 1080x1920 -framerate 30 -i :2 -t $secs -c:v libx264 -preset ultrafast -crf 14 -pix_fmt yuv420p $O/$name.mp4 &
FF=$!
sleep $delay
if [ "$ability" != "-" ]; then
  if [ "${KEY:-}" != "" ]; then xdotool key $KEY; else /tmp/claude-0/bin/srv.sh /tmp/claude-0/prod/mkt-1.21.1 cmd "powers cast Remnant $ability"; fi
fi
wait $FF
$K cmd "tick rate 20"
echo "$O/$name.mp4"
