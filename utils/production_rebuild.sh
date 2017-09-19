#!/bin/bash
# rebuild war file for the production deployment
# this script can be executed anywhere, but avoid doing so within an exiting project directory

CONFIG_LOCATION=/ifs/data/c2b2/af_lab/zji/geworkbench_current_config/

git clone https://github.com/geworkbench-group/geworkbench-web
WORKSPACE_LOCATION=./geworkbench-web/
scp  ${CONFIG_LOCATION}persistence.xml  ${WORKSPACE_LOCATION}src/main/resources/META-INF/.
scp  ${CONFIG_LOCATION}application.properties  ${WORKSPACE_LOCATION}src/main/resources/.

echo ...before cd
cd ${WORKSPACE_LOCATION}
echo now in `pwd`
mvn package
ls -l ${WORKSPACE_LOCATION}target/geworkbench.war
