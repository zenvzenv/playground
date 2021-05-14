package zhengwei.antlr.userviolation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/11 9:19
 */
@Setter
@Getter
@AllArgsConstructor
public class WindowTime {
    private int time;
    private String timeUnit;
}
