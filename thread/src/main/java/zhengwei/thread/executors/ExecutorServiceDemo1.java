package zhengwei.thread.executors;

import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/11 12:51
 */
public class ExecutorServiceDemo1 {
    public static void main(String[] args) {
//        isShutdown();
//        isTerminated();
//        executeHasException();
        threadException();
    }

    /**
     * {@link ExecutorService#isShutdown()}
     * {@link ExecutorService#shutdown()}}
     */
    private static void isShutdown() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(executorService.isShutdown());
        //非阻塞
        executorService.shutdown();
        //调用shutdown方法之后将不能够再去提交任务，会被拒绝策略拒绝
        System.out.println(executorService.isShutdown());
    }

    /**
     * {@link ExecutorService#isTerminated()}
     * {@link ThreadPoolExecutor#isTerminating()}
     */
    private static void isTerminated() {
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(executorService.isShutdown());
        System.out.println(executorService.isTerminated());
        System.out.println(((ThreadPoolExecutor) executorService).isTerminating());
        executorService.shutdown();
        //只要调用了shutdown方法之后，isShutdown就为true
        System.out.println(executorService.isShutdown());
        //只有等到线程池真的被终止的时候，isTerminated才为true
        System.out.println(executorService.isTerminated());
        //当调用了shutdown之后，线程池就处于正在关闭的状态，即isTermination为true
        System.out.println(((ThreadPoolExecutor) executorService).isTerminating());
    }

    private static void executeHasException() {
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.rangeClosed(1, 10).forEach(i -> executorService.execute(() -> {
            int temp = 1 / 0;
        }));
        executorService.shutdown();
    }

    private static final class MyThreadFactory implements ThreadFactory {
        private final int number;

        public MyThreadFactory(int number) {
            this.number = number;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "MyThreadFactory - [ " + number + " ]");
            t.setUncaughtExceptionHandler((thread, cause) -> {
                System.out.println("The thread - " + thread.getName() + " has been failed.");
                cause.printStackTrace();
            });
            return t;
        }
    }

    /**
     * 需要在线程里面处理异常的时候，最好还是在线程中定义对于的处理异常的方法
     */
    private static abstract class MyThread implements Runnable{
        @Override
        public void run() {
            try {
                doInit();
                doExecute();
            } catch (Exception e){
                doFailed();
            }
        }

        protected abstract void doExecute();

        protected abstract void doFailed();

        protected abstract void doInit();
    }

    private static void threadException() {
        Thread thread = new Thread(() -> {
            int temp = 1 / 0;
        }, "TTT");
        thread.setUncaughtExceptionHandler((t, cause) -> {
            System.out.println("-----");
            //在setUncaughtExceptionHandler方法中，不能够调用thread的start方法，否则会报错
//            t.start();
        });
        thread.start();
    }
}
