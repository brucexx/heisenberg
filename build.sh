#!/bin/bash
export JAVA_HOME=$JAVA_HOME_1_6

rm -rf output
mkdir output

mvn clean package -Dmaven.test.skip=true

cd target
unzip -o heisenberg-server.zip
cd heisenberg-server
tar czf heisenberg-server.tgz *
cd ../../

mv target/heisenberg-server/heisenberg-server.tgz output/heisenberg-server-1.0.8.1.tgz
rm -rf ./target
