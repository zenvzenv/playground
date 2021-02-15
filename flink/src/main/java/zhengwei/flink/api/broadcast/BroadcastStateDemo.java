package zhengwei.flink.api.broadcast;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import zhengwei.util.common.DateUtils;

import java.util.Map;

/**
 * 广播变量示例演示
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 17:39
 */
public class BroadcastStateDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        final String url = parameterTool.get("url");
        final String user = parameterTool.get("user");
        final String password = parameterTool.get("password");

        //构建实时的自定义随机数据事件流-数据源源不断产生,量会很大
        final DataStream<Tuple4<String, Long, String, Integer>> eventSource = env.addSource(new EventSource());
        //构建配置流-从MySQL定期查询最新的,数据量较小
        final DataStream<Map<String, Tuple2<String, Integer>>> configSource = env.addSource(new ConfigSource(url, user, password));

        //定义状态描述器-准备将配置流作为状态广播
        final MapStateDescriptor<Void, Map<String, Tuple2<String, Integer>>> descriptor = new MapStateDescriptor<>("config", Types.VOID, Types.MAP(Types.STRING, Types.TUPLE(Types.STRING, Types.INT)));

        //将配置流根据状态描述器广播出去,变成广播状态流
        final BroadcastStream<Map<String, Tuple2<String, Integer>>> broadcast = configSource.broadcast(descriptor);

        //将事件流和广播流进行连接
        final BroadcastConnectedStream<Tuple4<String, Long, String, Integer>, Map<String, Tuple2<String, Integer>>> connect = eventSource.connect(broadcast);
        connect
                .process(new BroadcastProcessFunction<
                                 Tuple4<String, Long, String, Integer>,
                                 Map<String, Tuple2<String, Integer>>,
                                 Tuple6<String, String, String, Integer, String, Integer>
                                 >() {
                             //处理事件流中的元素
                             @Override
                             public void processElement(Tuple4<String, Long, String, Integer> value, ReadOnlyContext ctx, Collector<Tuple6<String, String, String, Integer, String, Integer>> out) throws Exception {
                                 final String userId = value.f0;
                                 //根据状态描述器获取广播状态
                                 final ReadOnlyBroadcastState<Void, Map<String, Tuple2<String, Integer>>> broadcastState = ctx.getBroadcastState(descriptor);
                                 if (null != broadcastState) {
                                     final Map<String, Tuple2<String, Integer>> map = broadcastState.get(null);
                                     if (null != map) {
                                         final Tuple2<String, Integer> tuple2 = map.get(userId);
                                         final String userName = tuple2.f0;
                                         final Integer userAge = tuple2.f1;
                                         out.collect(Tuple6.of(userId, DateUtils.longToStrFormat(value.f1 / 1_000L), value.f2, value.f3, userName, userAge));
                                     }
                                 }
                             }

                             //处理广播流中的元素
                             @Override
                             public void processBroadcastElement(Map<String, Tuple2<String, Integer>> value, Context ctx, Collector<Tuple6<String, String, String, Integer, String, Integer>> out) throws Exception {
                                 //value就是MySQLSource中每隔一段时间获取到的最新的map数据
                                 //先根据状态描述器获取历史的广播状态
                                 final BroadcastState<Void, Map<String, Tuple2<String, Integer>>> broadcastState = ctx.getBroadcastState(descriptor);
                                 //再清空历史状态数据
                                 broadcastState.clear();
                                 //最后将最新的广播流数据放到state中（更新状态数据）
                                 broadcastState.put(null, value);
                             }
                         }
                )
                .print();

        env.execute();
    }
}
