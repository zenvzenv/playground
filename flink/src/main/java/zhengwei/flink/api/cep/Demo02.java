package zhengwei.flink.api.cep;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/14 10:01
 */
public class Demo02 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<String> input = env.socketTextStream("localhost", 8888)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<String>forMonotonousTimestamps()
                                .withTimestampAssigner((String s, long ts) -> System.currentTimeMillis())
                );
        final Pattern<String, String> a1 = Pattern.<String>begin("a1")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {
                        return value.equals("a");
                    }
                })
                .within(Time.of(1, TimeUnit.SECONDS));

        final Pattern<String, String> b1 = Pattern.<String>begin("b1")
                .where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {
                        return value.equals("b");
                    }
                })
                .within(Time.of(1, TimeUnit.SECONDS));

        final PatternStream<String> a1PatternStream = CEP.pattern(input, a1);
        final PatternStream<String> b1PatternStream = CEP.pattern(input, b1);

        final DataStream<String> a1Stream = a1PatternStream.select(new PatternSelectFunction<String, String>() {
            @Override
            public String select(Map<String, List<String>> pattern) {
                return pattern.get("a1").get(0);
            }
        });
        a1Stream.print("a1 ");
        final DataStream<String> b1Stream = b1PatternStream.select(new PatternSelectFunction<String, String>() {
            @Override
            public String select(Map<String, List<String>> pattern) {
                return pattern.get("b1").get(0);
            }
        });
        b1Stream.print("b1 ");

        OutputTag<String> outputTag=new OutputTag<String>("tag"){};
        a1Stream.connect(b1Stream).map(new CoMapFunction<String, String, String>() {
            String result;
            @Override
            public String map1(String value) {
                return "a";
            }

            @Override
            public String map2(String value) {
                return "b";
            }
        }, TypeInformation.of(String.class))
        .print("connect ");

        env.execute();
    }
}
