package zhengwei.thread.chapter04;

/**
 * 守护线程
 * 线程的几种状态：new -> runnable -> running -> block -> dead
 * JVM通过以ThreadGroup为单位来感知是否还有active的线程，直到所有的线程都死掉之后，JVM才会退出
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/5 13:34
 */
public class DaemonThread {
	public static void main(String[] args) {
		//new是新生状态
		Thread t = new Thread(() -> {
			System.out.println(Thread.currentThread().getName());
			try {
				//阻塞状态
				Thread.sleep(1000_10L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName());
		});
		t.setDaemon(true);//设置成守护线程，main线程结束，该线程也随之结束，设置是否是daemon需要在start之前
		t.start();//调用start方法，线程进入runnable状态，等待cpu的调度
		try {
			Thread.sleep(10_1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName());
	}
}
