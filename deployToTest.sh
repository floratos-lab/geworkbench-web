#!/bin/sh
#$ -S /bin/sh

. ~/.bash_profile

cd ~/git_src/geworkbench-web
git pull > ~/git_src/geworkbench-web/deployToTest.log
cp  ~/git_src/db_config/persistence.xml  ~/git_src/geworkbench-web/src/main/resources/META-INF/.

mv $CATALINA_HOME/webapps/geworkbench.war $CATALINA_HOME/webapps/geworkbench.war_OLD
rm -r $CATALINA_HOME/webapps/geworkbench_OLD
mv $CATALINA_HOME/webapps/geworkbench $CATALINA_HOME/webapps/geworkbench_OLD

mvn tomcat7:deploy -Denv=test 
