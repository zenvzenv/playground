package zhengwei.antlr.userviolation;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/6/10 18:26
 */
public class Run3 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<String> source = env.socketTextStream("localhost", 8888)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<String>forMonotonousTimestamps()
                                .withTimestampAssigner((String s, long ts) -> System.currentTimeMillis())
                )
                .keyBy(s -> s);
        source.print("source ");
        final Pattern<String, String> pattern = Pattern.<String>begin("begin", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) {
                        return true;
                    }
                }).times(3);
        final PatternStream<String> patternStream = CEP.pattern(source, pattern);
        patternStream.select(new PatternSelectFunction<String, Object>() {
            @Override
            public Object select(Map<String, List<String>> pattern) {
                return pattern.get("begin");
            }
        }).print("result ");
        env.execute();
    }
}
