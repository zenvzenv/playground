package zhengwei.thread.countdownlatch;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.*;

/**
 * CountDownLatch
 * 门闩设计，无锁
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/12 19:03
 */
public class CountDownLatchDemo {
    private static Random random = new Random(System.currentTimeMillis());
    private static ExecutorService executor = Executors.newFixedThreadPool(2);
    //门闩
    private static final CountDownLatch LATCH = new CountDownLatch(10);

    @Test
    void testNoneCountDownLatch() throws InterruptedException {
        //1
        int[] data = query();
        //2
        for (int i = 0; i < data.length; i++) {
            //异步
            executor.execute(new SimpleRunnable(data, i));
        }
        //3
        //给所有线程打一个标签shutdown，当所有任务执行完之后才会去执行shutdown，shutdown也是异步执行的
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        System.out.println("all of work is finish");
    }

    @Test
    void testUseCountDownLatch() throws InterruptedException {
        int[] data = query();
        for (int i = 0; i < data.length; i++) {
            executor.submit(new SimpleRunnable2(data, i, LATCH));
        }
        LATCH.await();
        System.out.println("all of work is finish");
    }

    private static int[] query() {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    }

    private static class SimpleRunnable implements Runnable {
        private final int[] data;
        private final int index;

        private SimpleRunnable(int[] data, int index) {
            this.data = data;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(2000));
                int value = data[index];
                if (value % 2 == 0) {
                    data[index] = value * 2;
                } else {
                    data[index] = value * 30;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " work is finish!");
        }
    }

    private static class SimpleRunnable2 extends SimpleRunnable {
        private final CountDownLatch latch;

        private SimpleRunnable2(int[] data, int index, CountDownLatch latch) {
            super(data, index);
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(2000));
                int value = super.data[super.index];
                if (value % 2 == 0) {
                    super.data[super.index] = value * 2;
                } else {
                    super.data[super.index] = value * 30;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
            System.out.println(Thread.currentThread().getName() + " work is finish!");
        }
    }
}
