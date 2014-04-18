#!/bin/sh
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

#set JAVA_OPTS
JAVA_OPTS="-Xss256k"
#==============================================================================

#stop Server
$JAVA_HOME/bin/jps |grep HeisenbergStartup|awk -F ' ' '{print $1}'|while read line
do
  eval "kill -9 $line"
done
#==============================================================================

#set HOME
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

#set CLASSPATH
HSB_CLASSPATH="$HSB_HOME/conf:$HSB_HOME/lib/classes"
for i in "$HSB_HOME"/lib/*.jar
do
    HSB_CLASSPATH="$HSB_CLASSPATH:$i"
done
#==============================================================================

#shutdown Server
RUN_CMD="\"$JAVA_HOME/bin/java\""
RUN_CMD="$RUN_CMD -Dhsb.home=\"$HSB_HOME\""
RUN_CMD="$RUN_CMD -classpath \"$HSB_CLASSPATH\""
RUN_CMD="$RUN_CMD $JAVA_OPTS"
RUN_CMD="$RUN_CMD com.baidu.hsb.HeisenbergShutdown $@"
RUN_CMD="$RUN_CMD >> \"$HSB_HOME/logs/console.log\" 2>&1 &"
echo $RUN_CMD
eval $RUN_CMD
#==============================================================================
