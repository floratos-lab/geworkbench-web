#!/bin/bash
# dump mysql database of geworkbench production
# this script should be execuate from mysql3.c2b2.columbia.edu

backup_path=//ifs/data/c2b2/af_lab/zji/geworkbench_production_backup/
mysqldump -u zji -p gework_web > ${backup_path}gework_web`date +%F`.sql

