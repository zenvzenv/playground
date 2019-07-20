package zhengwei.designpattern.singleton;

import java.util.Objects;

/**
 * 懒汉式
 * 虽然解决了按需加载对象，但是会有线程不安全的现象发生
 * 需要进一步的优化
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/5/14 13:24
 */
public class Singleton02 {
	/*
	volatile关键字遵循happens-before原则
	就是在读到被volatile修饰的变量的时候，在此之前必须要有写操作
	 */
	private static volatile Singleton02 INSTANCE;

	//私有化构造器
	private Singleton02() {
	}

	//不加synchronized关键字，会有线程安全问题
	public static Singleton02 getInstance01() {
		if (Objects.isNull(INSTANCE)) {
			try {
				//线程睡一秒，模拟业务
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			INSTANCE = new Singleton02();
		}
		return INSTANCE;
	}

	//加了synchronized关键字之后解决了线程安全问题，但是效率会下降
	public static synchronized Singleton02 getInstance02() {
		if (Objects.isNull(INSTANCE)) {
			try {
				//线程睡1秒，模拟业务逻辑
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			INSTANCE = new Singleton02();
		}
		return INSTANCE;
	}

	//局部加锁，还是会有线程安全问题，因为可能会有多个线程都回进入到判断条件中，同时等待同一把锁，锁释放之后还是会new出对象
	public static Singleton02 getInstance03() {
		if (Objects.isNull(INSTANCE)) {
			synchronized (Singleton02.class) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				INSTANCE = new Singleton02();
			}
		}
		return INSTANCE;
	}

	//双重检查，解决线程安全问题
	//前提是单例对象没有被volatile修饰，但是可能会有NullPointException，因为JVM在编译期间对我们的代码进行一些优化和在运行期间对代码的优化
	//导致执行顺序并不是我们想像中那样去执行的
	public static Singleton02 getInstance04() {
		if (Objects.isNull(INSTANCE)) {
			synchronized (Singleton02.class) {
				if (Objects.isNull(INSTANCE)) {
					INSTANCE = new Singleton02();
				}
			}
		}
		return INSTANCE;
	}
}
