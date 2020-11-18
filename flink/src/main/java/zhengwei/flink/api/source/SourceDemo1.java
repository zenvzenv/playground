package zhengwei.flink.api.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;

public class SourceDemo1 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //DataStream 中并不存真正的数据，类似于 Spark 的 RDD，是一个抽象集合

        //并行度为1
//        final DataStreamSource<Integer> nums = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8);
        final DataStreamSource<Integer> nums = env.fromCollection(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        nums.filter(integer -> (integer & 1) == 0).print();
        env.execute();
    }
}
