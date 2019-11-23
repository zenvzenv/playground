package zhengwei.thread.chapter05;

/**
 * 死锁
 * 线程T1启动，线程T2启动
 * "T2":
 *         at zhengwei.thread.chapter05.DeadLock.m2(DeadLockTest.java:44)
 *         - waiting to lock <0x000000076bab3640> (a java.lang.Object)
 *         at zhengwei.thread.chapter05.OtherService.s2(DeadLockTest.java:66)
 *         - locked <0x000000076bab06f0> (a java.lang.Object)
 *         at zhengwei.thread.chapter05.DeadLockTest.lambda$main$1(DeadLockTest.java:21)
 *         at zhengwei.thread.chapter05.DeadLockTest$$Lambda$2/1129670968.run(Unknown Source)
 *         at java.lang.Thread.run(Thread.java:748)
 * "T1":
 *         at zhengwei.thread.chapter05.OtherService.s1(DeadLockTest.java:59)
 *         - waiting to lock <0x000000076bab06f0> (a java.lang.Object)
 *         at zhengwei.thread.chapter05.DeadLock.m1(DeadLockTest.java:38)
 *         - locked <0x000000076bab3640> (a java.lang.Object)
 *         at zhengwei.thread.chapter05.DeadLockTest.lambda$main$0(DeadLockTest.java:16)
 *         at zhengwei.thread.chapter05.DeadLockTest$$Lambda$1/1254526270.run(Unknown Source)
 *         at java.lang.Thread.run(Thread.java:748)
 * 可以通过 jstack pid 查看进程的堆栈情况，可以检查是否有死锁
 * @author zhengwei AKA Awei
 * @since 2019/7/13 9:02
 */
public class DeadLockTest {
	public static void main(String[] args) {
		OtherService otherService = new OtherService();
		DeadLock deadLock = new DeadLock(otherService);
		otherService.setDeadLock(deadLock);
		new Thread(() -> {
			while (true) {
				deadLock.m1();
			}
		}, "T1").start();
		new Thread(() -> {
			while (true) {
				otherService.s2();
			}
		}, "T2").start();
	}
}

class DeadLock {
	private OtherService otherService;
	private static final Object LOCK = new Object();

	public DeadLock(OtherService otherService) {
		this.otherService = otherService;
	}

	public void m1() {
		synchronized (LOCK) {
			System.out.println("m1--");
			otherService.s1();
		}
	}

	public void m2() {
		synchronized (LOCK) {
			System.out.println("m2--");
		}
	}
}

class OtherService {
	private DeadLock deadLock;
	private static final Object LOCK = new Object();

	public void setDeadLock(DeadLock deadLock) {
		this.deadLock = deadLock;
	}

	public void s1() {
		synchronized (LOCK) {
			System.out.println("s1--");
		}
	}

	public void s2() {
		synchronized (LOCK) {
			System.out.println("s2--");
			deadLock.m2();
		}
	}
}