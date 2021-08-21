package zhengwei.flink.api.state;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/14 11:13
 */
public class KeyedStateWithRule {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    return Tuple2.of(fields[0], fields[1]);
                })
                .returns(Types.TUPLE(Types.STRING, Types.STRING))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Tuple2<String, String>>forMonotonousTimestamps()
                                .withTimestampAssigner((Tuple2<String, String> s, long ts) -> System.currentTimeMillis())
                ).keyBy(t2 -> t2.f0)
                .window(TumblingEventTimeWindows.of(Time.of(5, TimeUnit.SECONDS)))
                /*.process()*/;
    }

    private static final class MyProcessFunction extends KeyedProcessFunction<String, Tuple2<String, String>, Tuple2<String, String>> {
        private MapState<String, Object> mapState;

        @Override
        public void open(Configuration parameters) throws Exception {
            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<String, Object>("set", String.class, Object.class));
        }

        @Override
        public void processElement(Tuple2<String, String> value, Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
            if (!mapState.contains(value.f1)) {
                mapState.put(value.f1, null);
            }
        }
    }
}
