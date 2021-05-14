package zhengwei.antlr.userviolation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/11 9:30
 */
@Setter
@Getter
@AllArgsConstructor
public class DoSomething extends SimpleCondition<Event> {
    private String verb;
    private String noun;

    @Override
    public boolean filter(Event event) {
        return event.getVerb().equals(verb) && event.getNoun().equals(noun);
    }
}
