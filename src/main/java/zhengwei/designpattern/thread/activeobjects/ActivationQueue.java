package zhengwei.designpattern.thread.activeobjects;


import java.util.LinkedList;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:07
 */
public class ActivationQueue {
	private static final int MAX_METHOD_REQUEST_QUEUE_SIZE = 100;
	private final LinkedList<MethodRequest> methodQueue;

	public ActivationQueue() {
		this.methodQueue = new LinkedList<>();
	}

	public synchronized void put(MethodRequest request) {
		while (methodQueue.size() >= MAX_METHOD_REQUEST_QUEUE_SIZE) {
			try {
				this.wait();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		this.methodQueue.addLast(request);
		this.notifyAll();
	}

	public synchronized MethodRequest take() {
		while (methodQueue.isEmpty()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		MethodRequest methodRequest = methodQueue.removeFirst();
		this.notifyAll();
		return methodRequest;
	}
}
