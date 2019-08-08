package zhengwei.designpattern.consumerproductor;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/8 13:46
 */
public class ProducerThread extends Thread {
	private final MessageQueue messageQueue;
	private static final Random r = new Random(System.currentTimeMillis());
	private static final AtomicInteger counter = new AtomicInteger(0);

	public ProducerThread(MessageQueue messageQueue, int seq) {
		super("producer-" + seq);
		this.messageQueue = messageQueue;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Message message = new Message("mwssage-" + counter.getAndDecrement());
				messageQueue.put(message);
				System.out.println(Thread.currentThread().getName() + " - put a message -> " + message.getMessage());
				Thread.sleep(r.nextInt(1000));
			} catch (InterruptedException e) {
//				e.printStackTrace();
				break;
			}
		}
	}
}
