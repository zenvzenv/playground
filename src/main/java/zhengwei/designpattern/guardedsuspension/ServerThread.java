package zhengwei.designpattern.guardedsuspension;

import java.util.Random;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/30 13:30
 */
public class ServerThread extends Thread {
	private final RequestQueue queue;
	private final Random random;
	private volatile boolean flag = true;

	public ServerThread(RequestQueue queue) {
		this.queue = queue;
		this.random = new Random(System.currentTimeMillis());
	}

	@Override
	public void run() {
		while (flag) {
			Request request = queue.getRequest();
			if (request == null) {
				continue;
			}
			System.out.println("Server -> " + request.getValue());
			try {
				Thread.sleep(this.random.nextInt(1000));
			} catch (InterruptedException e) {
//				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * 关闭服务端
	 */
	public void close() {
		this.flag = false;
		this.interrupt();
	}
}
