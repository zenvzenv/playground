#!/usr/bin/env bash
#杀掉Java进程
if [[ $# -lt 1 ]]; then
    echo "Enter your app name"
    showUsage
    exit -1
fi
#get app name
APP_NAME=$1
processId=$(ps -ef | grep "java" | grep -v "grep" | grep "${APP_NAME}" | awk '{print $2}')
if [[ -n ${processId} ]]; then
    echo "The app process id  is "${processId}
    kill -9 ${processId}
    echo "killed process id success."
else
    echo "There is no process id."
fi
function showUsage() {
    echo "If you don not know hwo to use this shell."
    echo "There has a example."
    echo "for example:"
    echo "./kill_java_process.sh yourAppName"
}