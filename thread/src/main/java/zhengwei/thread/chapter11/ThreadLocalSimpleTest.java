package zhengwei.thread.chapter11;

/**
 * ThreadLocal简单测试
 * @author zhengwei AKA Awei
 * @since 2019/7/31 12:45
 */
public class ThreadLocalSimpleTest {
	//赋予ThreadLocal默认值，如果没有赋值的话，则返回默认值
	private final static ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "zhengwei");

	//JVM start main thread
	public static void main(String[] args) throws InterruptedException {
		threadLocal.set("aaa");
		Thread.sleep(1000L);
		System.out.println(threadLocal.get());
	}
}
