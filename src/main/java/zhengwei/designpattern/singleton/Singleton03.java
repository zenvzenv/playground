package zhengwei.designpattern.singleton;

/**
 * 静态内部类的方式
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/5/15 12:58
 */
public class Singleton03 {

	private Singleton03() {
	}

	/*
	一个类只会被JVM加载一次和初始化一次，一个类的静态变量在JVM中也只会只有一份，这由JVM来保证
	 */
	private static class Singleton04Handler {
		private final static Singleton03 INSTANCE = new Singleton03();
	}
	/*
	懒加载->一个类只有在被主动使用的时候才会去真正初始化它，即为类变量去赋我们指定的初始值，这就实现了懒加载
	 */
	public static Singleton03 getInstance() {
		return Singleton04Handler.INSTANCE;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			new Thread(() -> System.out.println(Singleton03.getInstance().hashCode())).start();
		}
	}
}
