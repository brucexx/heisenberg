#!/bin/sh
echo Baidu.com,Inc.                                  
echo Copyright '(c)' 2000-2013 All Rights Reserved.                                                                         
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

#stop Server
$JAVA_HOME/bin/jps |grep HeisenbergStartup|awk -F ' ' '{print $1}'|while read line
do
  eval "kill -9 $line"
done
#==============================================================================

#sleep sometime
sleep 1

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
. $HSB_HOME/bin/startup.sh
#==============================================================================