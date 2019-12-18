package zhengwei.thread.future;

import java.util.concurrent.*;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/18 12:50
 */
public class FutureDemo1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        testGet();
    }

    /**
     * @throws InterruptedException future的get方法将抛出InterruptedException
     */
    private static void testGet() throws ExecutionException, InterruptedException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Future<?> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        final Thread callerThread = Thread.currentThread();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                callerThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        /*
         * 主调线程会阻塞住，而不是future阻塞住
         * 如果打断了主调线程的话，那么将不会获取到结果
         * 但是线程池内的线程将不会收到影响
         */
        final Object result = future.get();
        System.out.println(result);
    }

    private static void testIsDone() {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<?> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
}
