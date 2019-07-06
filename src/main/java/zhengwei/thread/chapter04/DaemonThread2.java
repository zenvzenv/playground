package zhengwei.thread.chapter04;

/**
 * 所谓守护线程就是子线程守护父线程(即启动该线程的线程)，如果父线程退出了，那么子线程就退出
 * 子线程默认继承父线程的daemon的级别
 * @author zhengwei AKA Sherlock
 * @since 2019/7/6 9:39
 */
public class DaemonThread2 {
	public static void main(String[] args) {
		Thread t = new Thread(() -> {
			System.out.println(Thread.currentThread().getName()+"start.");
			Thread checkHealth=new Thread(()->{
				System.out.println(Thread.currentThread().getName()+"start.");
				while (true){
					System.out.println("check health ...");
					try {
						Thread.sleep(1_000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				System.out.println(Thread.currentThread().getName()+"done.");
			},"checkHealthThread");
			checkHealth.setDaemon(true);//设置为守护线程
			checkHealth.start();

			try {
				Thread.sleep(5_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		},"T");
//		t.setDaemon(true);
		t.start();
	}
}
