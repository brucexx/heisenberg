@echo off
echo Baidu.com,Inc.                                  
echo Copyright (c) 2000-2013 All Rights Reserved.                                                                         
echo Distributed 
echo https://github.com/brucexx/heisenberg
echo brucest0078@gmail.com

REM check JAVA_HOME & java
if "%JAVA_HOME%" == "" goto noJavaHome
if exist "%JAVA_HOME%\bin\java.exe" goto mainEntry
:noJavaHome
echo ---------------------------------------------------
echo Error: JAVA_HOME environment variable is not set. 
echo ---------------------------------------------------
goto end

:mainEntry
REM set JAVA_OPTS
set "JAVA_OPTS=-server -Xms1024m -Xmx1024m -Xmn256m -Xss256k"
REM performance Options
set "JAVA_OPTS=%JAVA_OPTS% -XX:+AggressiveOpts"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseBiasedLocking"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseFastAccessorMethods"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+DisableExplicitGC"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseParNewGC"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseConcMarkSweepGC"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+CMSParallelRemarkEnabled"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseCMSCompactAtFullCollection"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseCMSInitiatingOccupancyOnly"
set "JAVA_OPTS=%JAVA_OPTS% -XX:CMSInitiatingOccupancyFraction=75"
REM GC Log Options
REM set "JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCApplicationStoppedTime"
REM set "JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCTimeStamps"
REM set "JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCDetails"
REM debug Options
REM set "JAVA_OPTS=%JAVA_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,address=8065,server=y,suspend=n"

REM set HOME_DIR
set "CURR_DIR=%cd%"
cd ..
set "HSB_HOME=%cd%"
cd %CURR_DIR%
if exist "%HSB_HOME%\bin\startup.bat" goto okHome
echo ---------------------------------------------------
echo Error: HSB_HOME environment variable is not defined correctly.
echo ---------------------------------------------------
goto end

:okHome
set "APP_VERSION=1.0"

REM set HSB_CLASSPATH
setlocal enabledelayedexpansion
set "HSB_CLASSPATH=%HSB_HOME%\conf;%HSB_HOME%\lib\classes"
set "HSB_CLASSPATH=%HSB_CLASSPATH%;%HSB_HOME%\lib\heisenberg-server-%APP_VERSION%.jar"
set "HSB_CLASSPATH=%HSB_CLASSPATH%;%HSB_HOME%\lib\log4j-1.2.16.jar"
 FOR %%I IN ("%HSB_HOME%\lib\*.jar") DO (
   set "HSB_CLASSPATH=!HSB_CLASSPATH!;%%I"
)
echo "HSB_CLASSPATH=%HSB_CLASSPATH%"

REM startup Server
set "RUN_CMD="%JAVA_HOME%\bin\java.exe""
set "RUN_CMD=%RUN_CMD% -Dhsb.home="%HSB_HOME%""
set "RUN_CMD=%RUN_CMD% -classpath "%HSB_CLASSPATH%""
set "RUN_CMD=%RUN_CMD% %JAVA_OPTS%"
set "RUN_CMD=%RUN_CMD% com.baidu.hsb.HeisenbergStartup"
call %RUN_CMD%

:end
