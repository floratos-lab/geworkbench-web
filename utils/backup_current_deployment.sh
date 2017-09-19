#!/bin/bash
# backup the current deployment of geworkbench, except the database itself, which must be dumped separately

backup_path=//ifs/data/c2b2/af_lab/zji/geworkbench_production_backup/
date_string=`date +%F`
fullpath=${backup_path}${date_string}
mkdir -p ${fullpath}
scp -r ~/tomcat-instance/webapps/geworkbench zji@afdev.c2b2.columbia.edu:${fullpath}
scp ~/tomcat-instance/webapps/geworkbench.war zji@afdev.c2b2.columbia.edu:${fullpath}

