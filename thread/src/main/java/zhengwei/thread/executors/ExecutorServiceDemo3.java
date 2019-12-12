package zhengwei.thread.executors;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * ThreadPoolExecutor在初始化时是不会创建线程的，
 * 只有接到任务的时候，才会去创建线程，直到达到max thread为止
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/11 20:34
 */
public class ExecutorServiceDemo3 {
    public static void main(String[] args) throws InterruptedException {
//        testAllowCoreThreadTimeout();
//        testRemove();
        testPreStartCoreThread();
//        testPreStartAllThread();
//        aroundThreadExecute();
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

    /**
     * 删除work queue中的一个runnable，如果已经执行的话将不会被删除
     * {@link ThreadPoolExecutor#remove(Runnable r)}
     */
    private static void testRemove() throws InterruptedException {
        final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    System.out.println("i come from 1");
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        TimeUnit.SECONDS.sleep(1);
        Runnable r = () -> System.out.println("i will never be executed");
        executorService.execute(r);
        //移除runnable
        executorService.remove(r);
    }

    //prestartCoreThread

    /**
     * {@link ThreadPoolExecutor#prestartCoreThread()}
     */
    private static void testPreStartCoreThread() throws InterruptedException {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                2,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1)
        );
        final ThreadPoolExecutor executor1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        boolean b;
        b = executor.prestartCoreThread();
        System.out.println(b + "--" + executor.getActiveCount());
        System.out.println("==============");
        TimeUnit.SECONDS.sleep(1);
        b = executor.prestartCoreThread();
        System.out.println(b + "--" + executor.getActiveCount());
        System.out.println("==============");
        TimeUnit.SECONDS.sleep(1);
        b = executor.prestartCoreThread();
        System.out.println(b + "--" + executor.getActiveCount());
        System.out.println("==============");
    }

    //prestartAllThread

    /**
     * {@link ThreadPoolExecutor#prestartAllCoreThreads()}
     */
    private static void testPreStartAllThread() {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                2,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1)
        );
        int b;
        b = executor.prestartAllCoreThreads();
        System.out.println(b + "--" + executor.getActiveCount());
        System.out.println("==============");
        b = executor.prestartAllCoreThreads();
        System.out.println(b + "--" + executor.getActiveCount());
        System.out.println("==============");
        b = executor.prestartAllCoreThreads();
        System.out.println(b + "--" + executor.getActiveCount());
        System.out.println("==============");
    }

    //beforeExecute afterExecute
    private static void aroundThreadExecute() {
        final MyThreadPoolExecutor myThreadPoolExecutor = new MyThreadPoolExecutor(
                1,
                2,
                5,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        IntStream.rangeClosed(1, 20)
                .forEach(i -> myThreadPoolExecutor.execute(new MyRunnable(i)));
    }

    /**
     * {@link ThreadPoolExecutor#beforeExecute(Thread t, Runnable r)}
     * {@link ThreadPoolExecutor#afterExecute(Runnable r, Throwable t)}
     */
    private static class MyThreadPoolExecutor extends ThreadPoolExecutor {

        public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            System.out.println("will execute runnable number is  " + ((MyRunnable) r).getNumber());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            //无异常情况
            if (Objects.isNull(t)) {
                System.out.println("runnable execute normal");
            } else {//有异常情况
//                t.printStackTrace();
                System.out.println("runnable execute exception");
            }
        }
    }

    @Data
    private static class MyRunnable implements Runnable {
        private final int number;

        public MyRunnable(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            System.out.println("my number" + number);
            if (number % 2 == 0) {
                int temp = 1 / 0;
            }
        }
    }
}
