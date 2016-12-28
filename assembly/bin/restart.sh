#!/bin/sh

usage() {
	echo "Usage:"
	echo "    restartup.sh [-c config_folder] [-l log_folder] [-d debug mode] [-h]"
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
#$JAVA_HOME/bin/jps |grep "$CONFIG_DIR" |  grep HeisenbergStartup|awk -F ' ' '{print $1}'|while read line
#do
#  eval "kill -9 $line"
#done

#set HSB_HOME
HSB_HOME=`pwd`

if [ -z "$CONFIG_DIR" ] ; then
#default in home/conf
  CONFIG_DIR="$HSB_HOME/conf"
fi


if [ ! -d "$CONFIG_DIR" ] ; then
  echo "\033[31m config_folder must be specified! \033[0m"
  exit 1
fi

if [ -f "$CONFIG_DIR/pid" ] ; then
    PID=`sed -n "1,1p" $CONFIG_DIR/pid`
    OLD_CONFIG_DIR=`sed -n "2,1p" $CONFIG_DIR/pid`
    OLD_LOG_DIR=`sed -n "3,1p" $CONFIG_DIR/pid`
    if [ -z "$PID" ] ; then
      echo "\033[32m PID not extis,no process should be shutdown! \033[0m"
    else
		if [ -z "$LOG_BASE_DIR" ] ; then
		LOG_BASE_DIR="$OLD_LOG_DIR"
		fi
        echo "\033[32m PID[$PID] will be shutdown!origin config_dir[$OLD_CONFIG_DIR],origin log_dir[$OLD_LOG_DIR] \033[0m"
        kill -9 "$PID"
    fi
fi

if [ -z "$LOG_BASE_DIR" ] ; then
LOG_BASE_DIR="$HSB_HOME/logs"
fi

#==============================================================================
echo " sleep listen port to shutdown"
#sleep sometime
sleep 2
echo  "sleep over.."

CURR_DIR=`pwd`
cd `dirname "$0"`/..

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
if [ ! -z "$LOG_BASE_DIR" ] ; then
RUN_CMD=" $RUN_CMD -l $LOG_BASE_DIR"
fi
if [ ! -z "$CONFIG_DIR" ] ; then
RUN_CMD=" $RUN_CMD -c $CONFIG_DIR"
fi
RUN_CMD=" $RUN_CMD -d $DEBUG_MODE";
echo $RUN_CMD
eval $RUN_CMD
#==============================================================================
