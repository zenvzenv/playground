package zhengwei.flink.api.state;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author zhengwei AKA zenv
 * @since 2021/1/16 14:04
 */
public class KeyedState {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String key = fields[0];
                    final int count = Integer.parseInt(fields[1]);
                    return Tuple2.of(key, count);
                })
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(tuple2 -> tuple2.f0)
                .map(new MyKeyMap())
                .print();
        env.execute();
    }

    private static class MyKeyMap extends RichMapFunction<Tuple2<String, Integer>, Integer> {
        //只保留状态的状态
        private ValueState<Integer> keyCountState;
        //list 类型的状态
        private ListState<String> myListState;
        //map 类型的装填
        private MapState<String, Integer> myMapState;


        //状态变量需要在 open 方法中被初始化。
        //open 只会被执行一次，也就是当 MyKeyMap 被初始化的时候
        @Override
        public void open(Configuration parameters) throws Exception {
            keyCountState = getRuntimeContext().getState(new ValueStateDescriptor<>("keyCount", Integer.class));
            myListState = getRuntimeContext().getListState(new ListStateDescriptor<>("myList", String.class));
            myMapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("myMap", String.class, Integer.class));
        }

        @Override
        public Integer map(Tuple2<String, Integer> value) throws Exception {
            int cnt = keyCountState.value();
            cnt++;
            keyCountState.update(cnt);

            final Iterable<String> strings = myListState.get();
            strings.forEach(s -> {});

            return cnt;
        }
    }
}
