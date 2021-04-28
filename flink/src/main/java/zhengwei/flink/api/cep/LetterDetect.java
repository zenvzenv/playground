package zhengwei.flink.api.cep;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

public class LetterDetect {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStreamSource<String> source = env.socketTextStream("localhost", 8888);

        final Pattern<String, String> pattern = Pattern
                .<String>begin("first")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) throws Exception {
                        return s.contains("a");
                    }
                })
                .next("second")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) throws Exception {
                        return s.contains("b");
                    }
                })
                .next("third")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) throws Exception {
                        return s.contains("c");
                    }
                });

        final PatternStream<String> patternStream = CEP.pattern(source, pattern);
        patternStream.select(new PatternSelectFunction<String, String>() {
            @Override
            public String select(Map<String, List<String>> map) throws Exception {
                final List<String> first = map.get("first");
                final List<String> second = map.get("second");
                final List<String> third = map.get("third");
                return "first->" + first.size() + ",second->" + second.size() + ",third->" + third.size();
            }
        }).print();
    }
}
