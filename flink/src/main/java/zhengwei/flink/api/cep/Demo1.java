package zhengwei.flink.api.cep;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/13 13:20
 */
public class Demo1 {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<String> input = env.socketTextStream("localhost", 8888)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<String>forMonotonousTimestamps()
                                .withTimestampAssigner((String s, long ts) -> System.currentTimeMillis())
                );

        final Pattern<String, String> a = Pattern.<String>begin("a")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String value) {
                        return value.equals("a");
                    }
                });

        final Pattern<String, String> b = Pattern.<String>begin("b")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {
                        return value.equals("b");
                    }
                });


    }
}
