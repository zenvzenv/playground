package zhengwei.flink.api.join;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/19 9:13
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Goods {
    private String goodsId;
    private String goodsName;
    private BigDecimal price;

    public static final List<Goods> GOODS_LIST = new ArrayList<>();
    public static final Random random = new Random();

    static {
        GOODS_LIST.add(new Goods("1", "小米12", new BigDecimal(4890)));
        GOODS_LIST.add(new Goods("2", "iphone12", new BigDecimal(12000)));
        GOODS_LIST.add(new Goods("3", "MacBookPro", new BigDecimal(15000)));
        GOODS_LIST.add(new Goods("4", "ThinkPad X1", new BigDecimal(9800)));
        GOODS_LIST.add(new Goods("5", "MeiZu One", new BigDecimal(3200)));
        GOODS_LIST.add(new Goods("6", "Mate 40", new BigDecimal(6500)));
    }

    public static Goods randomGoods() {
        return GOODS_LIST.get(random.nextInt(GOODS_LIST.size()));
    }
}
