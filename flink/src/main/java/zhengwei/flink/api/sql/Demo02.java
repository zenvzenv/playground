package zhengwei.flink.api.sql;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import zhengwei.flink.api.sql.pojo.WordCount;

import java.util.Arrays;

import static org.apache.flink.table.api.Expressions.$;

/**
 * Table 和 SQL 方式来实现 WordCount
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/14 12:10
 */
public class Demo02 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        final StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env, settings);

        final DataStreamSource<WordCount> source = env.fromCollection(Arrays.asList(
                new WordCount("zw", 1),
                new WordCount("zw", 5),
                new WordCount("zw", 2),
                new WordCount("flink", 2),
                new WordCount("flink", 2),
                new WordCount("spark", 2),
                new WordCount("hadoop", 3),
                new WordCount("hive", 2)
        ));

        //1.sql
        tabEnv.createTemporaryView("wcTab", source, $("word"), $("frequency"));
        //别名需要和实体类的名称一致，否则会报错
        final String sql = "select word,sum(frequency) as frequency from wcTab group by word";
        final Table resultTab1 = tabEnv.sqlQuery(sql);
        final DataStream<Tuple2<Boolean, WordCount>> resultDS1 = tabEnv.toRetractStream(resultTab1, WordCount.class);
        resultDS1.print("sql result");


        //2.table
        final Table wcTab = tabEnv.fromDataStream(source, $("word"), $("frequency"));
        final Table resultTab2 = wcTab
                .groupBy($("word"))
                .select($("word"), $("frequency").sum().as("frequency"))
                .filter($("frequency").isGreaterOrEqual(2));
        final DataStream<Tuple2<Boolean, Row>> resultDS2 = tabEnv.toRetractStream(resultTab2, Row.class);
        resultDS2.print("table result");

        env.execute();
    }
}
