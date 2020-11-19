package zhengwei.flink.api.transformation;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;

public class JavaFlatTransformation {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStreamSource<String> source = env.fromElements(
                "flink spark hadoop",
                "zhengwei1 zhengwei2 zhengwei3",
                "zenv1 zenv2"
        );
        //FlatMapFunction 也有 RichFlatMapFunction
        final SingleOutputStreamOperator<String> flatMap = source.flatMap(
                (String s, Collector<String> collector) -> Arrays.stream(s.split("[ ]")).forEach(collector::collect)
        ).returns(Types.STRING);
        flatMap.print();
        env.execute();
    }
}
