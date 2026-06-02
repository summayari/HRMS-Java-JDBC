#!/bin/bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d out @sources.txt
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi
java -cp "out:lib/*" hrms.Main
