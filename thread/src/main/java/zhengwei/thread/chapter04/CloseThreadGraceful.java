package zhengwei.thread.chapter04;

/**
 * 优雅的关闭线程
 * Thread没有阻塞在 `while` 的判断循环体中
 * Thread还有机会来判断中断标志位，来判断是否要去结束线程
 *
 * 如果线程阻塞在 `while` 的判断循环体中的话
 * 那么Thread是没有机会去判断中断标志位的，即使我们去手动的interrupt了线程，线程也没有机会去判断标志位
 * 这时就要去强制打断线程
 *
 * @author zhengwei AKA Sherlock Awei
 * @since 2019/7/9 13:18
 */
public class CloseThreadGraceful {
	private static class Worker extends Thread {
		private volatile boolean start = true;

		@Override
		public void run() {
			while (start) {
				//do something
			}
		}

		public void shutdown() {
			this.start = false;
		}
	}

	private static class Worker2 extends Thread{
		@Override
		public void run() {
			//判断中断标志位，如果是中断状态的话就退出
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException e) {
					break;//return
				}
				//-------后续操作--------
			}
		}
	}

	public static void main(String[] args) {
		Worker worker=new Worker();
		worker.start();
		try {
			Thread.sleep(10_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		worker.shutdown();
	}
}
