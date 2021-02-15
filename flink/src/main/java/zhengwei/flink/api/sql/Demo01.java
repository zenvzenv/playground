package zhengwei.flink.api.sql;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import zhengwei.flink.api.sql.pojo.Order;

import java.util.Arrays;

import static org.apache.flink.table.api.Expressions.$;

/**
 * 将 DataStream 注册成 Table 和 View
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/5 11:09
 */
public class Demo01 {
    public static void main(String[] args) throws Exception {
        //准备 Flink SQL 的执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        final EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        final StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env, settings);

        //准备测试数据
        final DataStreamSource<Order> orderA = env.fromCollection(Arrays.asList(
                new Order("a", 1, 2.1),
                new Order("b", 2, 2.2),
                new Order("c", 1, 1.5)
        ));

        final DataStreamSource<Order> orderB = env.fromCollection(Arrays.asList(
                new Order("d", 4, 5.5),
                new Order("b", 6, 2.1),
                new Order("e", 1, 3.2)
        ));

        //注册表
        //注册成 Table
        final Table tabA = tabEnv.fromDataStream(orderA, $("name"), $("num"), $("amount"));
        //注册成临时 View
        tabEnv.createTemporaryView("tabB", orderB, $("name"), $("num"), $("amount"));


        final String sql = "select * from " + tabA + " where amount > 2 " +
                "union " +
                "select * from tabB where amount > 2";
        System.out.println(sql);

        //执行 sql
        final Table result = tabEnv.sqlQuery(sql);

        //Table 无法直接输出结果，需要将 Table 转成 DataStream 之后再输出
        //注意：此处不能使用 toAppendStream，因为 toAppendStream 不支持更新消费更新和删除更改
        final DataStream<Tuple2<Boolean, Order>> resultDS = tabEnv.toRetractStream(result, Order.class);

        resultDS.print();

        env.execute();
    }
}
