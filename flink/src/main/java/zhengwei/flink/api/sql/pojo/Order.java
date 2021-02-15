package zhengwei.flink.api.sql.pojo;

import lombok.*;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/14 12:09
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {
    private String name;
    private int num;
    private double amount;
    private long ordTime;

    public Order(String name, int num, double amount) {
        this.name = name;
        this.num = num;
        this.amount = amount;
    }
}
