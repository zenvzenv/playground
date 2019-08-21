#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
APP_HOME=$(cd ${BIN_HOME}/..; pwd)
CONF_HOME=$(cd ${APP_HOME}/conf; pwd)
SQL_HOME=$(cd ${APP_HOME}/sql; pwd)
echo "BIN_HOME==>"${BIN_HOME}
echo "APP_HOME==>"${APP_HOME}
echo "CONF_HOME==>"${CONF_HOME}
echo "SQL_HOME==>"${SQL_HOME}
now_time=$(date +"%Y%m%d%H%M")
delay=-30
table_time=$(date -d "${delay} day" +"%Y/%m/%d/%H/%M")
year=$(echo ${table_time} | awk -F'/' '{print $1}')
mouth=$(echo ${table_time} | awk -F'/' '{print $2}')
day=$(echo ${table_time} | awk -F'/' '{print $3}')
sybase_iq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
sql_file_name=drop_table_${now_time}.sql
# read table name from conf file
while read line
do
    if [[ ${line} == "#"* ]] ; then
        continue
    fi
    echo ${line}
    echo "drop table "${line} >> ${SQL_HOME}/${sql_file_name}
    echo "go" >> ${SQL_HOME}/${sql_file_name}
done < ${CONF_HOME}/drop_table_name.conf
function drop() {
sed -i "s/yyyy/${year}/g" $1
sed -i "s/mm/${mouth}/g" $1
sed -i "s/dd/${day}/g" $1
drop_sql=$(cat $1)
${sybase_iq_db_conn} << EOF
set temporary option conversion_error='off'
go
${drop_sql}
EOF
}
drop ${SQL_HOME}/${sql_file_name}