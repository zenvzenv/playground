package zhengwei.thread.threadpool;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/28 19:01
 */
public interface RejectPolicy {
    void reject(Runnable task);
}
