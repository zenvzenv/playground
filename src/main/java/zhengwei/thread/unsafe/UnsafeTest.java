package zhengwei.thread.unsafe;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private static Unsafe getUnsafe() {
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

		public CounterRunnable(Counter counter, int num) {
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
	@Test
	void test(){
		ExecutorService service= Executors.newFixedThreadPool(1000);
		Counter counter=null;
		long start=System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			service.submit(new CounterRunnable(counter,10000));
		}
		long end = System.currentTimeMillis();
	}
}
