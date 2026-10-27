#!/bin/bash
# slowsrv.sh -- starts the capture server with only the JVM on the slowed clock (SLOW env, default 0.1)
/tmp/claude-0/bin/srv.sh /tmp/claude-0/prod/mkt-1.21.1 start /tmp/claude-0/reel2/fjava -Xmx3G -jar fabric-server-launch.jar nogui
