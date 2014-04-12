#!/bin/sh

usage() {
	echo "Usage:"
	echo "    startup.sh [-c config_folder] [-l log_folder] [-d debug mode] [-h]"
	echo "Description:"
	echo "    config_folder - config folder path, not must,if empty,default classpath"
	echo "    log_folder - hsb server's logs base folder, /home/work  log path: /home/work/logs"
	echo "    debug_mode - 1|0  1 means debug port is open, 0 close ,default 0"
	echo "    -h - show this help"
	exit -1
}
LOG_BASE_DIR=""
CONFIG_DIR=""
DEBUG_MODE="0";

while getopts "h:l:c:d:" arg
do
	case $arg in
	    l) LOG_BASE_DIR=$OPTARG;;
		c) CONFIG_DIR=$OPTARG;;
		d) DEBUG_MODE=$OPTARG;;
		h) usage;;
		?) usage;;
	esac
done

echo Baidu.com,Inc.                                  
echo 'Copyright (c) 2000-2013 All Rights Reserved.'                                                                      
echo Distributed 
echo https://github.com/brucexx/heisenberg
echo brucest0078@gmail.com

#set JAVA_HOME
#JAVA_HOME=/home/work/soft/java

#check JAVA_HOME & java
noJavaHome=false
if [ -z "$JAVA_HOME" ] ; then
    noJavaHome=true
fi
if [ ! -e "$JAVA_HOME/bin/java" ] ; then
    noJavaHome=true
fi
if $noJavaHome ; then
    echo
    echo "Error: JAVA_HOME environment variable is not set."
    echo
    exit 1
fi
#==============================================================================

#set JAVA_OPTS
JAVA_OPTS="-server -Xms1024m -Xmx1024m -Xmn256m -Xss256k"
#performance Options
JAVA_OPTS="$JAVA_OPTS -XX:+AggressiveOpts"
JAVA_OPTS="$JAVA_OPTS -XX:+UseBiasedLocking"
JAVA_OPTS="$JAVA_OPTS -XX:+UseFastAccessorMethods"
JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC"
JAVA_OPTS="$JAVA_OPTS -XX:+CMSParallelRemarkEnabled"
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSCompactAtFullCollection"
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS="$JAVA_OPTS -XX:CMSInitiatingOccupancyFraction=75"
#GC Log Options
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
#debug Options
if [ "$DEBUG_MODE"x == "1"x ] ; then
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8065,server=y,suspend=n"
fi 
#==============================================================================

#set HOME
CURR_DIR=`pwd`
cd `dirname "$0"`/..


HSB_HOME=`pwd`
if [ ! -z "$LOG_BASE_DIR" ] ; then 
  if [ ! -d $LOG_BASE_DIR/logs ] ; then
     mkdir -p $LOG_BASE_DIR/logs
  fi
fi
cd $CURR_DIR
if [ -z "$HSB_HOME" ] ; then
    echo
    echo "Error: HSB_HOME environment variable is not defined correctly."
    echo
    exit 1
fi
#=============================================================================

#set CLASSPATH
HSB_CLASSPATH="$HSB_HOME/conf:$HSB_HOME/lib/classes"
for i in "$HSB_HOME"/lib/*.jar
do
    HSB_CLASSPATH="$HSB_CLASSPATH:$i"
done
#==============================================================================

#startup Server
RUN_CMD="\"$JAVA_HOME/bin/java\""
RUN_CMD="$RUN_CMD -Dhsb.home=\"$HSB_HOME\""
if [ ! -z "$LOG_BASE_DIR" ] ; then 
  RUN_CMD="$RUN_CMD -Dhsb.log.home=\"$LOG_BASE_DIR\""
fi

RUN_CMD="$RUN_CMD -classpath \"$HSB_CLASSPATH\""
RUN_CMD="$RUN_CMD $JAVA_OPTS"
RUN_CMD="$RUN_CMD com.baidu.hsb.HeisenbergStartup "
if [ ! -z "$CONFIG_DIR" ] ; then 
  RUN_CMD="$RUN_CMD -conf $CONFIG_DIR"
fi
RUN_CMD="$RUN_CMD $@"
RUN_CMD="$RUN_CMD >> \"$HSB_HOME/logs/console.log\" 2>&1 &"
echo $RUN_CMD
eval $RUN_CMD
#==============================================================================
