package zhengwei.thread.chapter05;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/13 8:38
 */
public class ThreadSynchronizedStatic {
	public static void main(String[] args) {
		new Thread(SynchronizedStatic::m1, "T1").start();
		new Thread(SynchronizedStatic::m2, "T2").start();
		new Thread(SynchronizedStatic::m3, "T3").start();
	}
}

/**
 * 若synchronized修饰在静态方法上的话，那么synchronized锁定的是class对象
 * 静态代码块是在类被初始化的时候要执行的，只会被执行一次
 * 在static代码块中我们锁定了SynchronizedStatic.class对象，所以在执行static代码的时候只能有一个线程去执行，其余线程都要等待
 * 执行完static代码块之后，由于m3并不是同步方法，所以会被立即执行，而m1和m2则要进行锁竞争，去竞争SynchronizedStatic.class对象的多
 */
class SynchronizedStatic {
	static {
		synchronized (SynchronizedStatic.class) {
			try {
				System.out.println("init by ->" + Thread.currentThread().getName() + "-start");
				Thread.sleep(5_000L);
				System.out.println("init by ->" + Thread.currentThread().getName() + "-end");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static synchronized void m1() {
		try {
			System.out.println("m1 -> " + Thread.currentThread().getName() + "-start");
			Thread.sleep(5_000L);
			System.out.println("m1 -> " + Thread.currentThread().getName() + "-end");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static synchronized void m2() {
		try {
			System.out.println("m2 -> " + Thread.currentThread().getName() + "-start");
			Thread.sleep(5_000L);
			System.out.println("m2 -> " + Thread.currentThread().getName() + "-end");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//m3方法没有被synchronized修饰
	static void m3() {
		try {
			System.out.println("m3 -> " + Thread.currentThread().getName() + "-start");
			Thread.sleep(5_000L);
			System.out.println("m3 -> " + Thread.currentThread().getName() + "-end");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}