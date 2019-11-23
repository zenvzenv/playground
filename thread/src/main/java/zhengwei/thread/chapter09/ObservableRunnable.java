package zhengwei.thread.chapter09;

/**
 * Thread状态观察类
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/23 12:35
 */
public abstract class ObservableRunnable implements Runnable {
	final protected LifecycleListener lifecycleListener;

	public ObservableRunnable(final LifecycleListener lifecycleListener) {
		this.lifecycleListener = lifecycleListener;
	}

	/**
	 * 触发通知
	 *
	 * @param runnableEvent 线程事件
	 */
	protected void notifyChange(final RunnableEvent runnableEvent) {
		lifecycleListener.onEvent(runnableEvent);
	}

	/**
	 * 线程运行状态枚举
	 */
	public enum RunnableState {
		RUNNING, ERROR, DONE;
	}

	public static class RunnableEvent {
		private final RunnableState state;
		private final Thread thread;
		private final Throwable throwable;

		public RunnableEvent(RunnableState state, Thread thread, Throwable throwable) {
			this.state = state;
			this.thread = thread;
			this.throwable = throwable;
		}

		public RunnableState getState() {
			return state;
		}

		public Thread getThread() {
			return thread;
		}

		public Throwable getThrowable() {
			return throwable;
		}
	}
}
