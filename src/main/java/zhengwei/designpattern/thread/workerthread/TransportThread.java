package zhengwei.designpattern.thread.workerthread;

import java.util.Random;

/**
 * 搬运线程
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:10
 */
public class TransportThread extends Thread {
	private final Channel channel;
	private static final Random random = new Random(System.currentTimeMillis());

	public TransportThread(String name, Channel channel) {
		super(name);
		this.channel = channel;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; true; i++) {
				Request request = new Request(getName(), i);
				this.channel.put(request);
				Thread.sleep(random.nextInt(1000));
			}
		} catch (InterruptedException e) {
//			e.printStackTrace();
		}
	}
}
