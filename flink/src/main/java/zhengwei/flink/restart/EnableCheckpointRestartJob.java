package zhengwei.flink.restart;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * 开启Checkpoint之后，Flink作业异常后的默认重启作业策略（固定频率）。
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/28 14:04
 */
@Slf4j
public class EnableCheckpointRestartJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 作业是fixed-delay重启策略
        env.enableCheckpointing(10_000);

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
        env.execute("EnableCheckpointRestartJob");
    }
}
