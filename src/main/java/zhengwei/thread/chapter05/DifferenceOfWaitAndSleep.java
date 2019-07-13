package zhengwei.thread.chapter05;

import java.util.stream.Stream;

/**
 * sleep和wait的区别
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/13 14:28
 */
public class DifferenceOfWaitAndSleep {
	private static final Object LOCK = new Object();

	static void m1() {
		try {
			Thread.sleep(2_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//如果不加synchronized将会报java.lang.IllegalMonitorStateException的错
	static void m2() {
		synchronized (LOCK) {
			try {
				LOCK.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static void m3() {
		synchronized (LOCK) {
			try {
				System.out.println(Thread.currentThread().getName() + "  enter");
				Thread.sleep(5_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static void m4() {
		synchronized (LOCK) {
			try {
				System.out.println(Thread.currentThread().getName() + " enter");
				LOCK.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		m2();
		Stream.of("T1","T2").forEach(t-> new Thread(DifferenceOfWaitAndSleep::m3,t).start());
	}
}
