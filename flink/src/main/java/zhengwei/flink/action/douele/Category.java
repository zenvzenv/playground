package zhengwei.flink.action.douele;

import lombok.*;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/15 9:42
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Category {
    private String category;
    private double totalPrice;
    private String dateTime;
}
