package zhengwei.thread.chapter09;

import zhengwei.util.common.SysUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/23 12:45
 */
public class ThreadLifecycleObserver implements LifecycleListener {
	private static final Object LOCK = new Object();

	public void concurrentQuery(List<String> ids) {
		if (SysUtils.isNull(ids)) return;
		ids.forEach(id -> new Thread(new ObservableRunnable(this) {
			@Override
			public void run() {
				try {
					notifyChange(new RunnableEvent(RunnableState.RUNNING, Thread.currentThread(), null));
					System.out.println("query for the id->" + id);
					Thread.sleep(1_000L);
					notifyChange(new RunnableEvent(RunnableState.DONE, Thread.currentThread(), null));
				} catch (Exception e) {
					notifyChange(new RunnableEvent(RunnableState.ERROR, Thread.currentThread(), e));
				}
			}
		}, id).start());
	}

	@Override
	public void onEvent(ObservableRunnable.RunnableEvent runnableEvent) {
		synchronized (LOCK) {
			System.out.println("The runnable [" + runnableEvent.getThread().getName() + "] data change and state is [" + runnableEvent.getState() + "]");
			if (runnableEvent.getState().equals(ObservableRunnable.RunnableState.ERROR)) {
				System.out.println("The runnable [" + runnableEvent.getThread().getName() + "] process failed");
				System.out.println(Optional.of(runnableEvent.getThrowable().getStackTrace()));
			}
		}
	}
}
