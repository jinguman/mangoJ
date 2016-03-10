#!/bin/bash

# crontab
# 1 1 1 * * /opt/mangoJ/bin/logClean.sh > /dev/null 2>&1

##### Variable #####

LOG_DIR=/opt/mangoJ/log
BACKUP_DIR=/opt/mangoJ/backup

if [ ! -d "$BACKUP_DIR" ]; then
        mkdir $BACKUP_DIR;
fi;

#Linux
DT=`date --date "-2 days day" +%Y-%m`;

#Linux
cd $LOG_DIR; find . -maxdepth 1 -name "mango.log.$DT*" -exec tar rvf $BACKUP_DIR/mango.$DT.log.tar {} \;
cd $LOG_DIR; find . -maxdepth 1 -name "traffic.log.$DT*" -exec tar rvf $BACKUP_DIR/traffic.$DT.log.tar {} \;


gzip $BACKUP_DIR/mango.$DT.log.tar
gzip $BACKUP_DIR/traffic.$DT.log.tar

rm $LOG_DIR/mango.log.*$DT*
rm $LOG_DIR/traffic.log.*$DT*