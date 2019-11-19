package zhengwei.thread.semaphore;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore，许可证管理器
 * 初始化一个固定容量的"池子"。该"池子"可以容纳固定数量的线程，在"池子"中线程可以使用公共资源。
 * acquire-"池子"的容量减一
 * release-"池子"的容量加一
 * 如果"池子"的容量为0的话，那么接下来的线程还想去访问公共资源的时候将会被阻塞，等待"池子"中有空出来的阿位置
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/19 13:36
 */
class SemaphoreDemo1 {
    @Test
    void test() throws InterruptedException {
        final SemaphoreLock semaphoreLock = new SemaphoreLock();
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    semaphoreLock.lock();
                    System.out.printf("%s lock\n", Thread.currentThread().getName());
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphoreLock.unlock();
                    System.out.printf("%s unlock\n", Thread.currentThread().getName());
                }
            }).start();
        }
        Thread.currentThread().join();
    }

    /**
     * 利用Semaphore做一个锁
     */
    private static class SemaphoreLock {
        /**
         * 控制线程个数
         * 如果容量为1的话，类似于synchronized，一次只能有一个线程使用公共资源
         * synchronized不能够让一个公用资源供多个线程同时使用
         * Semaphore能够让多个线程进入一个公共方法，去同时运行
         */
        private static final Semaphore SEMAPHORE = new Semaphore(1);

        public void lock() throws InterruptedException {
            SEMAPHORE.acquire();
        }

        void unlock() {
            SEMAPHORE.release();
        }
    }
}
