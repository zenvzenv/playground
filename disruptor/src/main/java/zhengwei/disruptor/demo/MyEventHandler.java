package zhengwei.disruptor.demo;

import com.lmax.disruptor.EventHandler;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6
 */
public class MyEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent o, long l, boolean b) throws Exception {
        System.out.println();
    }
}
