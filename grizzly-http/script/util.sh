#!/bin/bash

function is_process_running {
  local  pid=$1
  kill -0 $pid > /dev/null 2>&1
  local  status=$?
  return $status
}

function kill_process_with_retry {
   local pid="$1"
   local maxattempt="$2"
   local pname="$3"
   local sleeptime=5

   if ! is_process_running $pid ; then
     echo "ERROR: process name ${pname} with pid: ${pid} not found"
     exit 1
   fi

   for try in $(seq 1 $maxattempt); do
      echo "Killing $pname. [pid: $pid], attempt: $try"
      kill ${pid}
      sleep 5
      if is_process_running $pid; then
        echo "$pname is not shutdown [pid: $pid]"
        echo "sleeping for $sleeptime seconds before retry"
        sleep $sleeptime
      else
        echo "shutdown succeeded"
        return 0
      fi
   done

   echo "Error: unable to killed process for $maxattempt attempt(s), killing the process with -9"
   kill -9 $pid
   sleep $sleeptime

   if is_process_running $pid; then
      echo "$pname is not shutdown even after killed -9 [pid: $pid]"
      return 1
   else
    echo "shutdown succeeded with killed -9"
    return 0
   fi
}