#!/bin/bash

xdotool search --name "Intellij" windowactivate

xdotool mousemove 1383 41
xdotool click 1
sleep 1

xdotool click 1
sleep 1

xdotool mousemove 1307 44
xdotool click 1
sleep 1

xdotool search --name "Mozilla Firefox" windowactivate

xdotool key ctrl+R
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

sleep 2

xdotool click 1
