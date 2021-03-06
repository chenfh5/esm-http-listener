#!/usr/bin/env bash

host=$1
port=$2
log=""
pidfile=""
pwd=""
esm_bin_dir=""

function env_build(){
    date
    echo "start to run"
    cd `dirname $0`
    pwd=`pwd`
    echo pwd=$pwd
    log=$pwd/log
    pidfile=$pwd/pidfile.pid
    esm_bin_dir=$pwd/../esmdir
    mkdir $esm_bin_dir/log
    mkdir $esm_bin_dir/shell
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
    jarFile=$pwd/../lib/grizzly-http.jar

    echo OPTS=${OPTS}
    echo jarFile=${jarFile}

    java $OPTS -cp $jarFile io.github.chenfh5.server.ShellServer --host=$host --port=$port --esm=$esm_bin_dir > esm-http-listener.out 2>&1 &
    echo $! > $pidfile

    echo "begin to run success at:" `date`
}

main
ps aux | grep "io.github.chenfh5.server.ShellServer" | grep -v "grep"
echo "tailfing, 'Ctrl-C' to break"
tailf esm-http-listener.out