package zhengwei.thread.chapter10;

/**
 * 写操作线程
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/26 13:49
 */
public class ReadWorker extends Thread {
	private final SharedData sharedData;

	public ReadWorker(SharedData sharedData) {
		this.sharedData = sharedData;
	}

	@Override
	public void run() {
		try {
			while (true) {
				char[] readBuffer = sharedData.read();
				System.out.println(Thread.currentThread().getName() + " reads " + String.valueOf(readBuffer));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
