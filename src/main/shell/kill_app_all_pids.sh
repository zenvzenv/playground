#!/usr/bin/env bash
if [[ $# -lt 1 ]]; then
    showUsage
fi
#杀掉一个应用的所有进程号
cd $(dirname $0)
BIN_HOME=$(pwd)
APP_HOME=$(cd ${BIN_HOME}/..;pwd)
echo "BIN_HOME->"${BIN_HOME}
echo "APP_HOME ->"${APP_HOME}
your_app_name=${1}
your_yarn_app_name=${2}
#获得app的所有进程号并杀掉
pids=$(ps -ef | grep ${your_app_name} | grep -v grep | awk '{print $2}')
for pid in ${pids} ; do
    echo "app pid ->"${pid}
    kill -15 ${pid}
    sleep 2
done
if [[ -n ${your_yarn_app_name} ]]; then
    yarn_pids=$(yarn application -list | grep ${your_yarn_app_name})
    for pid in ${yarn_pids} ; do
        echo "yarn app pid ->"${pid}
        yarn application -kill ${pid}
        sleep 2
    done

fi
function showUsage() {
    echo "This Shell accept at lease one param"
    echo "<Usage>:"
    echo "kill_app_all_pids <your_app_name>"
    echo "or"
    echo "kill_app_all_pids <your_app_name> <your_yarn_app_name>"
}