package zhengwei.flink.action.autopraise;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Iterator;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/15 17:19
 */
public class TimerProcessFunc extends KeyedProcessFunction<String, Tuple3<String, String, Long>, Object> {
    private final long interval;

    public TimerProcessFunc(long interval) {
        this.interval = interval;
    }

    //key是订单号，value是订单完成时间
    //定义一个状态用来记录订单信息
    private MapState<String, Long> mapState;

    //初始化 MapState
    @Override
    public void open(Configuration parameters) throws Exception {
        mapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("order", String.class, Long.class));
    }

    @Override
    public void processElement(Tuple3<String, String, Long> value, Context ctx, Collector<Object> out) throws Exception {
        mapState.put(value.f1, value.f2);
        //注册一个定时器来定时查看订单是否评价过了，如果没有就自动好评
        //触发时间是订单生成时间 + interval 之后
        //在注册定时任务时需要注意时间语义
        //如果在前面没有注册时间时间字段，那么要使用 ProcessingTimeTimer
//        ctx.timerService().registerProcessingTimeTimer(value.f2 + interval);
        //如果注册了事件事件就是用 EventTimeTimer
        ctx.timerService().registerEventTimeTimer(value.f2 + interval);
    }

    //定时器逻辑
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Object> out) throws Exception {
        final Iterator<Map.Entry<String, Long>> iterator = mapState.iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Long> order = iterator.next();
            final String orderId = order.getKey();
            if ((orderId.hashCode() & 1) == 0) {
                System.out.println("订单(orderId: " + orderId + " )在 " + interval + "毫秒时间内已经评价，不做处理");
            } else {
                System.out.println("订单(orderId: " + orderId + " )在 " + interval + "毫秒时间内已经评价，系统自动给了默认好评");
            }
            iterator.remove();
            mapState.remove(orderId);
        }
    }
}
