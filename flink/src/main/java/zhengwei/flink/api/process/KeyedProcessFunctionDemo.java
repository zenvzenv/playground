package zhengwei.flink.api.process;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 底层 API process function
 */
public class KeyedProcessFunctionDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final String value = fields[1];
                    return Tuple2.of(key, value);
                })
                .returns(Types.TUPLE(Types.STRING, Types.STRING))
                .keyBy(tuple2 -> tuple2.f0)
                .process(new MyKeyedProcessFunction())
                .print();
        env.execute();
    }

    private static class MyKeyedProcessFunction extends KeyedProcessFunction<String, Tuple2<String, String>, Integer> {
        private ValueState<Long> timestampState;

        @Override
        public void open(Configuration parameters) throws Exception {
            timestampState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("timestampState", Long.class));
        }

        @Override
        public void processElement(Tuple2<String, String> tuple2, Context context, Collector<Integer> collector) throws Exception {
            collector.collect(tuple2.f1.length());
            //获取当前的 key
            context.getCurrentKey();
            //也可以将结果输出到侧输出流中
//            context.output();
            System.out.println(context.timestamp());
            //当前的 watermark，前提是用的是事件事件
            context.timerService().currentWatermark();
            //注册一个定时器，所传入的时间是绝对时间，即从 1970.1.1 开始的毫秒数
            //通常是用当前的处理时间向后延迟一段时间
            //当定时器里的时间到达之后，将会执行 onTimer 方法
            //当前定时器针对 key 有效，而不是 MyKeyedProcessFunction 当前实例的所有分区有效
            context.timerService().registerEventTimeTimer(1000L);
            final long time = context.timerService().currentProcessingTime() + 5_000L;
            //保存需要删除的时间戳到状态中
            timestampState.update(time);
            context.timerService().registerProcessingTimeTimer(time);

            //对于删除特定的定时器，使用的是传入的时间戳来区分各个定时器
            context.timerService().deleteProcessingTimeTimer(timestampState.value());
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<Integer> out) throws Exception {
            //获取当前 key
            ctx.getCurrentKey();
            //当前处理的时间语义，是处理时间还是事件时间
            ctx.timeDomain();
            //输出到侧输出流
//            ctx.output();
            System.out.println(timestamp + " 定时器被触发");
        }

        //清理工作
        @Override
        public void close() throws Exception {
            timestampState.clear();
        }
    }
}
