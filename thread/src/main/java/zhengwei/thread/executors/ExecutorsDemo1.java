package zhengwei.thread.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/3 12:38
 */
public class ExecutorsDemo1 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
    }
}
