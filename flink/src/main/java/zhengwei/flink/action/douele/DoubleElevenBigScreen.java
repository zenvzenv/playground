package zhengwei.flink.action.douele;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousEventTimeTrigger;

import java.time.Duration;

/**
 * 模拟双十一大屏处理
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 9:05
 */
public class DoubleElevenBigScreen {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        env.setParallelism(1);
        env.setRestartStrategy(RestartStrategies.noRestart());
        final SingleOutputStreamOperator<Category> preAgg = env
                .addSource(new DoubleElevenSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Tuple3<String, Double, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                //水位线的单位是毫秒 ms
                                .withTimestampAssigner((event, timestamp) -> event.f2)
                )
                .keyBy(t2 -> t2.f0)
                .window(TumblingEventTimeWindows.of(Time.days(1), Time.hours(-8)))
                //设置触发器，每秒触发一次
                .trigger(ContinuousEventTimeTrigger.of(Time.seconds(1)))
                .aggregate(new PriceAggregate(), new WindowResult());

        preAgg.print("预先聚合结果");

        preAgg.keyBy(Category::getDateTime)
                .window(TumblingEventTimeWindows.of(Time.seconds(1)))
                .process(new WindowResultProcess());

        env.execute();
    }
}
