package zhengwei.flink.api.transformation;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * reduce 算子
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/19 20:09
 */
public class JavaReduceTransformation {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        final String hostname = parameterTool.get("hostname");
        final int port = parameterTool.getInt("port");
        final DataStreamSource<String> source = env.socketTextStream(hostname, port);
        final SingleOutputStreamOperator<Tuple2<String, Integer>> flatMap = source.flatMap((String line, Collector<Tuple2<String, Integer>> collector) -> {
            final String[] split = line.split("[ ]");
            final String key = split[0];
            final Tuple2<String, Integer> value = Tuple2.of(key, Integer.parseInt(split[1]));
            collector.collect(value);
        }).returns(Types.TUPLE(Types.STRING, Types.INT));
        final KeyedStream<Tuple2<String, Integer>, String> keyBy = flatMap.keyBy(t -> t.f0);
        final SingleOutputStreamOperator<Tuple2<String, Integer>> reduce = keyBy.reduce((Tuple2<String, Integer> t1, Tuple2<String, Integer> t2) -> {
            t1.f1 += t2.f1;
            return t1;
        }).returns(Types.TUPLE(Types.STRING, Types.STRING));

        reduce.print();
        env.execute();
    }
}
