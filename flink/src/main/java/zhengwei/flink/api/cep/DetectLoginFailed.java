package zhengwei.flink.api.cep;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 检测登录异常情况，在指定时间内登录失败超过阈值则输出报警
 */
public class DetectLoginFailed {
    @Data
    @AllArgsConstructor
    final static class LoginEvent {
        private String userId;
        private String status;
        private long ts;
    }

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final SingleOutputStreamOperator<LoginEvent> source = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[,]");
                    final String userId = fields[0];
                    final String status = fields[1];
                    return new LoginEvent(userId, status, System.currentTimeMillis());
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                .withTimestampAssigner((LoginEvent e, long recordTime) -> e.getTs())
                );

        source.print();

        final Pattern<LoginEvent, LoginEvent> pattern = Pattern.<LoginEvent>begin("failEvents")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent value) {
                        return value.status.equals("failed");
                    }
                })
                .times(3)//若不带 consecutive 的话，则默认不是严格临近
                .consecutive()//表示严格临近
                .within(Time.of(10, TimeUnit.SECONDS));

        final PatternStream<LoginEvent> resultPattern = CEP.pattern(source, pattern);

        resultPattern.select(new PatternSelectFunction<LoginEvent, String>() {
            /**
             * key - pattern 的名字
             * value - pattern 所拦截下来的事件
             * @param map 拦截下来的结果
             * @return 处理结果
             */
            @Override
            public String select(Map<String, List<LoginEvent>> map) throws Exception {
                final List<LoginEvent> failEvents = map.get("failEvents");
                final LoginEvent loginEvent = failEvents.get(0);
                return "失败过多的用户 id 是：" + loginEvent.userId + "，失败了" + failEvents.size() + "次";
            }
        }).print();

        env.execute();
    }
}
