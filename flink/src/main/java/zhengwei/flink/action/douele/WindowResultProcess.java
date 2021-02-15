package zhengwei.flink.action.douele;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/15 11:37
 */
public class WindowResultProcess extends ProcessWindowFunction<Category, Object, String, TimeWindow> {
    @Override
    public void process(String dateTime, Context context, Iterable<Category> elements, Collector<Object> out) {
        final Queue<Category> queue = new PriorityQueue<>(3, (i1, i2) -> (int) (i1.getTotalPrice() - i2.getTotalPrice()));
        double totalPrice = 0.0d;
        double roundPrice = 0.0d;
        for (Category category : elements) {
            totalPrice += category.getTotalPrice();
            if (queue.size() < 3) {
                queue.add(category);
            } else {
                final Category top = queue.peek();
                if (category.getTotalPrice() > top.getTotalPrice()) {
                    queue.poll();
                    queue.add(category);
                }
            }
        }
    }
}
