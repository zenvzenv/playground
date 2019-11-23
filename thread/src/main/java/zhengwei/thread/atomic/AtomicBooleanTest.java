package zhengwei.thread.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Boolean原子类
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/7 19:06
 */
public class AtomicBooleanTest {
	@Test
	void testCreateWithoutArg() {
		AtomicBoolean atomicBoolean = new AtomicBoolean();
		System.out.println(atomicBoolean.get());
	}

	@Test
	void testGetAndSet() {
		AtomicBoolean atomicBoolean = new AtomicBoolean(true);
		boolean result = atomicBoolean.getAndSet(false);
		System.out.println(result);
	}

	@Test
	void testCompareAndSet() {
		AtomicBoolean atomicBoolean = new AtomicBoolean(true);
		//第一个参数：期望的值，所期望的值需要与内存中的值一样才会去修改内存中的值，否则修改失败
		//第二个参数：修改的值，如果期望的值和内存中的值符合条件则把内存中的值修改为该值
		boolean result1 = atomicBoolean.compareAndSet(false, true);
		System.out.println(result1);
		System.out.println(atomicBoolean);
		boolean result2 = atomicBoolean.compareAndSet(true, false);
		System.out.println(result2);
		System.out.println(atomicBoolean);
	}

	private final static AtomicBoolean flag = new AtomicBoolean(true);

	@Test
	void atomicBooleanInAction() throws InterruptedException {
		new Thread(() -> {
			while (flag.get()) {
				try {
					Thread.sleep(10000L);
					System.out.println("I am working");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("I am finished");
		}).start();
		Thread.sleep(50000L);
		flag.set(false);
	}
}
