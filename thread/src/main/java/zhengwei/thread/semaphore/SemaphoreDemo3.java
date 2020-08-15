package zhengwei.thread.semaphore;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/19 19:48
 */
public class SemaphoreDemo3 {
    @Test
    void test() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(5);
        Thread t1 = new Thread(() -> {
            //一次性拿走所有的许可证
            int permits = semaphore.drainPermits();
            try {
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release(permits);
            }
        }, "T1");
        t1.start();
        Thread t2 = new Thread(() -> {
            System.out.println(semaphore.availablePermits());
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }, "T2");
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        //是否有在等待的线程
        System.out.println(semaphore.hasQueuedThreads());
    }

    static class MySemaphore extends Semaphore {

        public MySemaphore(int permits) {
            super(permits);
        }

        /**
         * 获取正在等待的所有线程
         *
         * @return 正在等待的线程
         */
        public Collection<Thread> getWaitingThread() {
            return super.getQueuedThreads();
        }
    }

    @Test
    void testTryAcquire() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(5);
        Thread t1 = new Thread(() -> {
            //一次性拿走所有的许可证
            int permits = semaphore.drainPermits();
            try {
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release(permits);
            }
        }, "T1");
        t1.start();
        Thread t2 = new Thread(() -> {
            System.out.println(semaphore.availablePermits());
            try {
                //尝试获取，获取不到就不获取了，直接进行后续的操作
                boolean success = semaphore.tryAcquire(4, TimeUnit.SECONDS);
                System.out.println(success ? "successful" : "failure");
//                TimeUnit.SECONDS.sleep(4);
                System.out.println(Thread.currentThread().getName() + " finished");
                System.out.println(Thread.currentThread().getName() + " finished");
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }, "T2");
        t2.start();
        TimeUnit.SECONDS.sleep(1);
        Thread.currentThread().join();
    }
}
