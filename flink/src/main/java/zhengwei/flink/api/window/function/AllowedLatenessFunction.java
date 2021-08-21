package zhengwei.flink.api.window.function;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

/**
 * @author zhengwei AKA zenv
 * @since 2021/1/9 20:16
 */
public class AllowedLatenessFunction {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final OutputTag<Tuple2<String, Integer>> lateOutputTag = new OutputTag<Tuple2<String, Integer>>("late") {
        };
        final SingleOutputStreamOperator<Tuple2<String, Integer>> sum = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final int count = Integer.parseInt(fields[1]);
                    return Tuple2.of(key, count);
                })
                .keyBy(tuple2 -> tuple2.f0)
                .timeWindow(Time.seconds(15))
                //允许迟到数据
                //当窗口到达既定的关闭时刻时，先触发计算，但是计算完毕之后不将窗口关闭
                //再将窗口开启指定的时间之后再关闭
                //迟到的数据还可以被处理到
                //但是不推荐设置太长时间，因为窗口没有关闭的话，窗口中的状态需要保存在状态后端中，
                //如果时间过长，会对存储系统造成压力
                .allowedLateness(Time.minutes(1))
                .sideOutputLateData(lateOutputTag)
                .sum(1);
        sum.print();
        //lambda 架构，批处理 + 流处理
        //side output 是批处理，待处理完毕之后由用户自行合并到之前的结果中
        sum.getSideOutput(lateOutputTag).print("late");
        env.execute();
    }
}
