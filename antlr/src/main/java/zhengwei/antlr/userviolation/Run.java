package zhengwei.antlr.userviolation;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/11 8:52
 */
public class Run {
    public static void main(String[] args) throws Exception {
        //可指定时间范围，一个时间点到另一个时间点的范围内
        //可指定时间点，即使用 range 模式，指定开始时间和结束时间一样即可
        //可对事件发生指定阈值
        //可以指定时间窗口，但不可对时间窗口的时间指定范围
        //原型阶段仅支持对单个行为进行检测，例如：登录系统，刻录光盘……
//        final CodePointCharStream input = CharStreams.fromString("user range 20210511083000 20210511120000 burn CD 3");
        final CodePointCharStream input = CharStreams.fromString("user windowTime 3 HOURS burn CD 3");
        final UserViolationRuleLexer lexer = new UserViolationRuleLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final UserViolationRuleParser parser = new UserViolationRuleParser(tokens);
        final ParseTree rule = parser.rule();
        final TestUserViolationWithListener listener = new TestUserViolationWithListener();
        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, rule);
        final Pattern<Event, Event> pattern = listener.getPattern();
        System.out.println(pattern);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final DataStream<Event> source = env.socketTextStream("localhost", 8888)
                .filter(line -> !"".equals(line))
                .map(line -> {
                    final String[] fields = line.split("[ ]");
                    final String userId = fields[0];
                    final String verb = fields[1];
                    final String noun = fields[2];
                    return new Event(userId, verb, noun);
                })
                .keyBy(Event::getUserId)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forMonotonousTimestamps()
                                .withTimestampAssigner((Event e, long ts) -> System.currentTimeMillis())
                );

        source.print("source ");
        final PatternStream<Event> patternStream = CEP.pattern(source, pattern);
        patternStream.select(new PatternSelectFunction<Event, Object>() {
            @Override
            public Object select(Map<String, List<Event>> pattern) {
                return pattern.get("rule");
            }
        }).print("result");

        env.execute();
    }
}
