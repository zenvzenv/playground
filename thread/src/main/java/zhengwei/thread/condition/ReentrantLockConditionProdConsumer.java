package zhengwei.thread.condition;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock和Condition的生产者和消费者模式
 *
 * 在以往的synchronized版本的生产者消费者模式中，在生产者或消费者wait的时候，
 * 生产者或消费者线程会进入wait set中，并且释放锁，其他的线程可以去抢锁
 *
 * 在ReentrantLock和Condition的生产者消费者模式中，在生产者或消费调用lock的时候
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/24 9:14
 */
public class ReentrantLockConditionProdConsumer {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition condition = lock.newCondition();
    private static int data = 0;
    private static volatile boolean use = false;

    private static void buildData() {
        try {
            lock.lock();//synchronized->#monitor enter
            while (use) {
                condition.await();//monitor.wait()
            }
            data++;
            Optional.of("[ " + Thread.currentThread().getName() + " ] P -> " + data).ifPresent(System.out::println);
            TimeUnit.SECONDS.sleep(1);
            use = true;
            condition.signal();//monitor.notify()
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();//synchronized->#monitor end
        }
    }

    private static void useData() {
        try {
            lock.lock();
            while (!use) {
                //会自动释放锁，失去了CPU的执行权，直到别的线程调用signal或signalAll之后重新获取锁
                //语义和wait类似，wait是将线程放入到wait set中
                condition.await();
            }
            TimeUnit.SECONDS.sleep(1);
            Optional.of("[ " + Thread.currentThread().getName() + " ] C -> " + data).ifPresent(System.out::println);
            use = false;
            condition.signal();
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
