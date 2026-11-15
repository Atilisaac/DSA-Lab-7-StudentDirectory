#!/usr/bin/env sh
# Compile every source file and run the acceptance runner with assertions on.
set -eu

rm -rf out
mkdir -p out

javac -d out src/*.java test/*.java
java -ea -cp out LabTestRunner
