package zhengwei.thread.threadpool;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/28 19:02
 */
public interface Executor {
    void execute(Runnable task);
}
