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

PID=`jps | grep MangoJA | awk '{print $1}'`
export JAVA=/usr/local/java/bin/java
export JAVAOPTION='-Dlogback.configurationFile=file:../conf/logback.xml -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:./gc.log -Dsun.reflect.inflationThreshold=30'

export CLASSPATH=$CLASSPATH:../lib/mangoJ-0.1.jar
export CLASSPATH=$CLASSPATH:../lib/mongo-java-driver-3.2.1.jar
export CLASSPATH=$CLASSPATH:../lib/seedCodec-1.0.11.jar
export CLASSPATH=$CLASSPATH:../lib/seisFile-1.6.6.jar
export CLASSPATH=$CLASSPATH:../lib/slf4j-api-1.7.12.jar
export CLASSPATH=$CLASSPATH:../lib/lombok.jar
export CLASSPATH=$CLASSPATH:../lib/netty-all-4.0.32.Final.jar
export CLASSPATH=$CLASSPATH:../lib/commons-io-2.4.jar
export CLASSPATH=$CLASSPATH:../lib/commons-configuration-1.10.jar
export CLASSPATH=$CLASSPATH:../lib/commons-lang-2.6.jar
export CLASSPATH=$CLASSPATH:../lib/commons-logging-1.2.jar
export CLASSPATH=$CLASSPATH:../lib/commons-pool-1.6.jar
export CLASSPATH=$CLASSPATH:../lib/commons-dbcp-1.4.jar
export CLASSPATH=$CLASSPATH:../lib/mysql-connector-java-5.1.34.jar
export CLASSPATH=$CLASSPATH:../lib/logback-access-1.1.3.jar
export CLASSPATH=$CLASSPATH:../lib/logback-classic-1.1.3.jar
export CLASSPATH=$CLASSPATH:../lib/logback-core-1.1.3.jar
export CLASSPATH=$CLASSPATH:../lib/spring-aop-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-aspects-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-beans-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-context-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-context-support-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-core-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-expression-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-instrument-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-jdbc-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-test-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/spring-tx-4.2.4.RELEASE.jar
export CLASSPATH=$CLASSPATH:../lib/jackson-annotations-2.7.0.jar
export CLASSPATH=$CLASSPATH:../lib/jackson-core-2.7.0.jar
export CLASSPATH=$CLASSPATH:../lib/jackson-databind-2.6.4.jar
export CLASSPATH=$CLASSPATH:../lib/jackson-dataformat-xml-2.5.1.jar
export CLASSPATH=$CLASSPATH:../lib/jackson-module-jaxb-annotations-2.7.0.jar
export CLASSPATH=$CLASSPATH:../lib/stax2-api-4.0.0.jar
export CLASSPATH=$CLASSPATH:../

case "$1" in
  start)
       if [ ! -z $PID ]; then
           echo "mangoJA already started.($PID)"
           exit 1
       fi 
      nohup $JAVA -classpath $CLASSPATH $JAVAOPTION $JAVA_OPTS app.kit.MangoJA &
#      $JAVA -classpath $CLASSPATH $JAVAOPTION $JAVA_OPTS app.kit.MangoJA
      echo "mangoJA Server are started."
  ;;

  stop)
      kill -15 $PID
      echo "mangoJA process are finish.($PID)"
      ;;

  *)
      usage
      exit 1
      ;;
esac

exit 0

