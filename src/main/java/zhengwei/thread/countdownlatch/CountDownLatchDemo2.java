package zhengwei.thread.countdownlatch;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch允许一个或多个线程去等待(await())，直到一系列的其他操作在其他的线程中被完成(countdown())
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/13 19:12
 */
public class CountDownLatchDemo2 {
    /**
     * 可以有一个或多个线程去await，只要最后CountDownLatch中的count降为0，await的线程就会被激活
     */
    @Test
    void testASetOfThreadWait() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                System.out.println("do some init work");
                Thread.sleep(1_000L);
                latch.await();
                System.out.println("do some other thing");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                latch.await();
                Thread.sleep(3_000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                System.out.println("sync prepare for some date");
                Thread.sleep(2_000L);
                System.out.println("prepare data finish");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();
        //main线程join它自己的话，就会导致程序永远结束不了
        //自己等待自己
        Thread.currentThread().join();
    }

    /**
     * CountDownLatch的初始值必须大于等于0
     */
    @Test
    void testTips() {
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread currentThread = Thread.currentThread();
        new Thread(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //如果注释掉，将没有线程去countdown，那么CountDownLatch中的门闩也就不会变成0，程序将结束不了
                latch.countDown();
                //如果去打断main线程的话，将会导致程序结束
//                currentThread.interrupt();
            }
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("main thread has been interrupted");
        }
        //我们捕捉打断信号以做出相应操作
        if (Thread.interrupted()) {
            System.out.println("main thread has been interrupted");
        }
        System.out.println("=============================");
    }

    @Test
    void testAwaitByTime() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                System.out.println("prepare something to work...");
                Thread.sleep(50_000L);
                System.out.println("end prepare something...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();
        //指定等待时间
        // 如果到时间之后还有线程没有结束的话，也会强制结束；
        // 如果所有线程都提前结束的话那么await也会提前结束
        latch.await(10_000L, TimeUnit.MILLISECONDS);
        System.out.println("work is finish");
    }
}
