package zhengwei.flink.api.window.function;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/1/6 20:17
 */
public class IncrementAggreationWindowFunction {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final int count = Integer.parseInt(fields[1]);
                    return Tuple2.of(key, count);
                })
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(tuple2 -> tuple2.f0)
                .timeWindow(Time.seconds(10))
                .aggregate(new AggregateFunction<Tuple2<String, Integer>, Map<String, Integer>, Tuple2<String, Integer>>() {
                    final Map<String, Integer> map = new HashMap<>();

                    //初始化累加器
                    @Override
                    public Map<String, Integer> createAccumulator() {
                        return map;
                    }

                    //对累加器进行累加
                    //对 key 出现的次数进行累加
                    @Override
                    public Map<String, Integer> add(Tuple2<String, Integer> value, Map<String, Integer> accumulator) {
                        accumulator.put(value.f0, accumulator.get(value.f0) + accumulator.getOrDefault(value.f0, 0) + value.f1);
                        return accumulator;
                    }

                    //获取累加器结果
                    @Override
                    public Tuple2<String, Integer> getResult(Map<String, Integer> accumulator) {
                        return null;
                    }

                    //聚合
                    //不存在跨分区聚合，因为经过 keyBy 之后所有 key 相同的
                    //时间窗口一般不使用，一般用于 session 窗口
                    @Override
                    public Map<String, Integer> merge(Map<String, Integer> a, Map<String, Integer> b) {
                        return null;
                    }
                })
                .print();
        env.execute();
    }
}
