package zhengwei.flink.api.join;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/19 9:32
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FactOrder {
    private String goodsId;
    private String goodsName;
    private int count;
    private BigDecimal price;
}
