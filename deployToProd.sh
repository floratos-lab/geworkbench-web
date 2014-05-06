#!/bin/sh
#$ -S /bin/sh

. ~/.bash_profile

cd ~/git_src/geworkbench-web
git pull > ~/git_src/geworkbench-web/deployToTest.log
cp  ~/git_src/db_prod_config/persistence.xml  ~/git_src/geworkbench-web/src/main/resources/META-INF/.


mvn package 
