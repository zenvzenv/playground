package zhengwei.flink.action.douele;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import zhengwei.util.common.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/15 9:38
 */
public class WindowResult implements WindowFunction<Double, Category, String, TimeWindow> {
    @Override
    public void apply(String s, TimeWindow window, Iterable<Double> input, Collector<Category> out) {
        double totalPrice = 0.0;
        final Category category = new Category();
        for (Double price : input) {
            totalPrice += price;
        }
        totalPrice = new BigDecimal(totalPrice).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        category.setTotalPrice(totalPrice);
        category.setCategory(s);
        category.setDateTime(DateUtils.longToStrFormat(System.currentTimeMillis() / 1_000L));
        out.collect(category);
    }
}
