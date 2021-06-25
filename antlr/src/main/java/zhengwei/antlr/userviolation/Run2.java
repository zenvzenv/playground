package zhengwei.antlr.userviolation;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/6/10 17:30
 */
public class Run2 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        final KeyedStream<Event, String> source = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[ ]");
                    final String userId = fields[0];
                    final String verb = fields[1];
                    final String noun = fields[2];
                    return new Event(userId, verb, noun);
                })
                /*.assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forMonotonousTimestamps()
                                .withTimestampAssigner((Event e, long ts) -> System.currentTimeMillis())
                )*/
                .keyBy(Event::getUserId);

        source.print("source ");
        source.sum("count").print("sum");
        final Pattern<Event, Event> pattern = Pattern.<Event>begin("rule", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new DoSomething("burn", "cd"))
                .times(3);
        final PatternStream<Event> patternStream = CEP.pattern(source, pattern);
        patternStream.select(new PatternSelectFunction<Event, Object>() {
            @Override
            public Object select(Map<String, List<Event>> pattern) {
                return pattern.get("rule");
            }
        }).print("result");

        env.execute();
    }
}
