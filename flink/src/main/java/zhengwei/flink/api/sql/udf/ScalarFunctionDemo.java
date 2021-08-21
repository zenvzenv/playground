package zhengwei.flink.api.sql.udf;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

/**
 * 输入若干个任意类型的参数，输出
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/17 16:45
 */
public class ScalarFunctionDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env);

        final SingleOutputStreamOperator<Tuple2<String, Integer>> source = env
                .socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    return Tuple2.of(fields[0], Integer.parseInt(fields[1]));
                })
                .returns(Types.TUPLE(Types.STRING, Types.INT));
        //注册自定义函数
        final MyScalarFunction myScalarFunction = new MyScalarFunction();
        tabEnv.createTemporarySystemFunction("myScalarFunction", myScalarFunction);


        final Table table = tabEnv.fromDataStream(source, $("word"), $("count")).select(call("myScalarFunction", $("word")));
        tabEnv.toAppendStream(table, Row.class).print();

        env.execute();
    }

    /**
     * 必须有 eval 函数，访问权限必须是 public
     */
    public static final class MyScalarFunction extends ScalarFunction {
        public String eval(String word) {
            return word + "_eval";
        }
    }
}
