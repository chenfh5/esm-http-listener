#!/usr/bin/env bash

maxattempt=3
pid=""
pname="esm-http-listener"

function env_build(){
    date
    echo "start to stop"
    cd `dirname $0`
    pwd=`pwd`
    echo pwd=$pwd
    log=$pwd/log
    source "$pwd/util.sh"
    pid=`cat $pwd/pidfile.pid`
}

function main(){
    env_build
    echo maxattempt=${maxattempt}
    echo pid=${pid}
    echo log=${log}
    kill_process_with_retry "${pid}" "${maxattempt}" "${pname}"

    if [[ $? == 0 ]]; then
      rm -f $pwd/pidfile.pid
      exit 0
    else
      exit 1
    fi

    echo "end to stop at:" `date`
}

main
