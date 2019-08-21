package zhengwei.designpattern.thread.workerthread;

import java.util.Random;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 13:37
 */
public class WorkerThread extends Thread {
	private final Channel channel;
	private static final Random random = new Random(System.currentTimeMillis());

	public WorkerThread(String name, Channel channel) {
		super(name);
		this.channel = channel;
	}

	@Override
	public void run() {
		while (true) {
			try {
				channel.take().execute();
				Thread.sleep(random.nextInt(1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
