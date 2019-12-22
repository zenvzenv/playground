package zhengwei.thread.executors;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/21 14:45
 */
public class SchedulerServiceDemo1 {
    public static void main(String[] args) throws InterruptedException {
//        testSchedule();
//        testSchedulePolicy();
//        testSchedulePolicy2();
        testSetContinueExistingPeriodicTasksAfterShutdownPolicy();
    }

    private static void testSchedule() {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        final ScheduledFuture<?> f = executor.schedule(() -> System.out.println("I will be invoked"), 2, TimeUnit.SECONDS);
        //cancel之后不会执行
        f.cancel(true);
    }

    /**
     * {@link java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit)}
     * ScheduledThreadPoolExecutor采用的是和Timer一样的执行策略，等到上一个任务执行完毕之后才会去执行下一个任务
     * crontab，quartz采用的是周期优先的策略，周期到了就会
     */
    private static void testSchedulePolicy() throws InterruptedException {
        //任务会被放进DelayWorkQueue中，被哪个线程拿到执行是不一定的
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        final ScheduledFuture<?> f = executor.schedule(() -> System.out.println("I will be invoked"), 2, TimeUnit.SECONDS);
        final AtomicLong interval = new AtomicLong(0);
        for (int i = 0; i < 5; i++) {
            //在上一个任务执行结束时，如果已经到了固定周期的话，将会立即执行下一次任务
            executor.scheduleAtFixedRate(new MyRunnable(i, interval), 0, 2, TimeUnit.SECONDS);
        }
        //ScheduledThreadPoolExecutor并没有创建新的线程，而是将任务放入到了DelayWorkQueue中，由活动线程去获取任务并执行
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("now thread pool has " + executor.getActiveCount() + " threads");
        }
    }

    /**
     * {@link java.util.concurrent.ScheduledThreadPoolExecutor#setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean)}
     * true -> 在线程池shutdown之后，里面的定时周期任务将会继续执行
     * false -> 在线程池shutdown之后，里面的定时周期任务将不会继续执行
     * 默认为false
     */
    private static void testSetContinueExistingPeriodicTasksAfterShutdownPolicy() throws InterruptedException {
        //任务会被放进DelayWorkQueue中，被哪个线程拿到执行是不一定的
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        final AtomicLong interval = new AtomicLong(0);
        for (int i = 0; i < 5; i++) {
            //在上一个任务执行结束时，如果已经到了固定周期的话，将会立即执行下一次任务
            executor.scheduleAtFixedRate(new MyRunnable(i, interval), 0, 2, TimeUnit.SECONDS);
        }
        //ScheduledThreadPoolExecutor并没有创建新的线程，而是将任务放入到了DelayWorkQueue中，由活动线程去获取任务并执行
        TimeUnit.SECONDS.sleep(1);
        executor.shutdown();
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("now thread pool has " + executor.getActiveCount() + " threads");
        }
    }

    /**
     * {@link java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit)}
     * 在上一次任务执行结束的基础上延迟固定时间再去执行任务
     */
    private static void testSchedulePolicy2() throws InterruptedException {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        final AtomicLong interval = new AtomicLong(0);
        for (int i = 0; i < 5; i++) {
            //在上一个任务执行结束之后，再延迟delay时间再去执行接下来的任务
            executor.scheduleWithFixedDelay(()-> System.out.println(new Date()),1,2,TimeUnit.SECONDS);
        }
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("now thread pool has " + executor.getActiveCount() + " threads");
        }
    }

    /**
     * {@link ScheduledThreadPoolExecutor#setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean)}
     * true -> 在线程池shutdown之后，里面的固定延迟任务将会继续执行
     * false -> 在线程池shutdown之后，里面的固定延迟任务将会不会继续执行
     * 默认为true
     */
    private static void testSetExecuteExistingDelayedTasksAfterShutdownPolicy() throws InterruptedException {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        final AtomicLong interval = new AtomicLong(0);
        for (int i = 0; i < 5; i++) {
            //在上一个任务执行结束之后，再延迟delay时间再去执行接下来的任务
            executor.scheduleWithFixedDelay(()-> System.out.println(new Date()),1,2,TimeUnit.SECONDS);
        }
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("now thread pool has " + executor.getActiveCount() + " threads");
        }
    }

    private static class MyRunnable implements Runnable {
        private final int number;
        private final AtomicLong interval;

        public MyRunnable(int number, AtomicLong interval) {
            this.number = number;
            this.interval = interval;
        }

        @Override
        public void run() {
            long currentTimeMillis = System.currentTimeMillis();
            System.out.println(currentTimeMillis);
            if (0 == interval.get()) {
                System.out.println("the task [" + number + "] time trigger time is " + currentTimeMillis);
            } else {
                System.out.println("the  task " + number + " actually spend time is " + (System.currentTimeMillis() - interval.get()));
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            interval.set(currentTimeMillis);
        }
    }
}
