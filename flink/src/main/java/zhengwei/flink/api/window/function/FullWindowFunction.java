package zhengwei.flink.api.window.function;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.IterableUtils;
import zhengwei.util.common.DateUtils;

import java.time.format.DateTimeFormatter;

/**
 * 全窗口函数
 * <p>
 * 先把要计算的数据全部都收集起来，等到窗口关闭的时候，再把窗口中所有的数据拉出来进行计算
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/6 19:25
 */
public class FullWindowFunction {
    public static void main(String[] args) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                .timeWindow(Time.seconds(15))
                //第一个泛型：输入数据类型
                //第二个泛型：输出数据类型
                //第三个泛型：拿到当前 key 的信息
                //第四个泛型：window 的类型
                .apply(new WindowFunction<Tuple2<String, Integer>, Tuple4<String, String, String, Integer>, String, TimeWindow>() {
                    @Override
                    public void apply(String key, TimeWindow window, Iterable<Tuple2<String, Integer>> input, Collector<Tuple4<String, String, String, Integer>> out) throws Exception {
                        final long windowStart = window.getStart();
                        final long windowEnd = window.getEnd();
                        final int count = (int) IterableUtils.toStream(input).count();
                        //由于返回值是 void，所以需要通过 Collector 来收集结果数据
                        out.collect(Tuple4.of(DateUtils.longToStrFormat(windowStart), DateUtils.longToStrFormat(windowEnd), key, count));
                    }
                })
                .print();
        env.execute();
    }
}
