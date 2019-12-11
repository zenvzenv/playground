package zhengwei.thread.executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * ThreadPoolExecutor在初始化时是不会创建线程的，
 * 只有接到任务的时候，才会去创建线程，直到达到max thread为止
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/11 20:34
 */
public class ExecutorServiceDemo3 {
    public static void main(String[] args) {
        testAllowCoreThreadTimeout();
    }

    private static void testAllowCoreThreadTimeout() {
        final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        service.setKeepAliveTime(5, TimeUnit.SECONDS);
        //允许线程池可以回收core thread
        service.allowCoreThreadTimeOut(true);
        IntStream.rangeClosed(0, 5).forEach(i -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    //remove
    //prestartCoreThread
    //prestartAllThread
    //beforeExecute afterExecute
}
