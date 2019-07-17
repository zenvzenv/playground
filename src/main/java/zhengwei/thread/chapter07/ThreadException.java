package zhengwei.thread.chapter07;

import java.util.Arrays;

/**
 * 捕获线程再运行期间抛出的异常
 * public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
 *         checkAccess();
 *         uncaughtExceptionHandler = eh;
 * }
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/16 19:02
 */
public class ThreadException {
	private static final int A = 10;
	private static final int B = 0;

	public static void main(String[] args) {
		Thread t = new Thread(() -> {
			try {
				Thread.sleep(5_000L);
				int i = A / B;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		//捕获Thread在抛出非检查异常
		t.setUncaughtExceptionHandler((thread, e) -> {
			System.out.println("exception->" + e);
			System.out.println("thread info ->" + thread);
		});
		t.start();
		//获取Thread的线程堆栈信息，方便以后查错或自定义打印日志信息
		/*StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		Arrays.stream(stackTrace)
				.filter(stackTraceElement -> !stackTraceElement.isNativeMethod())
				.forEach(stackTraceElement -> System.out::println);*/
	}
}
