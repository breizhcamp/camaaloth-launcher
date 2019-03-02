#!/usr/bin/env bash
export LANG=C

rsync -t --progress "$1" /tmp/"$2"