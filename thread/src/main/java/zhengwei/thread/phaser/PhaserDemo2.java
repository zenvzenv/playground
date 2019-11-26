package zhengwei.thread.phaser;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 固定线程数量，并且可以循环去使用
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/26 18:47
 */
public class PhaserDemo2 {
    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * 铁人三项
     */
    public static void main(String[] args) {
        /*
         * 5个线程，只有等到5个线程全部结束才能集体结束
         */
        final Phaser phaser = new Phaser(5);
        IntStream.rangeClosed(1, 5).forEach(i -> new Athletes(i, phaser).start());
    }

    private static class Athletes extends Thread {
        private final int no;
        private final Phaser phaser;

        Athletes(int no, Phaser phaser) {
            this.no = no;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            try {
                sport(no, phaser, " start running...", " end running...");

                sport(no, phaser, " start bicycle...", " end bicycle...");

                sport(no, phaser, " start jump...", " end jump...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sport(int no, Phaser phaser, String s, String s2) throws InterruptedException {
        System.out.println(no + s);
        TimeUnit.SECONDS.sleep(random.nextInt(5));
        System.out.println(no + s2);
        phaser.arriveAndAwaitAdvance();
    }
}
