package zhengwei.antlr.userviolation;

import lombok.*;

import java.sql.Timestamp;
import java.util.Objects;

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
    private int count = 1;

    public Event(String userId, String verb, String noun) {
        this.userId = userId;
        this.verb = verb;
        this.noun = noun;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return userId.equals(event.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
