package zhengwei.flink.api.transformation;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class JavaFilterTrandformation {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStreamSource<Integer> source = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8);
        final SingleOutputStreamOperator<Integer> filter = source.filter(i -> (i & 1) == 1).returns(Integer.class);
        filter.print();
        env.execute();
    }
}
