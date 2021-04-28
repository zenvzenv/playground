package zhengwei.flink.api.join;

import lombok.*;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/19 9:14
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {
    private String orderId;
    private String goodsId;
    private int count;
    private long timestamp;
}
