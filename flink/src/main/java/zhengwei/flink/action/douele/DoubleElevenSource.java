package zhengwei.flink.action.douele;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 模拟双十一源数据
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 9:11
 */
public class DoubleElevenSource implements SourceFunction<Tuple3<String, Double, Long>> {
    private volatile boolean flag = true;
    private final String[] categories = new String[]{"女装", "男装", "图书", "家电", "洗护", "美妆", "运动", "游戏", "户外", "家具", "乐器", "办公"};
    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public void run(SourceContext<Tuple3<String, Double, Long>> ctx) throws Exception {
        while (flag) {
            final String category = categories[random.nextInt(categories.length - 1)];
            final double amount = random.nextDouble() * 100;
            final long time = System.currentTimeMillis();
            ctx.collect(new Tuple3<>(category, amount, time));
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
