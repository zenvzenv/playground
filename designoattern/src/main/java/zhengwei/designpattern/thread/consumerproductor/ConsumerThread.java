package zhengwei.designpattern.thread.consumerproductor;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/8 19:24
 */
public class ConsumerThread extends Thread {
	private final MessageQueue messageQueue;
	private static final Random r = new Random(System.currentTimeMillis());
	private static final AtomicInteger counter = new AtomicInteger(0);

	public ConsumerThread(MessageQueue messageQueue, int seq) {
		super("consumer-" + seq);
		this.messageQueue = messageQueue;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Message message = messageQueue.take();
				messageQueue.put(message);
				System.out.println(Thread.currentThread().getName() + " - take a message -> " + message.getMessage());
				Thread.sleep(1_000L);
			} catch (InterruptedException e) {
//				e.printStackTrace();
				break;
			}
		}
	}
}
