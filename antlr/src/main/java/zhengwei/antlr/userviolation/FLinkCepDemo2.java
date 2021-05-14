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
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/13 9:28
 */
public class FLinkCepDemo2 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<String> input = env.socketTextStream("localhost", 8888)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<String>forMonotonousTimestamps()
                                .withTimestampAssigner((String e, long ts) -> System.currentTimeMillis())
                );

        final Pattern<String, String> pattern = Pattern
                .<String>begin("a", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) {
                        return s.equals("a");
                    }
                })
                .or(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) {
                        return s.equals("b");
                    }
                })
                .times(3)
                .consecutive()
                .within(Time.of(1, TimeUnit.SECONDS));

        final PatternStream<String> patternStream = CEP.pattern(input, pattern);
        patternStream.select((PatternSelectFunction<String, Object>) pattern1 -> pattern1).print();
        env.execute();
    }
}
