package zhengwei.flink.api.window;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author zhengwei AKA zenv
 * @since 2021/1/9 15:20
 */
public class CountWindowDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final double temperature = Double.parseDouble(fields[1]);
                    return Tuple2.of(key, temperature);
                })
                .returns(Types.TUPLE(Types.STRING, Types.DOUBLE))
                .keyBy(tuple2 -> tuple2.f0)
                //滑动计数窗口，滑动步长为2，窗口长度为10
                .countWindow(10, 2)
                //计算接下来的10个数据的平均温度
                .aggregate(new AggregateFunction<Tuple2<String, Double>, Tuple2<Double, Integer>, Double>() {
                    //初始化累加器
                    @Override
                    public Tuple2<Double, Integer> createAccumulator() {
                        return Tuple2.of(0.0, 0);
                    }

                    //进行累加
                    //accumulator 可以存储聚合的中间状态，在编程上更加灵活
                    @Override
                    public Tuple2<Double, Integer> add(Tuple2<String, Double> value, Tuple2<Double, Integer> accumulator) {
                        final Tuple2<Double, Integer> tuple2 = Tuple2.of(accumulator.f0 + value.f1, accumulator.f1 + 1);
                        System.out.println(tuple2);
                        return tuple2;
                    }

                    //获取结果，在这是获取平均值
                    @Override
                    public Double getResult(Tuple2<Double, Integer> accumulator) {
                        return accumulator.f0 / accumulator.f1;
                    }

                    @Override
                    public Tuple2<Double, Integer> merge(Tuple2<Double, Integer> a, Tuple2<Double, Integer> b) {
                        return Tuple2.of(a.f0 + b.f0, a.f1 + b.f1);
                    }
                })
                .print();
        env.execute();
    }
}
