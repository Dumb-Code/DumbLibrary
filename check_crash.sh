#!/bin/sh

if [ ! -d "/run/crash-reports" ]
then
    ls run
    exit 9999
fi