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
 * 对于乐观读锁
 * 适用于有很多的读线程和很少的写线程
 * StampedLock有一个stamped(戳)，每一次的操作stamped都会发生改变
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/24 15:31
 */
public class StampedLockDemo2 {
    private static final StampedLock LOCK = new StampedLock();
    private static final List<Long> DATA = new ArrayList<>();
    private static final ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        for (int i = 0; i < 9; i++) {
            service.submit(() -> {
                while (true) {
                    read();
                }
            });
        }
        service.submit(() -> {
            while (true) {
                write();
            }
        });
    }

    private static void read() {
        //乐观锁，尝试去获取锁
        long stamped = LOCK.tryOptimisticRead();
        //检查在获取到读锁stamped之后，这个stamped有没有被修改过(即写锁被别的侠女获取了)
        //这里可以采用CAS的做法，自旋到stamped为修改之前的状态
        if (!LOCK.validate(stamped)) {
            try {
                //升级乐观锁为悲观锁，如果当前对象正在被修改，那么读锁可能会导致线程挂起
                stamped = LOCK.readLock();
                Optional.of(
                        DATA
                                .stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining("#", "R-", ""))
                ).ifPresent(System.out::println);
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                LOCK.unlockRead(stamped);
            }
        }
        Optional.of(
                DATA
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("#", "R-", ""))
        ).ifPresent(System.out::println);
    }

    private static void write() {
        long stamped = -1;
        try {
            stamped = LOCK.writeLock();
            DATA.add(System.currentTimeMillis());
        }finally {
            LOCK.unlockWrite(stamped);
        }
    }
}
