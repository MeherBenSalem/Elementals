#!/bin/bash
# recui.sh <name> <seconds> -- records the portrait display while the caller drives input
O=/tmp/claude-0/reel/clips; mkdir -p $O
ffmpeg -loglevel error -y -f x11grab -video_size 1080x1920 -framerate 30 -draw_mouse 0 -i :2 -t $2 -c:v libx264 -preset ultrafast -crf 14 -pix_fmt yuv420p $O/$1.mp4
