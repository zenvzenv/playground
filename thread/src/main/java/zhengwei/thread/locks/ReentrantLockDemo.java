package zhengwei.thread.locks;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * ReentrantLock，可重入锁，显示锁
 * 默认为非公平锁，可以指定为公平锁，但并不是100%公平
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/20 13:46
 */
public class ReentrantLockDemo {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        IntStream.rangeClosed(1, 2).forEach(i -> new Thread(ReentrantLockDemo::needLockCantInterrupt).start());
        //获取waiting queue的长度
        Optional.of(lock.getQueueLength()).ifPresent(System.out::println);
        //waiting queue中是否还有等待的线程
        Optional.of(lock.hasQueuedThreads()).ifPresent(System.out::println);
        //当前线程对该锁的保持次数
        Optional.of(lock.getHoldCount()).ifPresent(System.out::println);
        //查看某个线程在不在waiting queue中
        Optional.of(lock.hasQueuedThread(new Thread())).ifPresent(System.out::println);
        //锁是否被其他线程持有
        Optional.of(lock.isLocked()).ifPresent(System.out::println);
        //getQueuedThreads():获取等待的所有线程
    }

    private static void needLockCantInterrupt() {
        try {
            //不可打断
            lock.lock();
            System.out.println(Thread.currentThread().getName() + " get the lock");
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + " release the lock");
        }
    }

    private static void needLockInterruptibly() {
        try {
            //可打断
            lock.lockInterruptibly();
            System.out.println(Thread.currentThread().getName() + " get the lock");
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + " release the lock");
        }
    }

    private static void needLockTryLock() {
        //快速失败，获取不到就不获取锁
//        if (lock.tryLock(1, TimeUnit.SECONDS)) {
        if (lock.tryLock()) {
            try {
                System.out.println(Thread.currentThread().getName() + " get the lock");
                while (true) {

                }
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println(Thread.currentThread().getName() + " do not get the lock");
        }
    }

    private static synchronized void syncLock() {
        System.out.println();
    }
}
