package zhengwei.flink.api.broadcast;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 广播变量的事件源
 * [userID, eventTime, eventType, productID]
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 18:11
 */
public class EventSource implements SourceFunction<Tuple4<String, Long, String, Integer>> {
    private boolean flag = true;
    private final Random random = new Random();

    @Override
    public void run(SourceContext<Tuple4<String, Long, String, Integer>> ctx) throws Exception {
        while (flag) {
            final int random = this.random.nextInt(4);
            final String userId = "user_" + random;
            final long eventTime = System.currentTimeMillis();
            final String eventType = "type_" + random;
            ctx.collect(Tuple4.of(userId, eventTime, eventType, random));
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
