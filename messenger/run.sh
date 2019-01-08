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
sleep 1

xdotool mousemove 164 236
xdotool click 1

setxkbmap de
xdotool type sascha.baumeister@gmail.com

xdotool mousemove 564 236
xdotool click 1

xdotool mousemove 164 268
xdotool click 1

xdotool type sascha

xdotool mousemove 83 307
xdotool click 1

sleep 1.1

xdotool mousemove 324 153
xdotool click 1

# click ines
sleep 0.3
xdotool mousemove 90 270
xdotool click 1
