#!/bin/bash
cd `dirname $0`
java -Xms256m -Xmx512m -classpath Blockmania.jar -Djava.library.path=natives/linux com.github.begla.blockmania.Main

