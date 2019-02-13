#!/usr/bin/env bash

RECORDING=$2
DEST=record-$(date '+%Y-%m-%d-%H:%M:%S%z')-f00.nut

echo "Starting Nageru..."
echo "Recording dir ${RECORDING}"

ffmpeg -f pulse -i default -ac 2 -acodec pcm_s16le -f x11grab -r 50 -s 1920x1080 -i :0.0 -vcodec libx264 -vf scale=1280:720 -t 5 "${RECORDING}/${DEST}"

echo "Nageru over"
