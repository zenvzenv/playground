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
 * 按照一个失败频率来重启失败的任务。
 * 比如一个时间间隔内，允许作业失败多少次是可以容忍的，超过了容忍的次数则会退出作业，
 * 如果在规定的时间内失败的次数没有超过规定的次数，那么在下个时间周期内，可以失败的
 * 次数将会被重置
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/28 13:39
 */
@Slf4j
public class FailureRateRestartJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 第一个参数：允许失败的次数
        // 第二个参数：允许失败的时间间隔
        // 第三个参数：失败任务的重启延迟时间
        // 在5分钟内允许失败5次，每次失败重启的延迟时间为5秒
//        env.setRestartStrategy(RestartStrategies.failureRateRestart(5, Time.of(5, TimeUnit.MINUTES), Time.of(5, TimeUnit.SECONDS)));]

        // 改策略failureRateRestart(5, Time.of(5, TimeUnit.MINUTES), Time.of(5, TimeUnit.SECONDS)))，
        // 也即是 5秒内如果又5次异常，则终止作业。本测试将会 不停的重启，因为，5秒内始终不会达到5次异常。
        env.setRestartStrategy(RestartStrategies.failureRateRestart(5, Time.of(5, TimeUnit.SECONDS), Time.of(5, TimeUnit.SECONDS)));
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
                public Tuple3<String, Integer, String> map(Tuple3<String, Integer, Long> value) throws Exception {
                    if (value.f1 % 10 == 0) {
                        String msg = String.format("bad num [%d]", value.f1);
                        log.error(msg);
                        throw new RuntimeException(msg);
                    }
                    return new Tuple3<>(value.f0, value.f1, new Timestamp(value.f2).toString());
                }
            })
            .print();
        env.execute("FailureRateRestartJob");
    }
}
