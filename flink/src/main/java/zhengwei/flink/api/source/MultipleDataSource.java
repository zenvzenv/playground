package zhengwei.flink.api.source;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.NumberSequenceIterator;

/**
 * 多并行度数据源
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/17 19:47
 */
public class MultipleDataSource {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        final DataStreamSource<Long> nums = env.fromParallelCollection(new NumberSequenceIterator(1, 10), Long.class);
        final DataStreamSource<Long> nums = env.fromParallelCollection(new NumberSequenceIterator(1, 10), TypeInformation.of(Long.TYPE));
        System.out.println(nums.getParallelism());

        //多并行
        final DataStreamSource<Long> longDataStreamSource = env.generateSequence(1, 10000);
        System.out.println(longDataStreamSource.getParallelism());

        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        //多并行度
        final DataStreamSource<String> textFile = env.readTextFile(parameterTool.get("path"));

    }
}
