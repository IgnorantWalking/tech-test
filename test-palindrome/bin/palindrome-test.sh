#!/bin/bash
SOURCE=$(dirname "$0")
JAR_LOCATION=$(find -L $SOURCE/.. -maxdepth 2 -name "palindrome-algorithm*.jar" -print | head -n 1)

if [ -z "$JAVA_HOME" ]
then
      JAVA_PATH="java"
else
      JAVA_PATH=$JAVA_HOME"/bin/java"
fi

$JAVA_PATH -jar $JAR_LOCATION $@
