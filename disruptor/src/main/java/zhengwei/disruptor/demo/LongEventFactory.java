package zhengwei.disruptor.demo;

import com.lmax.disruptor.EventFactory;
import com.mysql.cj.result.LongValueFactory;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
