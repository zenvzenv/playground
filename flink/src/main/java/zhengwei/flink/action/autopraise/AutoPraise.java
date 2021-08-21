package zhengwei.flink.action.autopraise;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/15 15:19
 */
public class AutoPraise {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        //--interval 5000
        //订单多久间隔未评价之后自动好评
        final long interval = parameterTool.getLong("interval");

        env.addSource(new AutoPraiseSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<Tuple3<String, String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner((t3, timestamp) -> t3.f2)
                )
                .keyBy(t3 -> t3.f0)
                .process(new TimerProcessFunc(interval));

        env.execute();
    }
}
