package zhengwei.flink.api.join;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.TimeUnit;

/**
 * 构建一个商品Stream源（这个好比就是维表）
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/19 9:16
 */
public class MyGoodsSource implements SourceFunction<Goods> {
    private volatile boolean flag = true;

    @Override
    public void run(SourceContext<Goods> ctx) throws Exception {
        while (flag) {
            Goods.GOODS_LIST.forEach(ctx::collect);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
