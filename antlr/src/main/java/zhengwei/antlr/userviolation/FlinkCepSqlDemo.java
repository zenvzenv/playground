package zhengwei.antlr.userviolation;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.table.api.Expressions.$;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/12 10:33
 */
public class FlinkCepSqlDemo {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        final EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        final StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env, settings);
        final DataStream<Event> input = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    final String[] fields = line.split("[ ]");
                    final String userId = fields[0];
                    final String verb = fields[1];
                    final String noun = fields[2];
                    return new Event(userId, verb, noun);
                });
        tabEnv.createTemporaryView("eventTable", input, $("userId"), $("verb"), $("noun"), $("ts").rowtime());

//        tabEnv.c


        String sql = "select *\n" +
                "from eventTable\n" +
                "MATCH_RECOGNIZE(\n" +
                "\tPARTITION BY userId\n" +
                "\tORDER BY ts\n" +
                "\tMEASURES\n" +
                "\t\tLAST(e.ts) as `tso`\n" +
                "\tONE ROW PER MATCH\n" +
                "\tAFTER MATCH SKIP TO NEXT ROW\n" +
                "\tPATTERN (e) WITHIN INTERVAL '10' MINUTE\n" +
                "\tDEFINE\n" +
                "\t\te as e.verb = 'burn' and e.noun = 'CD'\n" +
                ")";

        final Table table = tabEnv.sqlQuery(sql);
        tabEnv.toAppendStream(table,Event.class).print("sql result ");
    }
}
