package zhengwei.flink.api.process;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author zhengwei AKA zenv
 * @since 2021/1/21 20:14
 */
public class KeyedProcessFuncApp {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final long timestamp = Long.parseLong(fields[0]);
                    final String key = fields[1];
                    final String value = fields[2];
                    return Tuple3.of(timestamp, key, value);
                })
                .returns(Types.TUPLE(Types.LONG, Types.STRING, Types.STRING))
                .keyBy(tuple3 -> tuple3.f1)
                .process(new TemperatureKeepsRising(10))
                .print();
        env.execute();
    }

    private static class TemperatureKeepsRising extends KeyedProcessFunction<String, Tuple3<Long, String, String>, String> {
        private final int interval;
        private ValueState<Double> lastTempState;
        private ValueState<Long> timestampState;

        public TemperatureKeepsRising(int interval) {
            this.interval = interval;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            lastTempState = getRuntimeContext().getState(new ValueStateDescriptor<>("lastTemp", Double.class));
            //初始化最新温度值
            lastTempState.update(Double.MIN_VALUE);
            timestampState = getRuntimeContext().getState(new ValueStateDescriptor<>("timestamp", Long.class));
        }

        @Override
        public void processElement(Tuple3<Long, String, String> value, Context ctx, Collector<String> out) throws Exception {

        }
    }
}
