package zhengwei.flink.api.join;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 构建订单明细 Stream 源
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/19 9:10
 */
public class MyOrderSource implements SourceFunction<Order> {
    private volatile boolean flag = true;
    private static final Random random = new Random(System.currentTimeMillis());

    @Override
    public void run(SourceContext<Order> ctx) throws Exception {
        while (flag) {
            final Goods goods = Goods.randomGoods();
            final Order order = new Order();
            order.setGoodsId(goods.getGoodsId());
            order.setCount(random.nextInt(10) + 1);
            order.setOrderId(UUID.randomUUID().toString());
            ctx.collect(order);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
