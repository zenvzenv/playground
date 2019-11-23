package zhengwei.jvm.memory;

/**
 * 逃逸分析
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/7 17:10
 */
public class EscapeAnalysis {
	/*
	 * -Xmx4G -Xms4G -XX:-DoEscapeAnalysis -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError
	 * 1000000个user对象全部在堆中分配
	 */
	/*public static void main(String[] args) {
		long a1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			alloc();
		}
		// 查看执行时间
		long a2 = System.currentTimeMillis();
		System.out.println("cost " + (a2 - a1) + " ms");
		// 为了方便查看堆内存中对象个数，线程sleep
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}*/

	/**
	 * -Xmx4G -Xms4G -XX:+DoEscapeAnalysis -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError
	 * 84536个user对象在堆上分配，其余对象将在栈上分配，创建1000000个对象的速度也比在对上创建的速度要快
	 */
	public static void main(String[] args) {
		long a1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			alloc();
		}
		// 查看执行时间
		long a2 = System.currentTimeMillis();
		System.out.println("cost " + (a2 - a1) + " ms");
		// 为了方便查看堆内存中对象个数，线程sleep
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	private static void alloc() {
		User user = new User();
	}

	private static class User {

	}
}
