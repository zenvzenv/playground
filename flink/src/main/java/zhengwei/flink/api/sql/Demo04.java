package zhengwei.flink.api.sql;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

/**
 * 从 kafka 读入经过 ETL 之后输出到 kafka
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 16:48
 */
public class Demo04 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env);
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        //--bootstrap localhost:9092
        final String bootstrap = parameterTool.get("bootstrap");
        //--inputTopic input
        final String inputTopic = parameterTool.get("inputTopic");
        //--outputTopic output
        final String outputTopic = parameterTool.get("outputTopic");
        //--groupId test
        final String groupId = parameterTool.get("groupId");

        //直接连接 kafka，从 kafka 中读取数据
        final TableResult inputTable = tabEnv.executeSql(
                "create table input_kafka (\n" +
                        "  `user_id` BIGINT,\n" +
                        "  `page_id` BIGINT,\n" +
                        "  `status` STRING\n" +
                        ") with (\n" +
                        "  'connector' = 'kafka',\n" +
                        "  'topic' = '" + inputTopic + "',\n" +
                        "  'properties.bootstrap.servers' = '" + bootstrap + "',\n" +
                        "  'properties.group.id' = ' " + groupId + " ',\n" +
                        "  'scan.startup.mode' = 'latest-offset',\n" +
                        "  'format' = 'json'\n" +
                        ")"
        );

        //直接连接 kafka，写入到 kafka
        final TableResult outputTable = tabEnv.executeSql(
                "create table output_kafka (\n" +
                        "  `user_id` BIGINT,\n" +
                        "  `page_id` BIGINT,\n" +
                        "  `status` STRING\n" +
                        ") with (\n" +
                        "  'connector' = 'kafka',\n" +
                        "  'topic' = '" + outputTopic + "',\n" +
                        "  'properties.bootstrap.servers' = '" + bootstrap + "',\n" +
                        "  'format' = 'json',\n" +
                        "  'sink.partitioner' = 'round-robin'\n" +
                        ")"

        );

        final String sql = "select user_id,page_id,status from input_kafka where status = 'success'";

        final Table resultTable = tabEnv.sqlQuery(sql);

        final DataStream<Tuple2<Boolean, Row>> resultDS = tabEnv.toRetractStream(resultTable, Row.class);
        resultDS.print();

        tabEnv.executeSql("insert into output_kafka select * from " + resultTable);

        env.execute();
    }
}
