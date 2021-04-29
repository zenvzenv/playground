package zhengwei.flink.api.cep;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class LetterDetect {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<String> source = env.socketTextStream("localhost", 8888)
                //因为 Flink 1.12+ 版本默认的处理时间语义是事件时间，需要加上水位线来推动时间前进
                .assignTimestampsAndWatermarks(WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner((String s, long ts) -> System.currentTimeMillis()));

        source.print();
        final Pattern<String, String> pattern = Pattern
                .<String>begin("first")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) {
                        return s.equals("a");
                    }
                })
                .next("second")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) {
                        return s.contains("b");
                    }
                })
                .next("third")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) {
                        return s.contains("c");
                    }
                });

        final PatternStream<String> patternStream = CEP.pattern(source, pattern);
        patternStream.select(new PatternSelectFunction<String, String>() {
            @Override
            public String select(Map<String, List<String>> map) {
                final List<String> first = map.get("first");
                final List<String> second = map.get("second");
                final List<String> third = map.get("third");
                return "first->" + first.size() + ",second->" + second.size() + ",third->" + third.size();
            }
        }).print("result >>>");

        env.execute();
    }
}
