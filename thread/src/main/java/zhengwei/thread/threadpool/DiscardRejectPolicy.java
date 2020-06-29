package zhengwei.thread.threadpool;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/28 19:25
 */
public class DiscardRejectPolicy implements RejectPolicy {
    @Override
    public void reject(Runnable task) {
        System.out.println(task + " is rejected");
    }
}
