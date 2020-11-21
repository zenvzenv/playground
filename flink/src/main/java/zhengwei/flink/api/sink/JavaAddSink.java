package zhengwei.flink.api.sink;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

/**
 * 自定义 sink
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/21 14:29
 */
public class JavaAddSink {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStreamSource<String> source = env.socketTextStream("localhost", 8888);
        source.addSink(new RichSinkFunction<String>() {
            @Override
            public void invoke(String value, Context context) throws Exception {
                final int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
                System.out.println(indexOfThisSubtask + "->" + value);
            }
        });
        env.execute();
    }
}
