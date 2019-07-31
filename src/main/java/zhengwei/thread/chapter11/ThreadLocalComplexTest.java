package zhengwei.thread.chapter11;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/31 15:53
 */
public class ThreadLocalComplexTest {
	private static final ThreadLocal threadLocal = new ThreadLocal();

	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(() -> {
			threadLocal.set(Thread.currentThread().getName());
			try {
				Thread.sleep(1_000L);
				System.out.println(threadLocal.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, "thread-1");
		Thread t2 = new Thread(() -> {
			threadLocal.set(Thread.currentThread().getName());
			try {
				Thread.sleep(1_000L);
				System.out.println(threadLocal.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, "thread-2");
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		System.out.println("=====================");
		System.out.println(threadLocal.get());
	}
}
