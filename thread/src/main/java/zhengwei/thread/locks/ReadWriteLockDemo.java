package zhengwei.thread.locks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读写分离锁
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/23 19:36
 */
public class ReadWriteLockDemo {
    //    private static final ReentrantLock lock = new ReentrantLock(true);
    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    //读锁
    private static final Lock readLock = readWriteLock.readLock();
    //写锁
    private static final Lock writeLock = readWriteLock.writeLock();
    private static final List<Long> list = new ArrayList<>();

    /**
     * W W 互斥
     * W R 互斥
     * R W 互斥
     * R R 不互斥
     */
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(ReadWriteLockDemo::write, "T1");
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        Thread t2 = new Thread(ReadWriteLockDemo::read, "T2");
        t2.start();
    }

    private static void write() {
        try {
            writeLock.lock();
            list.add(System.currentTimeMillis());
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    private static void read() {
        try {
            readLock.lock();
            list.forEach(System.out::println);
            TimeUnit.SECONDS.sleep(5);
            System.out.println(Thread.currentThread().getName() + "-------------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
    }
}
