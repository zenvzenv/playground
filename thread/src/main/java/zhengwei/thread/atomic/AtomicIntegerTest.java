package zhengwei.thread.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 主要是atomic.cpp
 * 	1.先判断CPU是不是多核的
 * @author zhengwei AKA Awei
 * @since 2019/9/2 13:00
 */
public class AtomicIntegerTest {
	private static final AtomicInteger integer = new AtomicInteger(0);
	private static int count = 0;

	@Test
	void testWithAtomic() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			Thread t = new Thread(() -> {
				for (int j = 0; j < 1000; j++) {
					integer.incrementAndGet();
				}
			});
			t.start();
			t.join();
		}
		System.out.println(integer);
	}

	@Test
	void testWithoutAtomic() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			Thread t = new Thread(() -> {
				for (int j = 0; j < 1000; j++) {
					synchronized (AtomicIntegerTest.class) {
						count++;
					}
				}
			});
			t.start();
			t.join();
		}
		System.out.println(count);
	}

	@Test
	void testCreate() {
		//内存值
		AtomicInteger integer = new AtomicInteger(1);
		//当且仅当期望的值与内存中的值相等的时候才会去更新内存值
		boolean result = integer.compareAndSet(1, 4);
		System.out.printf("expect->%b,integer->%d\n", result, integer.get());
		integer.lazySet(10);
		System.out.println(integer.get());
	}

	@Test
	void testGetAndAdd() {
		int andAdd = integer.getAndSet(1);
	}
}

class LockTest {
	private static final CompareLock lock = new CompareLock();

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			//					doSomething();
			new Thread(LockTest::doSomething2).start();
		}
	}

	private static void doSomething() throws InterruptedException {
		synchronized (LockTest.class) {
			System.out.println(Thread.currentThread().getName() + "--" + " get the lock");
			Thread.sleep(10000L);
		}
	}

	private static void doSomething2() {
		try {
			lock.tryLock();
			System.out.println(Thread.currentThread().getName() + " get the lock");
			Thread.sleep(10000L);
		} catch (FailedToGetLockException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}

class FailedToGetLockException extends Exception {
	public FailedToGetLockException(String message) {
		super(message);
	}
}

class CompareLock {
	private static final AtomicInteger FLAG = new AtomicInteger();
	//上锁的线程
	private static Thread currentThread;

	public void tryLock() throws FailedToGetLockException {
		boolean success = FLAG.compareAndSet(0, 1);
		if (!success) {
			throw new FailedToGetLockException("Get the lock failed");
		}
		currentThread = Thread.currentThread();
	}

	public void unlock() {
		if (0 == FLAG.get() || currentThread != Thread.currentThread()) {
			return;
		}
		FLAG.compareAndSet(1, 0);
	}
}