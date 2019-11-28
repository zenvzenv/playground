package zhengwei.thread.condition;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 不使用Condition来实现生产者消费者
 * 即生产一次消费一次(仅在公平锁情况下)
 *
 * Condition需要个ReentrantLock配合使用，要不然会报错
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/24 9:51
 */
public class ReentrantLockProdConsumer {
    //只有在是公平锁的情况下，生产者和消费者才能够较为正常的运转
    //若改成非公平锁，那么生产者和消费者的平衡将会被打破(生产一次，消费一次)
    private static final ReentrantLock lock = new ReentrantLock(true);
    private static final Condition condition = lock.newCondition();
    private static int data = 0;
    private static volatile boolean use = false;

    private static void buildData() {
        try {
            lock.lock();//synchronized->#monitor enter
            data++;
            Optional.of("[ " + Thread.currentThread().getName() + " ] P -> " + data).ifPresent(System.out::println);
            TimeUnit.SECONDS.sleep(1);
            use = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();//synchronized->#monitor end
        }
    }

    private static void useData() {
        try {
            lock.lock();
            TimeUnit.SECONDS.sleep(1);
            Optional.of("[ " + Thread.currentThread().getName() + " ] C -> " + data).ifPresent(System.out::println);
            use = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        new Thread(() -> {
            for (; ; ) {
                buildData();
            }
        }, "prod").start();
        new Thread(() -> {
            for (; ; ) {
                useData();
            }
        }, "consumer").start();

    }
}
