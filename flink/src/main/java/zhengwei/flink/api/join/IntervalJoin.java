package zhengwei.flink.api.join;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/16 11:28
 */
public class IntervalJoin {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        final SingleOutputStreamOperator<Goods> goodsSource = env.addSource(new MyGoodsSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<Goods>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis())
                );

        final SingleOutputStreamOperator<Order> orderSource = env.addSource(new MyOrderSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Order>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis())
                );

        orderSource
                .keyBy(Order::getGoodsId)
                .intervalJoin(goodsSource.keyBy(Goods::getGoodsId))
                //当前 join 时刻的前一秒至 join 时刻的后两秒
                //笛卡尔积
                .between(Time.seconds(-1), Time.seconds(2))
                .process(new ProcessJoinFunction<Order, Goods, FactOrder>() {
                    @Override
                    public void processElement(Order left, Goods right, Context ctx, Collector<FactOrder> out) {
                        final FactOrder factOrder = new FactOrder();
                        factOrder.setGoodsId(right.getGoodsId());
                        factOrder.setGoodsName(right.getGoodsName());
                        factOrder.setCount(left.getCount());
                        factOrder.setPrice(right.getPrice().multiply(new BigDecimal(left.getCount())));
                        out.collect(factOrder);
                    }
                })
                .print("interval join");

        env.execute();
    }
}
