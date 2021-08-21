package zhengwei.flink.action.autopraise;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 自动好评数据源
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 17:07
 */
public class AutoPraiseSource implements SourceFunction<Tuple3<String, String, Long>> {
    private volatile boolean flag = true;
    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public void run(SourceContext<Tuple3<String, String, Long>> ctx) throws Exception {
        while (flag) {
            final String userId = random.nextInt(5) + "";
            final String orderId = UUID.randomUUID().toString();
            final long orderTime = System.currentTimeMillis();
            ctx.collect(Tuple3.of(userId, orderId, orderTime));
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
