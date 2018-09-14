#!/usr/bin/env bash

host=$1
port=$2
log=""
pidfile=""

function env_build(){
    date
    echo "start to run"
    cd `dirname $0`
    pwd=`pwd`
    echo pwd=$pwd
    log=$pwd/log
    pidfile=$pwd/pidfile.pid
    source "$pwd/util.sh"
}

function check_pid_running {
    if [[ -f "$pidfile" ]] ; then
      pid=`cat ${pidfile}`
      if is_process_running $pid ; then
        echo "esm-http-listener server already running [pid: $pid]. Aborting"
        exit 1
      fi
    fi
}

function main(){
    env_build
    echo host=${host}
    echo port=${port}
    echo log=${log}
    echo pidfile=${pidfile}
    check_pid_running

    # executor
    OPTS="-Xms1G -Xmx2G"
    java $OPTS -cp io.github.chenfh5.server.ShellServer > $log/esm-http-listener.out 2>&1 &
    echo $! > $pidfile
    echo "begin to run at:" `date`
}

main
