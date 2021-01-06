package zhengwei.flink.api.window.function;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * 窗口增量聚合函数-reduce/aggregation function
 * <p>
 * 每次聚合完之后并补输出结果，而是等到 window 被关闭之后才会输出结果，之前的结果都被存储在状态后端当中
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/6 18:27
 */
public class IncrementReduceWindowFunction {
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
                //实现类似于 sum 的功能
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    //第一个参数是已经聚合好的的值
                    //待聚合的最新状态的值
                    //返回聚合之后的值
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                        return Tuple2.of(value1.f0, value1.f1 + value2.f1);
                    }
                })
                .print();
        env.execute();
    }
}
