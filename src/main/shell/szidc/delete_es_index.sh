#!/usr/bin/env bash
delay=-1
index_date=$(date -d "${delay} day" +"%Y%m%d")
index_name="tc_netflow_all_${index_date}"
es_node="192.168.152.116:9200"
curl -X DELETE "${es_node}/${index_name}"