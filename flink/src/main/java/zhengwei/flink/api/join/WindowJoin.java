package zhengwei.flink.api.join;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 演示双流 WindowJoin - 滚动窗口 join
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/16 9:12
 */
public class WindowJoin {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStream<Goods> goodsSource = env
                .addSource(new MyGoodsSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Goods>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis())
                );
        final SingleOutputStreamOperator<Order> orderSource = env.addSource(new MyOrderSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<Order>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis())
                );

        orderSource.join(goodsSource)
                .where(Order::getGoodsId)//第一个流的连接字段条件
                .equalTo(Goods::getGoodsId)//第二个流的连接字段条件
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .apply((Order order, Goods goods) -> {
                    final FactOrder factOrder = new FactOrder();
                    factOrder.setGoodsId(goods.getGoodsId());
                    factOrder.setGoodsName(goods.getGoodsName());
                    factOrder.setCount(order.getCount());
                    factOrder.setPrice(goods.getPrice().multiply(new BigDecimal(order.getCount())));
                    return factOrder;
                })
                .print("滚动窗口 join");

        env.execute();
    }
}
