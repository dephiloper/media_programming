#!/bin/bash

xdotool search --name "Intellij" windowactivate

xdotool mousemove 1383 41
xdotool click 1
sleep 0.5

xdotool click 1
sleep 0.5

xdotool mousemove 1307 44
xdotool click 1
sleep 1

xdotool search --name "Mozilla Firefox" windowactivate

xdotool key alt+1
sleep 2.8
xdotool key ctrl+r
