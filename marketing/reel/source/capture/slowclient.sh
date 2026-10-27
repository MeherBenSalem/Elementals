#!/bin/bash
# slowclient.sh <factor> [server port] -- launches the dev client directly (no Gradle) under libfaketime at the given clock speed
F=${1:-0.1}; PORT=${2:-25570}
cd /home/user/Elementals/1.21.1/fabric/runs/client
mapfile -t CMD < /tmp/claude-0/reel2/client_cmd.txt
CMD[${#CMD[@]}-1]="localhost:$PORT"
export DISPLAY=:2 LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe MESA_GL_VERSION_OVERRIDE=4.5 MESA_GLSL_VERSION_OVERRIDE=450
export FAKETIME="+0 x$F" FAKETIME_DONT_FAKE_MONOTONIC=0 LD_PRELOAD=/usr/lib/x86_64-linux-gnu/faketime/libfaketimeMT.so.1
exec "${CMD[@]}"
