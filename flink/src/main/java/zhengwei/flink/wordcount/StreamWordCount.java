package zhengwei.flink.wordcount;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //在 Flink 1.12.x 中已经将流批统一了
        //只需在 env 中设置下运行模式即可
        //automatic 会根据数据源自动推测出是批处理还是流处理
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        env
                .socketTextStream(parameterTool.get("hostname"), parameterTool.getInt("port"))
                .flatMap((FlatMapFunction<String, Tuple2<String, Integer>>) (line, collector) -> {
                    final String[] words = line.split("[ ]");
                    for (String word : words) {
                        collector.collect(Tuple2.of(word, 1));
                    }
                })
                .keyBy(tuple2->tuple2.f0)
                .sum(1);
        env.execute();
    }
}
