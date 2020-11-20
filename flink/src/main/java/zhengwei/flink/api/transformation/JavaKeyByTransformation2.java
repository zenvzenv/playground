package zhengwei.flink.api.transformation;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/19 18:45
 */
public class JavaKeyByTransformation2 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        final String hostname = parameterTool.get("hostname");
        final int port = parameterTool.getInt("port");
        final DataStreamSource<String> source = env.socketTextStream(hostname, port);

        final SingleOutputStreamOperator<Tuple3<String, String, Double>> flatMap = source.flatMap((String line, Collector<Tuple3<String, String, Double>> collector) -> {
            final String[] split = line.split("[ ]");
            String province = split[0];
            String city = split[1];
            double money = Double.parseDouble(split[2]);
            collector.collect(Tuple3.of(province, city, money));
        }).returns(Types.TUPLE(Types.STRING, Types.STRING, Types.DOUBLE));

        final SingleOutputStreamOperator<Tuple3<String, String, Double>> sum = flatMap.keyBy(t -> t.f0).sum(2);
        sum.print();

        env.execute();
    }
}
