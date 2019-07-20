package zhengwei.jvm.classloader;

/**
 * 测试类的类构造器
 * 虚拟机会保证一个类的类构造器<clinit>()在多线程环境中被正确的加锁、同步，如果多个线程同时去初始化一个类，
 * 那么只会有一个线程去执行这个类的类构造器<clinit>()，其他线程都需要阻塞等待，直到活动线程执行<clinit>()方法完毕。
 * 特别需要注意的是，在这种情形下，其他线程虽然会被阻塞，但如果执行<clinit>()方法的那条线程退出后，
 * 其他线程在唤醒之后不会再次进入/执行<clinit>()方法，因为 在同一个类加载器下，一个类型只会被初始化一次。
 * 如果在一个类的<clinit>()方法中有耗时很长的操作，就可能造成多个线程阻塞，在实际应用中这种阻塞往往是隐藏的
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/20 9:08
 */
public class TestClassClinitMethod {
	static {
		System.out.println("TestClassClinitMethod init ...");
	}
	/*static {
		System.out.println(Thread.currentThread()+" enter");
		try {
			Thread.sleep(5_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread()+" exit");
	}*/
	private static class ClassClinit{
		static {
			System.out.println(Thread.currentThread()+" enter");
			try {
				Thread.sleep(5_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread()+" exit");
		}
	}

	public static void main(String[] args) {
		Runnable runnable= () -> {
			System.out.println(Thread.currentThread()+" start");
			ClassClinit t=new ClassClinit();
			System.out.println(Thread.currentThread()+" over");
		};
		new Thread(runnable,"t1").start();
		new Thread(runnable,"t2").start();
	}
}
