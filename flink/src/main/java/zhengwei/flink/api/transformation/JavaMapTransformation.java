package zhengwei.flink.api.transformation;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 对 DataStream 进行操作生成新的 DataStream
 */
public class JavaMapTransformation {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final DataStreamSource<Integer> res = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8);
        //功能更丰富的的 Map Function
        final SingleOutputStreamOperator<Integer> map = res.map(new RichMapFunction<Integer, Integer>() {
            /**
             * 在构造方法之后，在 map 方法执行之前，执行一次
             * 用来初始化连接，或者初始化或回复历史状态
             */
            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
            }

            @Override
            public void close() throws Exception {
                super.close();
            }

            @Override
            public Integer map(Integer i) throws Exception {
                return i * 2;
            }
        });
        map.print();
        env.execute();
    }
}
