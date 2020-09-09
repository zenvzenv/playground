#!/usr/bin/env bash
#springboot整合Spark的启动脚本
if [[ $# -lt 4 ]]; then
    echo "params lt 4"
    echo "usage: $0 -t 202008200000 -p [dev|prod|test]"
    exit
fi
params=$(getopt -o t:p: -l cycleTime:profile: -n "$0" -- "$@")
echo "${params}"
eval set -- "${params}"
while true; do
    case "$1" in
        -t|--cycleTime)
            enhanceTraceDate=$2
            shift 2
            ;;
        -p|--profile)
            activeSpringProfile=$2
            if [[ ${activeSpringProfile} == "test" || ${activeSpringProfile} == "dev" ]]; then
                extraSparkOpts="--queue root.users.test "
            elif [[ ${activeSpringProfile} == "prod" ]]; then
                extraSparkOpts="--queue sansuo \
                                --keytab /home/linkage/tools/hadoopclient/key/user.keytab \
                                --principal 440000_22_wlk_gass@HADOOP.COM "
            fi
            shift 2
            ;;
        --)
            shift
            break
            ;;
    esac
done
cd "$(dirname $0)"
BIN_HOME="$(pwd)"
JAR_HOME="$(cd ${BIN_HOME}/../jars; pwd)"
LIB_HOME="$(cd ${BIN_HOME}/../lib; pwd)"
CONFIG_HOME="$(cd ${BIN_HOME}/../config; pwd)"
MAIN_CLASS="com.ai.c3.trace.TraceApplication"
echo "enhanceTraceDate -> ${enhanceTraceDate}"

#dirver_opts
DRIVER_OPTS="-DcycleTime=${enhanceTraceDate} \
-Dspring.profiles.active=${activeSpringProfile} \
-Dspring.config.location=${CONFIG_HOME}/application-${activeSpringProfile}.properties,${CONFIG_HOME}/application.properties \
-Dlog4j.configuration=file:${CONFIG_HOME}/log4j.properties \
"

#spark_opts
SPARK_OPTS="--master yarn \
--deploy-mode client \
--num-executors 1 \
--executor-cores 1 \
--executor-memory 1G \
${extraSparkOpts} \
--conf spark.dynamicAllocation.enabled=false \
--conf spark.hadoop.validateOutputSpecs=false \
--conf spark.worker.cleanup.enabled=true \
--conf spark.serializer=org.apache.spark.serializer.KryoSerializer
"
# --jars
DEP_JARS="$(find "${LIB_HOME}" -name "*.jar" | xargs echo | tr ' ' ',')"

#submit
spark-submit \
--class "${MAIN_CLASS}" \
--driver-java-options "${DRIVER_OPTS}" \
--jars "${DEP_JARS}" \
${SPARK_OPTS} \
"${JAR_HOME}/ais-c3-enhance-trace-0.0.1-SNAPSHOT.jar"
