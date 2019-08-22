package zhengwei.designpattern.thread.activeobjects;

/**
 * 定时线程
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:14
 */
public class SchedulerThread extends Thread {
	private final ActivationQueue activationQueue;

	public SchedulerThread(ActivationQueue activationQueue) {
		this.activationQueue = activationQueue;
	}

	public void invoke(MethodRequest request) {
		this.activationQueue.put(request);
	}

	@Override
	public void run() {
		while (true) {
			activationQueue.take().execute();
		}
	}
}
