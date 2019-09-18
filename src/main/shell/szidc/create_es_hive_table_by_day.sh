#!/usr/bin/env bash
cd "$(dirname ${0})"
BIN_HOME=$(pwd)
APP_HOME=$(cd ${BIN_HOME}/..; pwd)
SQL_HOME=$(cd ${APP_HOME}/sql; pwd)
LOG_HOME=$(cd ${APP_HOME}/logs; pwd)
echo "BIN_HOME->${BIN_HOME}"
echo "APP_HOME->${APP_HOME}"
echo "SQL_HOME->${SQL_HOME}"
echo "LOG_HOME->${LOG_HOME}"
fields="cycle_time              string,
	   dura                    double,
	   source_ip               string,
	   destination_ip          string,
	   prtcl_id                string,
	   source_port             int,
	   destination_port        int,
	   down_pkts               bigint,
	   down_flux               bigint,
	   down_flux_speed         double,
	   up_pkts                 bigint,
	   up_flux                 bigint,
	   up_flux_speed           double,
	   prov_id                 int,
	   opt_id                  int,
	   busi_id                 int,
	   data_center_id          string,
	   router_ip               string,
	   in_port_num             string,
	   out_port_num            string,
	   in_port_encoded         string,
	   out_port_encoded        string,
	   application_prtcl_name  string,
	   source_as               int,
	   dest_as                 int,
	   next_ip                 string,
	   bpp                     string"
es_nodes="192.168.2.231:9200,192.168.2.232:9200,192.168.2.233:9200"
es_date=$(date -d "+1 day" +"%Y%m%d")
es_resource="tc_netflow_all_${es_date}"
sql="create external table tc_netflow_all_es_${es_date} ( ${fields} ) \
stored by 'org.elasticsearch.hadoop.hive.EsStorageHandler' tblproperties('es.resource'='${es_resource}','es.nodes'='${es_nodes}','es.index.auto.create'='true');"
echo ${sql} >> ${SQL_HOME}/create_es_hive_table_${es_nodes}.sql
/opt/cloudera/parcels/CDH-6.0.1-1.cdh6.0.1.p0.590678/bin/hive "$@"  --hiveconf mapreduce.job.queuename=root.hive_queue -e "${sql}" >> ${LOG_HOME}/create_es_hive_table_at_${es_date}.log 2>&1 &