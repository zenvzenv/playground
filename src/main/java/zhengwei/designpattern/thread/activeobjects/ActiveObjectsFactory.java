package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:27
 */
public final class ActiveObjectsFactory<E> {
	public ActiveObjectsFactory() {
	}

	public ActiveObjects<E> createActiveObjects() {
		Servant servant = new Servant();
		ActivationQueue queue = new ActivationQueue();
		SchedulerThread schedulerThread = new SchedulerThread(queue);
		ActiveObjectsProxy<E> proxy = new ActiveObjectsProxy<>(schedulerThread, servant);
		schedulerThread.start();
		return proxy;
	}
}
