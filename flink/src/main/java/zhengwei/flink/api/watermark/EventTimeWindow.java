package zhengwei.flink.api.watermark;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * 1. 事件发送时间
 * 2. 事件进入 Flink 的时间
 * 3. 事件处理时间
 * <p>
 * Watermark =进入Flink 的最大的事件时间(mxtEventTime)-指定的延迟时间(t)
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/14 10:15
 */
public class EventTimeWindow {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置时间语义是时间时间，默认是事件处理事件(ProcessTime)
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);
        //设置周期性的产生水位线的时间戳，当数据量很大时如果每个事件都产生水位线的话会影响性能
        env.getConfig().setAutoWatermarkInterval(100);
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final int count = Integer.parseInt(fields[1]);
                    //事件事件
                    final long time = Long.parseLong(fields[2]);
                    return Tuple3.of(key, count, time);
                })
                .returns(Types.TUPLE(Types.STRING, Types.INT, Types.LONG))
                .assignTimestampsAndWatermarks(
                        //使用固定间隔的水位线，水位线为2秒
                        WatermarkStrategy.<Tuple3<String, Integer, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                //指定 EventTime 对应的字段
                                //在计算是否到达水位线时，用的是毫秒
                                .withTimestampAssigner((Tuple3<String, Integer, Long> element, long recordTimestamp) -> element.f2 * 1_000L)
                )
                .keyBy(tuple3 -> tuple3.f0)
                //长度为3的滚动窗口
                .timeWindow(Time.seconds(3))
                .minBy(1)
                .print();
        env.execute();
    }
}
