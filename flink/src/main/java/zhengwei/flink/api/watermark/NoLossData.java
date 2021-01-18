package zhengwei.flink.api.watermark;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

/**
 * flink 对于不丢数据做了三重保障，水位线 + allowedLateness + sideOutputLateData 来保障数据不丢失
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/14 19:10
 */
public class NoLossData {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);
        final OutputTag<Tuple3<String, Integer, Long>> late = new OutputTag<Tuple3<String, Integer, Long>>("late") {
        };
        final SingleOutputStreamOperator<Tuple3<String, Integer, Long>> min = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final int count = Integer.parseInt(fields[1]);
                    final long timestamp = Long.parseLong(fields[2]);
                    return Tuple3.of(key, count, timestamp);
                })
                .returns(Types.TUPLE(Types.STRING, Types.INT, Types.LONG))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Tuple3<String, Integer, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                                .withTimestampAssigner((Tuple3<String, Integer, Long> element, long recordTimestamp) -> element.f2 * 1_000L)
                )
                .keyBy(tuple3 -> tuple3.f0)
                .timeWindow(Time.seconds(5))
                //当水位线触发了窗口计算的之后，窗口不会关闭，而是会延迟5秒之后再关闭
                //窗口会在11秒的时候关闭，窗口大小 + 延迟时间 + 水位线
                .allowedLateness(Time.seconds(5))
                //当延时关闭的窗口也关闭了，可以有兜底方法，最终输出到侧输出流中
                .sideOutputLateData(late)
                .minBy(1);
        min.print("min");
        min.getSideOutput(late).print("late");

        env.execute();
    }
}
