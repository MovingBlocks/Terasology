#!/bin/bash
cd "$(dirname "$0")"
java -Xms128m -Xmx1024m -jar libs/Terasology.jar

# Alternatively use our Launcher from: https://github.com/MovingBlocks/TerasologyLauncher/releases
