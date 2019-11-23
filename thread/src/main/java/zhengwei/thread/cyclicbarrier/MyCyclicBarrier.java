package zhengwei.thread.cyclicbarrier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 利用CountDownLatch实现一个CyclicBarrier
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/14 20:35
 */
public class MyCyclicBarrier extends CountDownLatch {
    private final Runnable runnable;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count    the number of times {@link #countDown} must be invoked
     *                 before threads can pass through {@link #await}
     * @param runnable call back thread
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public MyCyclicBarrier(int count, Runnable runnable) {
        super(count);
        this.runnable = runnable;
    }

    public void countDown() {
        super.countDown();
        if (getCount() == 0) {
            this.runnable.run();
        }
    }

    public static void main(String[] args) {
        final MyCyclicBarrier myCyclicBarrier = new MyCyclicBarrier(2, () -> System.out.println("all of thread are finished"));
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                myCyclicBarrier.countDown();
                System.out.println(Thread.currentThread().getName() + " finished work");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                myCyclicBarrier.countDown();
                System.out.println(Thread.currentThread().getName() + " finished work");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
