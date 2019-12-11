package zhengwei.thread.executors;

import java.util.concurrent.*;

/**
 * 四大拒绝策略
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/11 19:54
 */
public class ExecutorServiceDemo2 {
    private static final ThreadPoolExecutor service = new ThreadPoolExecutor(1,
            2,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1),
            (ThreadFactory) Thread::new);

    public static void main(String[] args) throws InterruptedException {
//        testAbortPolicy();
//        testDiscardPolicy();
//        testCallerRunsPolicy();
        testDiscardOldestPolicy();
    }

    /**
     * AbortPolicy拒绝策略，直接将不能够处理的任务拒绝掉并抛出异常
     * {@link ThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler)}
     */
    private static void testAbortPolicy() throws InterruptedException {
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        runTest();
    }

    private static void runTest() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            service.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " execute");
                    TimeUnit.SECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        TimeUnit.SECONDS.sleep(1);
        service.execute(() -> System.out.println("no4. thread executor"));
        service.shutdown();
    }

    /**
     * DiscardPolicy拒绝策略，直接拒绝不能够处理的任务，并且没有任何响应，谨慎使用
     * {@link ThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler)}
     */
    private static void testDiscardPolicy() throws InterruptedException {
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        for (int i = 0; i < 3; i++) {
            service.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " execute");
                    TimeUnit.SECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        TimeUnit.SECONDS.sleep(1);
        service.execute(() -> System.out.println("no4. thread executor"));
        service.shutdown();
    }

    /**
     * CallerRunsPolicy拒绝策略，拒绝不能够处理的任务并将任务交由调用提交任务的线程自己去执行
     * {@link ThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler)}
     */
    private static void testCallerRunsPolicy() throws InterruptedException {
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < 3; i++) {
            service.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " execute");
                    TimeUnit.SECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        TimeUnit.SECONDS.sleep(1);
        service.execute(() -> {
            System.out.println("no4. thread executor");
            System.out.println(Thread.currentThread().getName());
        });
        service.shutdown();
    }

    /**
     * DiscardOldestPolicy拒绝策略，会把队列中还没有处理的最老的任务给清删掉，然后把新的任务加到任务队列中去
     * {@link ThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler)}
     */
    private static void testDiscardOldestPolicy() throws InterruptedException {
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < 3; i++) {
            service.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println(Thread.currentThread().getName() + " from 1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        TimeUnit.SECONDS.sleep(1);
        service.execute(() -> {
            System.out.println(Thread.currentThread().getName() + " from 2");
        });
        service.shutdown();
    }
}
