package zhengwei.antlr.userviolation;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/11 10:20
 */
public class FlinkCEPDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<Event> input = env.socketTextStream("localhost", 8888)
                .filter(line -> !"".equals(line))
                .map(line -> {
                    final String[] fields = line.split("[ ]");
                    final String userId = fields[0];
                    final String verb = fields[1];
                    final String noun = fields[2];
                    return new Event(userId, verb, noun);
                })
                .keyBy(Event::getUserId)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forMonotonousTimestamps()
                                .withTimestampAssigner((Event e, long ts) -> System.currentTimeMillis())
                );

        input.print("input ");

        final Pattern<Event, Event> pattern = Pattern
                .<Event>begin("rule")
                .where(new RangeTime("20210511083000", "20210511120000"))
                .where(new DoSomething("burn", "CD"))
                .times(3);
        System.out.println(pattern);

        final PatternStream<Event> patternStream = CEP.pattern(input, pattern);
        patternStream.select(new PatternSelectFunction<Event, Object>() {
            @Override
            public Object select(Map<String, List<Event>> pattern) throws Exception {
                return pattern.get("rule");
            }
        })
                .print("result");

        env.execute();
    }
}
