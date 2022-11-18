package zhengwei.flink.api.cep.script;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

/**
 * 局限性：
 *  1. 泛型无法确
 *  2. 需要实现导入所有的类引用
 *  3. 需要事先知道生成 pattern 的方法名
 *  4. pattern 的可配置化需要
 */
public class Run {
    public static void main(String[] args) throws Exception {
        Pattern<String, String> pattern = ScriptEngine.getPattern(
                "import org.apache.flink.cep.pattern.Pattern\n" +
                        "import org.apache.flink.streaming.api.windowing.time.Time\n" +
                        "import zhengwei.flink.api.cep.script.SimpleConditionTest1\n" +
                        "def getPattern(){\n" +
                        "    SimpleConditionTest1 simpleWhere = new SimpleConditionTest1();\n" +
                        "    return Pattern.<String>begin(\"start\")\n" +
                        "            .where(simpleWhere)\n" +
                        "            .times(5)\n" +
                        "            .within(Time.seconds(10))\n" +
                        "}",
                "getPattern");

        System.out.println(pattern);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final SingleOutputStreamOperator<String> source = env
                .socketTextStream("localhost", 8888)
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<String>forMonotonousTimestamps()
                        .withTimestampAssigner((String s, long ts) -> System.currentTimeMillis())
                );

        final PatternStream<String> patternStream = CEP.pattern(source, pattern);

        patternStream.select(new PatternSelectFunction<String, String>() {
            @Override
            public String select(Map<String, List<String>> map) throws Exception {
                return map.get("start").toString();
            }
        }).print();

        env.execute();
    }
}
