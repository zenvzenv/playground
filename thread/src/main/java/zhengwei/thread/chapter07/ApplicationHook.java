package zhengwei.thread.chapter07;

/**
 * 为我们的程序加上一个hook，钩子程序，在程序即将退出的时候，程序会继续做一些善后工作
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/16 13:50
 */
public class ApplicationHook {
	public static void main(String[] args) {
		/*
		注册钩子程序，在程序停止前会去执行线程中的代码
		但是kill -9 pid之后程序是没有机会去执行相关代码的
		 */
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				System.out.println("app will exit.");
				Thread.sleep(1_000L);
				notifyAndRelease();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}));
		int i = 0;
		while (true) {
			try {
				Thread.sleep(1_000L);
				System.out.println("i am working -> " + i);
				i++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void notifyAndRelease() {
		try {
			System.out.println("start to release source(connection,thread pool...)");
			Thread.sleep(1_000L);
			System.out.println("end to release source.");
			System.out.println("App is done");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
