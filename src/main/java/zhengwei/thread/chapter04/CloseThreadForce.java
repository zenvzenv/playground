package zhengwei.thread.chapter04;

/**
 * 强制关闭线程
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/10 18:51
 */
public class CloseThreadForce {
	private Thread executeThread;
	private boolean finished = false;

	public void execute(Runnable task) {
		executeThread = new Thread(() -> {
			Thread monitor = new Thread();
			monitor.setDaemon(true);
			monitor.start();
			try {
				monitor.join();
				finished = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		executeThread.start();
	}

	public void shutdown(long mills) {
		long currentTime = System.currentTimeMillis();
		while (!finished) {
			if ((System.currentTimeMillis() - currentTime >= mills)) {
				System.out.println("任务超时，结束执行");
				executeThread.interrupt();
				break;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.out.println("执行线程被打断");
				break;
			}
		}
		finished = false;
	}

	public static void main(String[] args) {
		CloseThreadForce closeThreadForce = new CloseThreadForce();
		long start = System.currentTimeMillis();
		closeThreadForce.execute(() -> {
			//do something
			while (true) {
				//
			}
		});
		closeThreadForce.shutdown(10_000L);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}
