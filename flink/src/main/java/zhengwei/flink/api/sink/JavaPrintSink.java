package zhengwei.flink.api.sink;

import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/21 13:22
 */
public class JavaPrintSink {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStreamSource<String> source = env.socketTextStream("localhost", 8888);
        final DataStreamSink<String> print = source.print("res");

        //print sink 是并行输出算子，可以手动设置并行度
        print.setParallelism(2);
        env.execute();
    }
}
