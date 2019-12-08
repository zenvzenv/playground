package zhengwei.thread.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/3 12:38
 */
public class ExecutorsDemo1 {
    public static void main(String[] args) {
//        useCachedThread();
//        useFixedThreadPool();
        useSignalThreadPool();
    }

    /**
     * These pools will typically improve the performance of programs that execute many short-lived asynchronous tasks
     * cached thread pool适用于那些很多生命周期较短的线程的场景
     * <p>
     * 所使用的ThreadPoolExecutor方法如下：
     * ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
     * core thread size -> 0
     * max thread size -> Integer.MAX_VALUE
     * alive time -> 60s
     * work queue -> SynchronousQueue ->此队列为阻塞队列，容量为1，当且经当一个线程从队列中把任务取走之后，另一个线程才会去再放入一个任务
     * <p>
     * newCachedThreadPool刚开始是不会创建线程的，来一个任务创建一个线程，如果之前任务一直都没有处理完毕，那么就会一直创建线程知道Integer的最大值为止
     * 所以如果thread执行的任务太长的话，会导致线程池中的线程池过多，所以不适合那种执行时间很长的任务，适合执行时间短而多的任务
     * 当线程池空闲时，因为不存在核心线程，那么所有线程存活的最大时间为60秒，最后将没有存活线程
     */
    private static void useCachedThread() {
        final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        IntStream.rangeClosed(1, 100)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " [ " + i + " ] ");
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
        System.out.println(executorService.getActiveCount());
    }

    /**
     * At any point, at most threads will be active processing tasks.
     * 任何时刻，线程池中线程不变，都是指定的个数
     * <p>
     * new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
     * core thread size -> nThreads
     * max thread size -> nThreads
     * alive time -> 0s
     * work queue -> LinkedBlockingQueue -> 此阻塞队列默认可以存放Integer.MAX_VALUE个任务
     * <p>
     * 比较常用的线程池，线程数两固定，因为核心线程和最大线程数一样，所以线程池中的线程都是核心线程，不会被回收
     * 工作队列则可以结构Integer.MAX_VALUE个任务
     */
    private static void useFixedThreadPool() {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        IntStream.rangeClosed(1, 20)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " [ " + i + " ] active count -> " + executorService.getActiveCount());
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
    }

    /**
     * 线程池中自始至终只会有一个线程
     *
     * new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
     *
     * core thread size -> 1
     * max thread size -> 1
     * alive time ->0s
     * work queue -> LinkedBlockingQueue -> 最多可接受Integer.MAX_VALUE个任务
     *
     * newSignalThreadPool与普通的Thread的区别
     *  1. 普通的Thread在执行完自己的业务逻辑之后生命周期就结束了，但是SignalThreadPool中的线程将会一直存在
     *  2. 普通的Thread不能够存储任务，执行完了就执行完了，下次再去执行的时候要再去创建Thread，SignalThreadPool中的线程会去work queue中去获取任务执行
     */
    private static void useSignalThreadPool() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        IntStream.rangeClosed(1, 20)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " start working");
                        TimeUnit.SECONDS.sleep(10);
                        System.out.println(Thread.currentThread().getName() + " end working");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
    }
}
