package zhengwei.flink.api.sql;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Tumble;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import zhengwei.flink.api.sql.pojo.Order;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.lit;

/**
 * 使用Flink SQL来统计5秒内 每个用户的 订单总数、订单的最大金额、订单的最小金额
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/14 16:16
 */
public class Demo03 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        env.setRestartStrategy(RestartStrategies.noRestart());
        final EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        final StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env, settings);

        final DataStream<Order> source = env.addSource(new SourceFunction<Order>() {
            private volatile boolean flag = true;
            private final Random random = new Random();

            @Override
            public void run(SourceContext<Order> ctx) {
//                System.out.println(ctx);
                while (flag) {
                    final Order order = new Order(
                            UUID.randomUUID().toString(),
                            random.nextInt(100),
                            random.nextDouble(),
                            System.currentTimeMillis()
                    );
//                    System.out.println("event time -> " + order.getOrdTime());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ctx.collect(order);
                }
            }

            @Override
            public void cancel() {
                flag = false;
            }
        });

        //5秒 water mark
        final DataStream<Order> orderDS = source.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Order>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((o, recordTimestamp) -> o.getOrdTime())
        );


        //1. sql
        tabEnv.createTemporaryView("t_order", orderDS, $("name"), $("num"), $("amount"), $("ordTime").rowtime());
        //sql 形式的 30 秒滚动窗口 tumble
        final String sql = "select name,sum(num) as totalNum,max(amount) as maxAmount,min(amount) as minAmount from t_order group by name, tumble(ordTime, interval '30' second)";
        final Table resultTab1 = tabEnv.sqlQuery(sql);
        //toAppendStream -> 将计算后的数据append到结果DataStream中去
        //toRetractStream  -> 将计算后的新的数据在DataStream原数据的基础上更新true或是删除false
        final DataStream<Tuple2<Boolean, Row>> resultDS1 = tabEnv.toRetractStream(resultTab1, Row.class);
        resultDS1.print("sql result");

        //2. table
        final Table orderTab = tabEnv.fromDataStream(orderDS, $("name"), $("num"), $("amount"), $("ordTime").rowtime());
        final Table resultTable2 = orderTab
                .window(Tumble.over(lit(5).second())
                        .on($("ordTime"))
                        .as("tumbleWindow"))
                .groupBy($("tumbleWindow"), $("name"))
                .select($("name"),
                        $("num").sum().as("totalNum"),
                        $("amount").max().as("maxAmount"),
                        $("amount").min().as("minAmount"));
        final DataStream<Tuple2<Boolean, Row>> resultDS2 = tabEnv.toRetractStream(resultTable2, Row.class);
        resultDS2.print("table result");

        env.execute();
    }
}
