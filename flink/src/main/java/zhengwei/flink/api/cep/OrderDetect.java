package zhengwei.flink.api.cep;

import lombok.Data;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.PatternTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 检测订单状态，订单是否在规定时间内进行了支付操作
 */
public class OrderDetect {
    @Data
    final static class Order {
        private String orderId;
        private String status;
        private String dealNo;
        private long ts = System.currentTimeMillis();

        public Order(String orderId, String status, String dealNo) {
            this.orderId = orderId;
            this.status = status;
            this.dealNo = dealNo;
        }
    }

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<Order> source = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String orderId = fields[0];
                    final String status = fields[1];
                    final String dealNo = fields[2];
                    return new Order(orderId, status, dealNo);
                })
                .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());

        //1. 定义 pattern
        final Pattern<Order, Order> pattern = Pattern.<Order>begin("create")
                .where(new SimpleCondition<Order>() {
                    @Override
                    public boolean filter(Order value) {
                        return value.getStatus().equals("create");
                    }
                })
                .followedBy("pay")
                .where(new SimpleCondition<Order>() {
                    @Override
                    public boolean filter(Order value) {
                        return value.getStatus().equals("pay");
                    }
                })
                .within(Time.of(15, TimeUnit.MINUTES));

        //2. 定义侧输出流用来表示超时未支付订单事件
        final OutputTag<String> orderOutputTag = new OutputTag<String>("order") {
        };

        final PatternStream<Order> patternStream = CEP.pattern(source, pattern);

        final SingleOutputStreamOperator<String> result = patternStream.select(orderOutputTag, new OrderTimeoutSelect(), new OrderPaySelect());

        result.print("normal>>>");
        final DataStream<String> sideOutput = result.getSideOutput(orderOutputTag);
        sideOutput.print("exception>>>");

        env.execute();
    }

    final static class OrderTimeoutSelect implements PatternTimeoutFunction<Order, String> {

        /**
         * 此时说明在规定的时间内，订单没有完成 pay 操作，只完成了 create 操作
         *
         * @param pattern 拦截到的结果
         * @param l       时间戳
         */
        @Override
        public String timeout(Map<String, List<Order>> pattern, long l) {
            final List<Order> create = pattern.get("create");
            //因为 pattern 是单例的，所以 list 中只会有一个事件
            final Order order = create.get(0);
            return "超时的未支付的订单id是：" + order.orderId + "，事件为：" + l;
        }
    }

    final static class OrderPaySelect implements PatternSelectFunction<Order, String> {

        @Override
        public String select(Map<String, List<Order>> pattern) {
            final Order pay = pattern.get("pay").get(0);
            return "成功支付的订单号：" + pay.orderId;
        }
    }
}
