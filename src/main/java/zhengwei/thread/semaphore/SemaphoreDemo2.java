package zhengwei.thread.semaphore;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore的一些API
 * 线程池、数据库连接池等一些
 * connection pool
 * 拒绝策略
 * 1.设置超时时间
 * 2.阻塞线程
 * 3.什么也不做，直接丢弃，discard
 * 4.call back
 * 5.throw a exception
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/19 19:02
 */
class SemaphoreDemo2 {
    @Test
    void test1() {
        //许可证
        //特别的，当许可证的数量为1的时候，Semaphore具有Lock的作用
        final Semaphore semaphore = new Semaphore(1);
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " in");
                try {
                    //获取许可证
                    semaphore.acquire();
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println(Thread.currentThread().getName() + " get the semaphore");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //释放许可证
                    semaphore.release();
                }
                System.out.println(Thread.currentThread().getName() + " out");
            }).start();
        }
    }

    /**
     * 如果一个线程一次性拿走两个许可证，但是在释放的时候只释放了一个许可证
     * 那么另外一个线程将获取不到足够的许可证，而导致线程一直阻塞，程序不会退出
     */
    @Test
    void test2() {
        //许可证
        //特别的，当许可证的数量为1的时候，Semaphore具有Lock的作用
        final Semaphore semaphore = new Semaphore(2);
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " in");
                try {
                    //获取许可证
                    semaphore.acquire(2);
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println(Thread.currentThread().getName() + " get the semaphore");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //释放许可证
                    //需要释放全部的许可证
//                    semaphore.release();
                    semaphore.release(2);
                }
                System.out.println(Thread.currentThread().getName() + " out");
            }).start();
        }
    }

    @Test
    void testMonitor() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(2);
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " in");
                try {
                    //获取许可证
                    semaphore.acquire();
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println(Thread.currentThread().getName() + " get the semaphore");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //释放许可证
                    //需要释放全部的许可证
//                    semaphore.release();
                    semaphore.release();
                }
                System.out.println(Thread.currentThread().getName() + " out");
            }).start();
        }
        while (true) {
            //当前还有多少许可证剩余
            System.out.println("AP->" + semaphore.availablePermits());
            //评估当前还有几个线程在等待
            System.out.println("QL->" + semaphore.getQueueLength());
            System.out.println("---------------------------------");
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * 可中断等待
     */
    @Test
    void testCanInterrupt() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(1);
        Thread t1 = new Thread(() -> {
            try {
                semaphore.acquire();
                TimeUnit.SECONDS.sleep(5);
                System.out.println("T1 finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
        t1.start();
        TimeUnit.MILLISECONDS.sleep(50);
        Thread t2 = new Thread(() -> {
            try {
                semaphore.acquire();
//                TimeUnit.SECONDS.sleep(5);
                System.out.println("T2 finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
        t2.start();
        TimeUnit.MILLISECONDS.sleep(50);
        t2.interrupt();
    }

    /**
     * 不可打断
     */
    @Test
    void testCantInterrupt() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(1);
        Thread t1 = new Thread(() -> {
            try {
                semaphore.acquire();
                TimeUnit.SECONDS.sleep(5);
                System.out.println("T1 finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
        t1.start();
        TimeUnit.MILLISECONDS.sleep(50);
        Thread t2 = new Thread(() -> {
            try {
                //不可打断
                semaphore.acquireUninterruptibly();
//                TimeUnit.SECONDS.sleep(5);
                System.out.println("T2 finished");
            } finally {
                semaphore.release();
            }
        });
        t2.start();
        TimeUnit.MILLISECONDS.sleep(50);
        t2.interrupt();
    }
}
