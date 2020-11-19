package zhengwei.flink.api.transformation;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;

public class JavaKeyByTransformation {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        //单并行度读取 socket 文本流
        final DataStreamSource<String> source = env.socketTextStream(parameterTool.get("hostname"), parameterTool.getInt("port"));
        final SingleOutputStreamOperator<String> flatMap = source.flatMap((String s, Collector<String> out) -> {
            Arrays.stream(s.split("[ ]")).forEach(out::collect);
        }).returns(Types.STRING);
        final SingleOutputStreamOperator<Tuple2<String, Integer>> map = flatMap.map(s -> Tuple2.of(s, 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT));

        //如果此处没有调用 sum 这种聚合算子的话，那么传输过来的单词会存放到对应 TaskManager 的 subTask 的组中
        //类似于 Spark 的 hash partition，存在 shuffle 行为
        final SingleOutputStreamOperator<Tuple2<String, Integer>> sum = map.keyBy(0).sum(1);
        sum.print();
        env.execute();
    }
}
