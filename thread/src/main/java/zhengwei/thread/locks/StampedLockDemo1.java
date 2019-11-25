package zhengwei.thread.locks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

/**
 * ReentrantLock
 * 比synchronized的API更灵活，可扩展，性能比synchronized好一点
 * ReentrantReadWriteLock
 * 读写分离
 * StampedLock
 *  Java8中新加的读写锁
 *
 * ReentrantReadWriteLock中的读和写都是一种悲观锁的体现，
 * StampedLock加入了一种新的模式——乐观读，它是指当乐观读时假定没有其它线程修改数据，
 * 读取完成后再检查下版本号有没有变化，没有变化就读取成功了，这种模式更适用于读多写少的场景。
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/24 14:47
 */
public class StampedLockDemo1 {
    private static final StampedLock LOCK = new StampedLock();
    private static final List<Long> DATA = new ArrayList<>();
    private static final ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        Runnable readTask = () -> {
            for (; ; ) {
                read();
            }
        };
        Runnable writeTask = () -> {
            for (; ; ) {
                write();
            }
        };
        service.submit(readTask);
        service.submit(readTask);
        service.submit(readTask);
        service.submit(readTask);
        service.submit(readTask);
        service.submit(readTask);
        service.submit(readTask);
        service.submit(readTask);
        service.submit(writeTask);
    }

    private static void read() {
        long stamped = -1;
        try {
            //悲观式的读
            stamped = LOCK.readLock();
            Optional.of(
                    DATA
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining("#", "-", "\n"))
            ).ifPresent(System.out::println);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            LOCK.unlockRead(stamped);
        }
    }

    private static void write() {
        long stamped = -1;
        try {
            stamped = LOCK.writeLock();
            DATA.add(System.currentTimeMillis());
        } finally {
            LOCK.unlockWrite(stamped);
        }
    }
}
