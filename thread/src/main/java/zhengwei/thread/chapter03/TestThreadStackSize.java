package zhengwei.thread.chapter03;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/2 19:22
 */
public class TestThreadStackSize {
	private static int count = 0;

	public static void main(String[] args) {
		//创建Thread时，指定该线程的stackSize，以调整Thread的栈帧深度
		new Thread(null, () -> {
			try {
				add(0);
			} catch (Error e) {
				System.out.println(count);
			}
		}, "stackSize", 1 << 24).start();
	}

	static void add(int num) {
		++count;
		add(num + 1);
	}
}
