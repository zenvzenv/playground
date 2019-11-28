package zhengwei.thread.condition;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/24 10:21
 */
public class ProducerConsumer {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition PRODUCER_CONDITION = lock.newCondition();
    private static final Condition CONSUMER_CONDITION = lock.newCondition();
    //共享资源
    private static final LinkedList<Long> TIMESTAMP_POOL = new LinkedList<>();
    //最大容量
    private static final int MAX_CAPACITY = 100;

    public static void main(String[] args) {
        IntStream.range(0, 6).forEach(ProducerConsumer::createProducer);
        IntStream.range(0, 10).forEach(ProducerConsumer::createConsumer);
    }

    /**
     * 生产者方法
     */
    private static void produce() {
        try {
            lock.lock();
            while (MAX_CAPACITY == TIMESTAMP_POOL.size()) {
                PRODUCER_CONDITION.await();
            }
            //如果调用该方法的线程不是持有锁的线程将会报错IllegalMonitorStateException
            System.out.println("PRODUCER_CONDITION.getWaitQueueLength -> " + lock.getWaitQueueLength(PRODUCER_CONDITION));
            System.out.println("PRODUCER_CONDITION.hasWaiters -> " + lock.hasWaiters(PRODUCER_CONDITION));
            long value = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + " - P - " + value);
            TIMESTAMP_POOL.add(value);
            //唤醒消费者
            CONSUMER_CONDITION.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 消费者方法
     */
    private static void consumer() {
        try {
            lock.lock();
            while (TIMESTAMP_POOL.isEmpty()) {
                CONSUMER_CONDITION.await();
            }
            System.out.println("CONSUMER_CONDITION.getWaitQueueLength -> " + lock.getWaitQueueLength(CONSUMER_CONDITION));
            System.out.println("CONSUMER_CONDITION.hasWaiters -> " + lock.hasWaiters(CONSUMER_CONDITION));
            long value = TIMESTAMP_POOL.removeFirst();
            System.out.println(Thread.currentThread().getName() + " - C - " + value);
            //唤醒生产者
            PRODUCER_CONDITION.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private static void sleep(long time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createProducer(int i) {
        new Thread(() -> {
            while (true) {
                produce();
                sleep(2);
            }
        }, "P-" + i).start();
    }

    private static void createConsumer(int i) {
        new Thread(() -> {
            while (true) {
                consumer();
                sleep(2);
            }
        }, "C-" + i).start();
    }
}
