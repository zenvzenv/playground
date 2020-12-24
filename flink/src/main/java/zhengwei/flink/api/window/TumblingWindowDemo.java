package zhengwei.flink.api.window;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * 滚动时间窗口，滚动事件窗口是特殊的滑动窗口，滑动步长 = 窗口大小
 * <p>
 * 窗口分配器：滚动窗口，滑动窗口，计数窗口，全局窗口
 * <p>
 * 一个完整的开窗操作包含分配窗口和窗口函数
 * <p>
 * 本例功能：开一个时间跨度为30秒的滚动窗口，统计其中每个key的最小值
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/5 14:19
 */
public class TumblingWindowDemo {
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
        //长度为15秒的滚动窗口
        final WindowedStream<Tuple2<String, Integer>, String, TimeWindow> window = keyBy.window(TumblingProcessingTimeWindows.of(Time.seconds(30)));
        //声明一个服你懂窗口的简写形式
        final WindowedStream<Tuple2<String, Integer>, String, TimeWindow> windows = keyBy.timeWindow(Time.seconds(20));
        final SingleOutputStreamOperator<Tuple2<String, Integer>> result = window.minBy(1);
        result.print();
        env.execute();
    }
}
