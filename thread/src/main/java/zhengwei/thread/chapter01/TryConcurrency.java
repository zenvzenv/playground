package zhengwei.thread.chapter01;

/**
 * 线程
 * Java应用程序启动时，main方法就是一个线程，线程的名字就叫main
 * 想要创建一个线程的话必须要创建Thread实例，并重写Tread中的run()方法，调用start()时，会调用一个native方法start0()方法，而start0()会调用重写的run()方法
 * 线程的生命周期分为new,runnable,running,block和terminate
 * 当启动一个线程的时候，即调用线程的start()方法时，至少有两个线程，一个是启动该线程的线程和执行run()方法的线程
 * 一个线程只能被启动依次，一个线程被启动多次的话会报错
 * 如果只是调用了Thread中的run()方法而不是调用Thread的start()，那么这就相当于调用一个普通实体的方法一样，并不会启动该线程。
 * @author zhengwei AKA Sherlock
 * @since 2019/6/22 19:25
 */
public class TryConcurrency {
	private static void readDB(){
		System.out.println("start to read database...");
		try {
			Thread.sleep(1000*10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("end to read database...");
	}
	private static void writeToFile(){
		System.out.println("start to write file...");
		try {
			Thread.sleep(1000*10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("end to write file...");
	}

	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getName());
		new Thread(()->{
			System.out.println("thread name -> "+Thread.currentThread().getName());
			readDB();
		},"readDB").start();
		new Thread(()->{
			System.out.println("thread name -> "+Thread.currentThread().getName());
			writeToFile();
		},"writeToFile").start();
	}
}
