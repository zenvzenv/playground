package zhengwei.thread.phaser;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * arrive方法，实现类似CountDownLatch的功能
 * 不会阻塞，此方法只是通知别的正在等待的线程我已经到达了之后就会继续去执行后续操作
 * 在调用了arriveAndAwaitAdvance()方法的线程会阻塞住，等待其他线程arrive(即调用arrive()方法)
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/27 14:13
 */
public class PhaserArrive {
    private static final Random random = new Random(System.currentTimeMillis());

    private static class ArriveTask extends Thread {
        private final Phaser phaser;

        private ArriveTask(String name, Phaser phaser) {
            super(name);
            this.phaser = phaser;
            start();
        }

        @Override
        public void run() {
            doFirstThing();
            //不会阻塞，通知别的线程我已经到达了，之后会继续后续操作
            phaser.arrive();
            System.out.println(Thread.currentThread().getName() + " arrive and go on do other thing");
            doOtherThing();
        }
    }

    public static void main(String[] args) {
        final Phaser phaser = new Phaser(5);
        IntStream.rangeClosed(1, 4).forEach(i -> new ArriveTask("T-" + i, phaser));
        phaser.arriveAndAwaitAdvance();
        System.out.println("all preceding thing is finished, main thread keep moving");
    }

    private static void doFirstThing() {
        System.out.println(Thread.currentThread().getName() + " start first thing.");
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " end first thing");
    }

    private static void doOtherThing() {
        System.out.println(Thread.currentThread().getName() + " start other thing.");
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " end other thing");
    }
}
