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
echo Copyright '(c)' 2000-2013 All Rights Reserved.                                                                         
echo Distributed 
echo https://github.com/brucexx/heisenberg
echo brucest0078@gmail.com

SOFT_DIR="${HOME}/soft"
if [ -d ${SOFT_DIR}/java ]; then
	export JAVA_HOME=${SOFT_DIR}/java
fi

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

#stop Server
$JAVA_HOME/bin/jps |grep HeisenbergStartup|awk -F ' ' '{print $1}'|while read line
do
  eval "kill -9 $line"
done
#==============================================================================
echo " sleep listen port to shutdown"
#sleep sometime
sleep 5
echo  "sleep over.."
 
#set HSB_HOME
CURR_DIR=`pwd`
cd `dirname "$0"`/..
HSB_HOME=`pwd`
cd $CURR_DIR
if [ -z "$HSB_HOME" ] ; then
    echo
    echo "Error: HSB_HOME environment variable is not defined correctly."
    echo
    exit 1
fi
#==============================================================================

#startup Server
RUN_CMD=". $HSB_HOME/bin/startup.sh"
if [ ! -z "$LOG_BASE_DIR"] ; then
RUN_CMD=" $RUN_CMD -l $LOG_BASE_DIR"
fi
if [ ! -z "$CONFIG_DIR"] ; then
RUN_CMD=" $RUN_CMD -c $CONFIG_DIR"
fi
RUN_CMD=" $RUN_CMD -d $DEBUG_MODE";
echo $RUN_CMD
eval $RUN_CMD
#==============================================================================
