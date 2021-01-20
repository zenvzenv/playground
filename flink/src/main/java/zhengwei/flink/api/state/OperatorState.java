package zhengwei.flink.api.state;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author zhengwei AKA zenv
 * @since 2021/1/14 20:16
 */
public class OperatorState {
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
                //定义一个有状态 map，统计每个分区中数据的个数
                .map(new MyCountMapper())
                .print();
        env.execute();
    }

    private static class MyCountMapper implements MapFunction<Tuple2<String, Integer>, Integer>, CheckpointedFunction {
        private transient ListState<Integer> countPer;
        private int localCount;

        @Override
        public Integer map(Tuple2<String, Integer> value) throws Exception {
            return ++localCount;
        }

        //当 flink 做 checkpoint 时会调用这个方法
        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            countPer.clear();
            countPer.add(localCount);
        }

        //当重新构建状态时
        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            final ListStateDescriptor<Integer> descriptor = new ListStateDescriptor<>("localCount", Integer.class);
            countPer = context.getOperatorStateStore().getListState(descriptor);
            countPer.get().forEach(i -> localCount += i);
        }
    }
}
