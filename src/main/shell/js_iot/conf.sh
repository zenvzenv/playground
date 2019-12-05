#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
APP_HOME=$(cd ${BIN_HOME}/..; pwd)
CONF_HOME=$(cd ${APP_HOME}/conf; pwd)
SQL_HOME=$(cd ${APP_HOME}/sql; pwd)
DATA_HOME=$(cd ${APP_HOME}/data; pwd)
LOGS_HOME=$(cd ${APP_HOME}/logs; pwd)
SCP_IP="172.16.65.116"
SCP_USER="sybiq"
SCP_PASSWORD="asiainfo@2019"
SCP_PORT="22"
SCP_SOURCE_FILE_PATH_PREFIX="/data01/home/asiainfo/data/dataimport/cp/statcenter"
SCP_TARGET_FILE_PATH_PREFIX="~/project/data"
SCP_FILE_NAME_KEY_WORD="*"