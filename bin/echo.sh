#!/bin/bash

set -e

if [[ $BASH_SOURCE = */* ]]; then
    DIR=${BASH_SOURCE%/*}/
else
    DIR=./
fi

exec java -Xmx256M -jar "$DIR/../echo/target/scala-2.13/echo-assembly-0.1.0-SNAPSHOT.jar"
