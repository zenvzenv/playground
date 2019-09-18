#!/usr/bin/env bash
cd "$(dirname ${0})"
BIN_HOME=$(pwd)
APP_HOME=$(cd ${BIN_HOME}/..; pwd)
SQL_HOME=$(cd ${APP_HOME}/sql; pwd)
LOG_HOME=$(cd ${APP_HOME}/logs; pwd)
CONF_HOME=$(cd ${APP_HOME}/conf; pwd)
echo "BIN_HOME->${BIN_HOME}"
echo "APP_HOME->${APP_HOME}"
echo "SQL_HOME->${SQL_HOME}"
echo "LOG_HOME->${LOG_HOME}"
echo "CONF_HOME->${CONF_HOME}"
es_node="192.168.152.116:9200"
index_name="tc_netflow_all_"$(date -d "1 day" +"%Y%m%d")
echo "index_name -> ${index_name}"
index_schema=$(cat ${CONF_HOME}/index_schema.json)
echo "index_schema -> ${index_schema}"
curl -X PUT "${es_node}/${index_name}" -H "Content-Type:application/json" -d''${index_schema}''