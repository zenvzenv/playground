package zhengwei.flink.restart;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * 演示重启FixedDelay的重启作业，flink作业异常后的行为以固定的频率重启作业。
 * 在整个作业的生命周期中，以固定次数重启出错的作业
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/27 20:30
 */
@Slf4j
public class FixedDelayRestartJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //第一个参数：重试次数
        //第二个参数：间隔时间
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(2, TimeUnit.SECONDS)));
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(3, TimeUnit.SECONDS)));

        env
            .addSource(new SourceFunction<Tuple3<String, Integer, Long>>() {
                @Override
                public void run(SourceContext<Tuple3<String, Integer, Long>> ctx) throws Exception {
                    int index = 1;
                    while (true) {
                        ctx.collect(new Tuple3<>("key", index++, System.currentTimeMillis()));
                        TimeUnit.MILLISECONDS.sleep(2);
                    }
                }

                @Override
                public void cancel() {

                }
            })
            .map(new MapFunction<Tuple3<String, Integer, Long>, Tuple3<String, Integer, String>>() {
                @Override
                public Tuple3<String, Integer, String> map(Tuple3<String, Integer, Long> event) throws Exception {
                    if (event.f1 % 10 == 0) {
                        String msg = String.format("Bad num [%d]", event.f1);
                        log.error(msg);
                        throw new RuntimeException(msg);
                    }
                    return new Tuple3<>(event.f0, event.f1, new Timestamp(System.currentTimeMillis()).toString());
                }
            })
            .print();
        env.execute("FixedDelayRestartJob");
    }
}
