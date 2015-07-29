#!/usr/bin/env bash
###
# Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
# 
# Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
# the European Commission - subsequent versions of the EUPL (the "Licence");
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
#   http://ec.europa.eu/idabc/eupl
# 
# Unless required by applicable law or agreed to in writing, software 
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and 
# limitations under the Licence.
# 
# This product combines work with different licenses. See the "NOTICE" text
# file for details on the various modules and licenses.
# The "NOTICE" text file is part of the distribution. Any derivative works
# that you distribute must include a readable copy of the "NOTICE" text file.
###

# Author: Erik Torres <ertorser[AT]upv.es>
# Url:    https://github.com/etorres
# Date:   07/28/2015

###
# Usage examples:
#
# 1) start/stop the LeishVL <microservice>:
#  
#    lvl-<microservice> start/stop
#
# 2) check the status of the service:
#
#    lvl-<microservice> status
#
# 3) do not execute operation, just print the command:
#
#    RUN_JAVA_DRY=1 lvl-<microservice> start
###

# stop on errors
set -e

SCRIPT_NAME=$(basename $0)

# resolve to absolute path where this script run
if [ ! -h $0 ]; then
  SCRIPT_DIR=$(cd $(dirname $0) && pwd)
else
  SCRIPT_DIR=$(cd $(dirname $(readlink $0)) && pwd)
fi

APP_SUFIX=drive
APP_VERSION=0.3.0

APP_NAME=lvl-${APP_SUFIX}
APP_DIR=$(dirname $SCRIPT_DIR)
APP_CONFIG=${APP_DIR}/etc
APP_JARFILE=${APP_DIR}/lib/leishvl-${APP_VERSION}/${APP_NAME}-${APP_VERSION}-uber.jar
APP_DAEMON_CLASS="eu.eubrazilcc.lvl.${APP_SUFIX}.AppDaemon"
APP_WORKING_DIR=${APP_DIR}/var/run/${APP_NAME}/
APP_OUT_FILE=${APP_WORKING_DIR}/${APP_NAME}.out
APP_ERR_FILE=${APP_WORKING_DIR}/${APP_NAME}.err
APP_PID_FILE=${APP_WORKING_DIR}/${APP_NAME}.pid

VERTX_JARFILES="${APP_DIR}/lib/vert.x-3.0.0/lib/*"

JSVC_EXEC="/usr/bin/jsvc"

if [[ ! -e "${APP_DIR}" || ! -d "${APP_DIR}" ]] ; then
  echo "Base directory $APP_DIR does not exist or is not a directory. Exiting..."
  exit 1
fi

# resolve Java home
_JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
JAVA_HOME=${JAVA_HOME:=$_JAVA_HOME}

# resolve Java command
JAVA_CMD=java
if [ -d "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

# detect Java version
if type -p "$JAVA_CMD" 1>/dev/null 2>&1; then
  JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  if [[ "$JAVA_VERSION" < "1.7" ]]; then
    echo "Java version is less than 1.7. Exiting..."
    exit 1
  fi
else
  echo "No Java installation found. Exiting..."
  exit 1
fi

# check jsvc
if ! type -p "$JSVC_EXEC" 1>/dev/null 2>&1; then
  echo "No jsvc installation found. Exiting..."
  exit 1
fi

start_exec()
{  
  # compute maximum available memory as: pow(2, ceil(log(x)/log(2)))
  MAX_MEMORY=$(echo "2^($(grep MemTotal /proc/meminfo | echo "l(`awk '{print $2}'`*0.75/1024)/l(2)" | bc -l) / 1)" | bc)

  # JVM optimization
  JVM_OPTIMIZATION="-XX:+UseNUMA -XX:+UseParallelGC"

  # default parameters
  RUN_JAVA_OPTS="-Xmx${MAX_MEMORY}m -Xss1024k $JVM_OPTIMIZATION"
  RUN_JAVA_DRY=${RUN_JAVA_DRY:=} # Do not execute when is not empty, but just print

  RUN_JAVA_OPTS="$RUN_JAVA_OPTS -Dlogback.configurationFile=${APP_CONFIG}/logback.xml"
  APP_RUN_ARGS="-c ${APP_CONFIG}/application.conf"

  # display full Java command
  if [ -n "$RUN_JAVA_DRY" ]; then
    echo "$JSVC_EXEC -jvm server -cp $VERTX_JARFILES:$APP_JARFILE -home $JAVA_HOME -cwd ${APP_DIR}/var/run/${APP_NAME}/ -outfile $APP_OUT_FILE -errfile $APP_ERR_FILE -pidfile $APP_PID_FILE $RUN_JAVA_OPTS $APP_DAEMON_CLASS $APP_RUN_ARGS"
  fi

  # run with jsvc
  if [ -z "$RUN_JAVA_DRY" ]; then
    mkdir -p ${APP_WORKING_DIR} ;
    if [ ! -d ${APP_WORKING_DIR} ]; then
        echo "error in working directory creation: ${APP_WORKING_DIR}" ;
        exit 2 ;
    fi
    $JSVC_EXEC -jvm server -cp $VERTX_JARFILES:$APP_JARFILE -home $JAVA_HOME -cwd ${APP_DIR}/var/run/${APP_NAME}/ -outfile $APP_OUT_FILE -errfile $APP_ERR_FILE -pidfile $APP_PID_FILE $RUN_JAVA_OPTS $APP_DAEMON_CLASS $APP_RUN_ARGS
  fi
}

stop_exec()
{
  $JSVC_EXEC -jvm server -cp $VERTX_JARFILES:$APP_JARFILE -home $JAVA_HOME -cwd ${APP_WORKING_DIR} -outfile $APP_OUT_FILE -errfile $APP_ERR_FILE -pidfile $APP_PID_FILE -stop $APP_DAEMON_CLASS  
}

status_exec()
{
  if [ -e $APP_PIDFILE ] && [ -f $APP_PIDFILE ]; then
    PID=$(cat $APP_PIDFILE)
    if [ -e /proc/${PID} -a /proc/${PID}/exe -ef $JSVC_EXEC ] && [ -n "`grep ${APP_NAME} /proc/${PID}/cmdline`" ] ; then
      echo "$APP_NAME is running"
    else
      echo "$APP_NAME is stopped"
    fi
  else
    if [ -n "`ps ax | grep ${APP_NAME} | grep -v grep`" ]; then
      echo "$APP_NAME is stopped"
    else
      echo "$APP_NAME is running, but $APP_PIDFILE is unavailable"
    fi
  fi
}

case "$1" in
  start)
        start_exec
        ;;
  stop)
        stop_exec
        ;;
  status)
        status_exec
        ;;
  *)
        echo "Usage: $SCRIPT_DIR/$SCRIPT_NAME {start|stop|status}" >&2
        exit 3
        ;;
esac
