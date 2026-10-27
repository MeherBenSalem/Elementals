#!/bin/bash
# cap.sh <name> <real_seconds> -- records the portrait display at 15 fps (the slowed game renders ~6 fps real)
mkdir -p /tmp/claude-0/reel2/raw
ffmpeg -loglevel error -y -f x11grab -video_size 1080x1920 -framerate 15 -draw_mouse 0 -i :2 -t $2 -threads 2 -c:v libx264 -preset ultrafast -crf 10 -pix_fmt yuv420p /tmp/claude-0/reel2/raw/$1.mkv
