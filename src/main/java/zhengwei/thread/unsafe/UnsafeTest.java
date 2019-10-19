package zhengwei.thread.unsafe;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/4 20:47
 */
public class UnsafeTest {
    @Test
    void testGetUnsafe() {
		/*Unsafe unsafe = Unsafe.getUnsafe();
		System.out.println(unsafe);*/
        Unsafe unsafe1 = getUnsafe();
        System.out.println(unsafe1);
    }

    static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            //设置可以访问私有属性
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计数器
     */
    interface Counter {
        void increment();

        long getCounter();
    }

    /**
     * 计数器线程
     */
    static class CounterRunnable implements Runnable {
        private final Counter counter;
        private final int num;

        CounterRunnable(Counter counter, int num) {
            this.counter = counter;
            this.num = num;
        }

        @Override
        public void run() {
            for (int i = 0; i < num; i++) {
                counter.increment();
            }
        }
    }

    /**
     * 不加任何锁
     */
    static class StupidCounter implements Counter {
        private long counter = 0;

        @Override
        public void increment() {
            counter++;
        }

        @Override
        public long getCounter() {
            return counter;
        }
    }

    /**
     * time pass -> 234
     * counter ->9598729
     */
    @Test
    void testStupidCounter() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(1000);
        Counter counter = new StupidCounter();
        calc(service, counter);
    }

    /**
     * 加上synchronized关键字的计数器
     * 把所有自加一操作串行化
     */
    static class SynchronizedCounter implements Counter {
        private long counter = 0;

        /**
         * 彻底串行化
         */
        @Override
        public synchronized void increment() {
            counter++;
        }

        /**
         * 只读，无需加锁
         *
         * @return
         */
        @Override
        public long getCounter() {
            return counter;
        }
    }

    /**
     * time pass -> 722
     * counter ->10000000
     */
    @Test
    void testSyncCounter() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(1000);
        Counter counter = new SynchronizedCounter();
        calc(service, counter);
    }

    private void calc(ExecutorService service, Counter counter) throws InterruptedException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            service.submit(new CounterRunnable(counter, 10000));
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.HOURS);
        long end = System.currentTimeMillis();
        System.out.println("time pass -> " + (end - start));
        System.out.println("counter ->" + counter.getCounter());
    }

    /**
     * 显示锁lock
     */
    static class LockCounter implements Counter {
        private long counter = 0;
        //默认是不公平锁
        private final Lock lock = new ReentrantLock();

        @Override
        public void increment() {
            try {
                lock.lock();
                counter++;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public long getCounter() {
            return counter;
        }
    }

    /**
     * time pass -> 494
     * counter ->10000000
     */
    @Test
    void testLockCounter() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(1000);
        Counter counter = new LockCounter();
        calc(service, counter);
    }

    /**
     * 原子类型计数器
     */
    static class AtomicCounter implements Counter {
        private AtomicLong counter = new AtomicLong();

        @Override
        public void increment() {
            counter.incrementAndGet();
        }

        @Override
        public long getCounter() {
            return counter.get();
        }
    }

    /**
     * time pass -> 373
     * counter ->10000000
     */
    @Test
    void testAtomicCounter() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(1000);
        Counter counter = new AtomicCounter();
        calc(service, counter);
    }

    static class MyCASCounter implements Counter {
        private volatile long counter = 0;
        private Unsafe unsafe;
        private long offset;

        public MyCASCounter() throws NoSuchFieldException {
            unsafe = getUnsafe();
            offset = unsafe.objectFieldOffset(MyCASCounter.class.getDeclaredField("counter"));
        }

        @Override
        public void increment() {
            long current = counter;
            //在和期望值比对不成功的时候进行对当前值重新赋值，以来保证下次比对成功
            while (!unsafe.compareAndSwapLong(this, offset, current, current + 1)) {
                current = this.counter;
            }
        }

        @Override
        public long getCounter() {
            return counter;
        }
    }

	/**
	 * time pass -> 1263
	 * counter ->10000000
	 */
    @Test
	void testMyCASCounter() throws InterruptedException, NoSuchFieldException {
		ExecutorService service = Executors.newFixedThreadPool(1000);
		Counter counter = new MyCASCounter();
		calc(service, counter);
	}
}
