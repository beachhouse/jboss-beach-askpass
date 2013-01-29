#!/bin/sh
if [ "$1" = "cancel" ]; then
    exit 1
fi
echo $1 | md5sum