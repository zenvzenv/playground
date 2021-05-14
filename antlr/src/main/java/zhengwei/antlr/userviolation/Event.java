package zhengwei.antlr.userviolation;

import lombok.*;

import java.sql.Timestamp;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/11 9:55
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private String userId;
    private String verb;
    private String noun;
    private Timestamp ts = new Timestamp(System.currentTimeMillis());

    public Event(String userId, String verb, String noun) {
        this.userId = userId;
        this.verb = verb;
        this.noun = noun;
    }
}
