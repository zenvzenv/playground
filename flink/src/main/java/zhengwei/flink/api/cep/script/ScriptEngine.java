package zhengwei.flink.api.cep.script;

import org.apache.flink.cep.pattern.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngine {
    public static <T> Pattern<T, T> getPattern(String text, String name) throws ScriptException, NoSuchMethodException {
        ScriptEngineManager factory = new ScriptEngineManager();
        javax.script.ScriptEngine engine = factory.getEngineByName("groovy");
        System.out.println(engine.toString());
        engine.eval(text);
        Pattern<T, T> pattern = (Pattern<T, T>) ((Invocable) engine).invokeFunction(name);
        return pattern;
    }
}