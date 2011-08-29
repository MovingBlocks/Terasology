#!/bin/bash
cd `dirname $0`
java -Xms512m -Xmx1024m -classpath Blockmania.jar -Djava.library.path=natives/macosx com.github.begla.blockmania.Main

