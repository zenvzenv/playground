package zhengwei.thread.chapter03;

/**
 * 测试堆栈溢出
 * @author zhengwei AKA Sherlock
 * @since 2019/7/2 19:14
 */
public class TestStackOverFlow {
	private static int count = 0;

	public static void main(String[] args) {
		try {
			add(0);
		} catch (Error e) {
			e.printStackTrace();
			System.out.println(count);
		}
	}

	static void add(int num) {
		++count;
		add(num++);
	}
}
