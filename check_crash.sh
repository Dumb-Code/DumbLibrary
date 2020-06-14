#!/bin/sh

ls run
if [ -d "/run/crash-reports" ]; then
    exit 9999
fi