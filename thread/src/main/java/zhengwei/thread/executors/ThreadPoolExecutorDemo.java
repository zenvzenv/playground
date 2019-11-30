package zhengwei.thread.executors;

import java.util.concurrent.*;

/**
 * 线程池ThreadPoolExecutor参数介绍
 * int corePoolSize 核心线程数，即使线程是空闲的，也不会被回收，除非allowCoreThreadTimeOut被设置
 * int maximumPoolSize 线程池中最大的线程容量
 * long keepAliveTime 当线程池中的的活动的线程数量大于核心线程数量且多出来的线程处于空闲状态，那么这么空闲线程的最大空闲时间就由这个参数指定
 * TimeUnit unit 空闲时间的时间单位
 * BlockingQueue<Runnable> workQueue 工作阻塞队列，当一个任务来临时，并不会直接交由线程去执行，会先交给workQueue阻塞队列
 * ThreadFactory threadFactory 创建线程的工厂
 * RejectedExecutionHandler handler 拒绝策略
 *
 * 什么时候会创建非核心线程？
 * 当线程池中的活动线程大于核心线程数，并且工作阻塞队列已经满的时候，线程池会创建非核心线程去处理阻塞队列中的任务，直到线程数到达最大线程数为止
 *
 * 什么时候会触发拒绝策略？
 * 当线程池中的线程数量达到最大线程数量，并且工作阻塞队列已经满的时候，再往线程池中提交任务的时候将会被拒绝策略接管
 *
 * 什么时候会回收线程？
 * 当线程池中的线程数量超过核心线程数量，并且超过的线程的状态是空闲的，那么当空闲了keepAliveTime时间后将会被回收
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/30 12:30
 */
public class ThreadPoolExecutorDemo {
    /**
     * 测试点1：
     * coreSize=1
     * maxSize=2
     * block queue size=1
     * submit 3 task
     * 测试点2：
     * core size=1
     * max size=2
     * block queue size=5
     * submit 7 task
     * 测试点3：
     * core size=1
     * max size=2
     * block queue size=5
     * submit 8 task
     */
    public static void main(String[] args) {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) buildThreadPoolExecutor();
        int activeSize = -1;
        int queueSize = -1;
        while (true) {
            if (activeSize != executorService.getActiveCount() || queueSize != executorService.getQueue().size()) {
                System.out.println("=======================================");
                System.out.println("active thread size -> " + executorService.getActiveCount());
                System.out.println("core thread size -> " + executorService.getCorePoolSize());
                System.out.println("queue size -> " + executorService.getQueue().size());
                System.out.println("max thread size -> " + executorService.getMaximumPoolSize());
                System.out.println("=======================================");
                activeSize = executorService.getActiveCount();
                queueSize = executorService.getQueue().size();
            }
        }
    }

    /**
     * 创建线程池
     *
     * @return 线程池
     */
    private static ExecutorService buildThreadPoolExecutor() {
        ExecutorService executors = new ThreadPoolExecutor(1,
                2,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                (ThreadFactory) Thread::new,
                new ThreadPoolExecutor.AbortPolicy());
        System.out.println("the thread pool create done");
        //提交任务
        executors.execute(() -> ThreadPoolExecutorDemo.sleepSeconds(100));
        executors.execute(() -> ThreadPoolExecutorDemo.sleepSeconds(100));
        executors.execute(() -> ThreadPoolExecutorDemo.sleepSeconds(100));
        executors.execute(() -> ThreadPoolExecutorDemo.sleepSeconds(100));
        return executors;
    }

    private static void sleepSeconds(long second) {
        try {
            System.out.println(Thread.currentThread().getName() + " do something");
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
