package zhengwei.flink.restart;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.TimeUnit;

/**
 * 没有重启策略的实例，不重启作业也是 Flink 的默认策略
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/27 19:57
 */
@Slf4j
public class NoRestartJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        /*
        使用没有重启的策略，Flink出现异常之后程序退出。
        增加env.setRestartStrategy(RestartStrategies.noRestart());观察行为和默认一样。
         */
        env.setRestartStrategy(RestartStrategies.noRestart());

        DataStream<Tuple3<String, Integer, Long>> source = env
                .addSource(new SourceFunction<Tuple3<String, Integer, Long>>() {
                    @Override
                    public void run(SourceContext<Tuple3<String, Integer, Long>> ctx) throws Exception {
                        int index = 1;
                        while (true) {
                            ctx.collect(new Tuple3<>("key", index++, System.currentTimeMillis()));
                            // Just for testing
                            TimeUnit.SECONDS.sleep(1);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
        source.map(new MapFunction<Tuple3<String, Integer, Long>, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(Tuple3<String, Integer, Long> event) throws Exception {
                if (event.f1 % 100 == 0) {
                    String msg = String.format("Bad data [%d]...", event.f1);
                    log.error(msg);
                    // 抛出异常，作业根据 配置 的重启策略进行恢复，无重启策略作业直接退出。
                    throw new RuntimeException(msg);
                }
                return new Tuple2<>(event.f0, event.f1);
            }
        })
                .print();

        env.execute("NoRestart");
    }
}
