package zhengwei.flink.api.window;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * 滑动窗口简单示例：统计一个窗口内 key 的最小值
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/24 20:05
 */
public class SlidingWindowDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStreamSource<String> source = env.socketTextStream("localhost", 8888);
        //kafka 数据以 "," 号分割
        final SingleOutputStreamOperator<Tuple2<String, Integer>> map = source
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    return Tuple2.of(fields[0], Integer.parseInt(fields[1]));
                })
                //需要补充返回类型，因为泛型在 runtime 的时候会被擦除，Flink 在 runtime 不知道具体类型
                .returns(Types.TUPLE(Types.STRING, Types.INT));
        //Flink 1.11 对 keyBy 进行了升级
        final KeyedStream<Tuple2<String, Integer>, String> keyBy = map.keyBy(tuples -> tuples.f0);
        //时间长度为30秒，步长为15秒的滑动窗口
        final WindowedStream<Tuple2<String, Integer>, String, TimeWindow> window = keyBy.window(SlidingProcessingTimeWindows.of(Time.seconds(30), Time.seconds(15)));
        //声明一个滑动窗口的简写形式
//        final WindowedStream<Tuple2<String, Integer>, String, TimeWindow> windows = keyBy.timeWindow(Time.seconds(30),Time.seconds(15));
        final SingleOutputStreamOperator<Tuple2<String, Integer>> result = window.minBy(1);
        result.print();
        env.execute();
    }
}
