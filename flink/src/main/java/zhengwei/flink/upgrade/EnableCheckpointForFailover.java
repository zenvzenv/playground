package zhengwei.flink.upgrade;

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
 * @author zhengwei AKA Awei
 * @since 2020/10/28 15:57
 */
@Slf4j
public class EnableCheckpointForFailover {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(2, TimeUnit.SECONDS)));
        env.enableCheckpointing(20);

        env
            .addSource(new SourceFunction<Tuple3<String, Integer, Long>>() {
                @Override
                public void run(SourceContext<Tuple3<String, Integer, Long>> ctx) throws Exception {
                    int i = 1;
                    while (true) {
                        ctx.collect(new Tuple3<>("key", i++, System.currentTimeMillis()));
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
                        String msg = String.format("Bad data [%d]...", event.f1);
                        log.error(msg);
                        // 抛出异常，作业根据 配置 的重启策略进行恢复，无重启策略作业直接退出。
                        throw new RuntimeException(msg);
                    }
                    return new Tuple3<>(event.f0, event.f1, new Timestamp(System.currentTimeMillis()).toString());
                }
            })
            .keyBy(0)
            .sum(1)
            .print();
        env.execute("EnableCheckpointForFailover");
    }
}
