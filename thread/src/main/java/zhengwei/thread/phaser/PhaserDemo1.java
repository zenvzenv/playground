package zhengwei.thread.phaser;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 用Phaser实现类似CountDownLatch的例子
 * 当所有线程都执行完毕之后开始执行后续动作
 *
 * 动态注册线程
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/26 13:33
 */
public class PhaserDemo1 {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        final Phaser phaser = new Phaser();
        IntStream.rangeClosed(1, 5).boxed().map(i -> phaser).forEach(Task::new);
        //注册线程
        phaser.register();
        //等待
        phaser.arriveAndAwaitAdvance();
        System.out.println("all of work has been done");
    }

    private static class Task extends Thread {
        private final Phaser phaser;

        Task(Phaser phaser) {
            this.phaser = phaser;
            //类似于CountDownLatch中的countdown
            this.phaser.register();
            start();
        }

        @Override
        public void run() {
            System.out.println("The worker [ " + getName() + " ] is working...");
            try {
                TimeUnit.SECONDS.sleep(RANDOM.nextInt(6));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            phaser.arriveAndAwaitAdvance();
        }
    }
}
