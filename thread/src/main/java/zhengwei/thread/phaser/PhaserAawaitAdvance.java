package zhengwei.thread.phaser;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * awaitAdvance(int)
 * 如果给的值和当前阶段的值不相等的话将会立即返回，
 * 如果给的值和当前阶段的值是相等的那么就会阻塞等待其他线程结束
 * 调用awaitAdvance(int)方法的线程不会参与Phaser的party的计算
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/27 14:37
 */
public class PhaserAawaitAdvance {
    private static final Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        final Phaser phaser = new Phaser(5);
        IntStream.rangeClosed(1, 5).forEach(i -> new AwaitAdvanceTask("T-" + i, phaser));
        phaser.awaitAdvance(phaser.getPhase());
        System.out.println("------------");
    }

    private static class AwaitAdvanceTask extends Thread {
        private final Phaser phaser;

        AwaitAdvanceTask(String name, Phaser phaser) {
            super(name);
            this.phaser = phaser;
            start();
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(getName() + " is finished.");
            phaser.arriveAndAwaitAdvance();
        }
    }
}
