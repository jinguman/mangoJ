#!/bin/bash
## FUCTIONS ##
usage() {
    echo "mangoJA Server startup/down script, rel. 2015.10.27."
    echo "Usage:"
    echo "  $0  start yyyy mm(start process)"
    echo "  $0  stop (stop process)"
    echo " "
} # usage

### MAIN ###

#PID=`ps -ef | grep java | grep spis |  grep -v grep | grep -v CassandraDaemon | awk '{print $2}'`
PID=`jps | grep MangoJA | awk '{print $1}'`
export JAVA=/usr/local/java/bin/java
export JAVAOPTION='-Dlog4j.configuration=file:../config/log4j.xml -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:./gc.log -Dsun.reflect.inflationThreshold=30'


export CLASSPATH=$CLASSPATH:../lib/JSAP-2.1.jar
export CLASSPATH=$CLASSPATH:../lib/log4j-1.2.17.jar
export CLASSPATH=$CLASSPATH:../lib/mangoJ-0.1.jar
export CLASSPATH=$CLASSPATH:../lib/mongo-java-driver-3.1.0.jar
export CLASSPATH=$CLASSPATH:../lib/mysql-connector-java-5.1.34.jar
export CLASSPATH=$CLASSPATH:../lib/rxtx-2.1.7.jar
export CLASSPATH=$CLASSPATH:../lib/seedCodec-1.0.11.jar
export CLASSPATH=$CLASSPATH:../lib/seisFile-1.6.6.jar
export CLASSPATH=$CLASSPATH:../lib/slf4j-api-1.7.12.jar
export CLASSPATH=$CLASSPATH:../lib/slf4j-log4j12-1.7.12.jar
export CLASSPATH=$CLASSPATH:../lib/stax2-api-3.1.1.jar
export CLASSPATH=$CLASSPATH:../lib/stax-api-1.0-2.jar
export CLASSPATH=$CLASSPATH:../lib/woodstox-core-lgpl-4.2.0.jar
export CLASSPATH=$CLASSPATH:../lib/lombok.jar
export CLASSPATH=$CLASSPATH:../lib/netty-all-4.0.32.Final.jar
export CLASSPATH=$CLASSPATH:../lib/commons-io-2.4.jar
export CLASSPATH=$CLASSPATH:../

case "$1" in
  start)
       if [ ! -z $PID ]; then
           echo "mangoJA already started.($PID)"
           exit 1
       fi 
      nohup $JAVA -classpath $CLASSPATH $JAVAOPTION $JAVA_OPTS com.kit.MangoJA &
#      $JAVA -classpath $CLASSPATH $JAVAOPTION $JAVA_OPTS com.kit.MangoJA
      echo "mangoJA Server are started."
  ;;

  stop)
      #PID=`ps -ef | grep java | grep daps | grep -v grep | grep -v CassandraDaemon | awk '{print $2}'`
      kill -15 $PID
      echo "mangoJA process are finish.($PID)"
      ;;

  *)
      usage
      exit 1
      ;;
esac

exit 0

