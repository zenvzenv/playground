package zhengwei.thread.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 带有时间戳的原子引用类型
 * ABA问题就是：
 * 当有一个变量，它的值为A，线程T1对它进行了修改变成了B；
 * 此时线程T2还没有进行操作，
 * 同时线程T1又把变量从B变成了A，即变量经历了A->B->B的一个流程
 * 此时线程T2进行操作，查看变量还是A，进行原子比对通过，然后线程T2继续进行接下来的操作，
 * 但此时的变量已经发生变化，而不是原来的A了，这时操作变量就会有问题，
 * 带有戳的原子引用类型可以有效解决ABA问题
 *
 * @author zhengwei AKA Awei
 * @since 2019/9/23 19:37
 */
public class AtomicStampReferenceTest {
	private static final AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100, 0);

	@Test
	void testABA() throws InterruptedException {
		Thread t1 = new Thread(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				boolean success = atomicStampedReference.compareAndSet(100, 101, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1);
				System.out.println("1->" + success);
				success = atomicStampedReference.compareAndSet(101, 100, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1);
				System.out.println("2->" + success);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		Thread t2 = new Thread(() -> {
			try {
				//线程T2持有的戳是初始化时的戳即0
				int stamp = atomicStampedReference.getStamp();
				System.out.println("Before stamp:stamp->" + stamp);
				TimeUnit.SECONDS.sleep(1);
				//拿着旧的戳去比对显然是不会比对成功的，返回false
				boolean success = atomicStampedReference.compareAndSet(100, 101, stamp, stamp + 1);
				System.out.println("3->" + success);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
}
