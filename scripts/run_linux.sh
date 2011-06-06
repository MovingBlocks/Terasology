#!/bin/bash
cd `dirname $0`
java -Xms128m -Xmx512m -classpath *:lib/*:lib/lwjgl-2.7.1/jar/* -Djava.library.path=lib/lwjgl-2.7.1/native/linux com.github.begla.blockmania.Main

