package zhengwei.flink.api.cep.script;

import org.apache.flink.cep.pattern.conditions.SimpleCondition;

import java.io.Serializable;

public class SimpleConditionTest1 extends SimpleCondition<String> implements Serializable {
    @Override
    public boolean filter(String s) throws Exception {
        return "tmd".equals(s);
    }
}
