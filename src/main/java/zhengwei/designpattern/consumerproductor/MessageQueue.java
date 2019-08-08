package zhengwei.designpattern.consumerproductor;

import java.util.LinkedList;

/**
 * 先线程安全但不是不可更改的
 * @author zhengwei AKA Awei
 * @since 2019/8/8 13:36
 */
public class MessageQueue {
	private final LinkedList<Message> queue;
	private static final int DEFAULT_MAX_LIMIT = 100;
	private final int limit;

	public MessageQueue() {
		this(DEFAULT_MAX_LIMIT);
	}

	public MessageQueue(int limit) {
		this.limit = limit;
		this.queue = new LinkedList<>();
	}

	public void put(Message message) throws InterruptedException {
		synchronized (queue) {
			while (queue.size() > limit) {
				queue.wait();
			}
			queue.addLast(message);
			queue.notifyAll();
		}
	}

	public Message take() throws InterruptedException {
		synchronized (queue) {
			while (queue.isEmpty()) {
				queue.wait();
			}
			Message message = queue.removeFirst();
			queue.notifyAll();
			return message;
		}
	}

	public int getMaxLimit() {
		return this.limit;
	}

	public int getMessageSize() {
		synchronized (queue) {
			return queue.size();
		}
	}
}
