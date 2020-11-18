package zhengwei.flink.wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        env
                .socketTextStream(parameterTool.get("hostname"), parameterTool.getInt("port"))
                .flatMap((FlatMapFunction<String, Tuple2<String, Integer>>) (line, collector) -> {
                    final String[] words = line.split("[ ]");
                    for (String word : words) {
                        collector.collect(Tuple2.of(word, 1));
                    }
                })
                .keyBy(0)
                .sum(1);
        env.execute();
    }
}
