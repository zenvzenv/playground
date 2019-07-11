package zhengwei.common;

/**
 * 强制关闭Thread的工具类
 * 如何实现强制退出的？
 * 使用一个executorThread来包装要真正执行业务逻辑的线程realTask
 * 把要真正执行业务逻辑设置成守护线程(daemon)
 * 用用executorThread来启动realTask，那么realTask是executorThread的守护线程
 * 如果executorThread结束的话，那么realTask也将会结束生命周期
 * 那么我们直接把executorThread结束，realTask自然而然就结束了
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/10 19:10
 */
public class ThreadService {
	private Thread executorThread;
	private boolean finished = false;

	public void execute(Runnable task) {
		executorThread = new Thread(() -> {
			//真正要执行业务逻辑的线程
			Thread realTask = new Thread(task);
			realTask.setDaemon(true);
			realTask.start();
			try {
				//让executorThread等待realTask执行完毕
				realTask.join();
				finished = true;
			} catch (InterruptedException e) {
				//捕获到打断信号
				System.out.println("executorThread被打断，执行线程结束生命周期");
//				e.printStackTrace();
			}
		});
		executorThread.start();
	}

	public void shutdown(long mills) {
		long currentTimeMillis = System.currentTimeMillis();
		while (!finished) {
			if (System.currentTimeMillis() - currentTimeMillis >= mills) {
				System.out.println("任务超时，需要结束它了");
				//打断executorThread，让executorThread去捕获中断信号
				executorThread.interrupt();
				break;
			}
			/*try {
				executorThread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}

	public static void main(String[] args) {
		ThreadService service = new ThreadService();
		long start = System.currentTimeMillis();
		service.execute(() -> {
			//do something
			/*while (true){

			}*/
			try {
				System.out.println("开始执行真正的业务逻辑...");
				Thread.sleep(5_000L);
				System.out.println("真正的业务逻辑执行结束...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		service.shutdown(10_000L);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}
