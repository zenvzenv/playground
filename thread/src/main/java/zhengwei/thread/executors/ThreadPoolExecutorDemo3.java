package zhengwei.thread.executors;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 验证下ThreadPoolExecutor的线程创建机制
 * ThreadPoolExecutor创建线程是懒创建的，只有需要用的时候才会去创建线程，之后超出核心线程的线程将会被回收直到线程数为核心线程数量
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/3 13:47
 */
public class ThreadPoolExecutorDemo3 {
    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                10,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10)
        );
        System.out.println("active count -> " + threadPoolExecutor.getActiveCount() + ", core size -> " + threadPoolExecutor.getCorePoolSize());
        IntStream.rangeClosed(1, 20)
                .forEach(i -> threadPoolExecutor.submit(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + "---active count---" + threadPoolExecutor.getActiveCount());
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
        System.out.println("active count -> " + threadPoolExecutor.getActiveCount());
        TimeUnit.MINUTES.sleep(4);
    }
}
