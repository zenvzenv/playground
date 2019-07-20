package zhengwei.designpattern.singleton;

/**
 * 饿汉式
 * 方便实用，并且线程安全，一个类只会被JVM加载一次，所以一个类只会被初始化一次
 * 当该类被首次主动使用的时候，该类会被加载并初始化，静态变量就会被初始化
 * 所以不论获取几次对象，始终是同一个对象
 * 缺点是不论这个对象的单例对象是否被用到，只要设计到该类的一个主动使用(对类其他静态变量的操作，作为其他类的一个父类...)，JVM中都会加载这个对象的实例
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/5/14 13:14
 */
public class Singleton01 {
	//实例化对象
	private static final Singleton01 INSTANCE = new Singleton01();

	//私有化构造器
	private Singleton01() {
	}

	public static Singleton01 getInstance() {
		return INSTANCE;
	}

	public static void main(String[] args) {
		Singleton01 instance01 = Singleton01.getInstance();
		Singleton01 instance02 = Singleton01.getInstance();
		System.out.println(instance01 == instance02);
	}
}
