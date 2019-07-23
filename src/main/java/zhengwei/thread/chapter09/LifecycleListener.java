package zhengwei.thread.chapter09;

/**
 * Thread生命周期监听器
 * @author zhengwei AKA Awei
 * @since 2019/7/23 12:36
 */
public interface LifecycleListener {
	void onEvent(ObservableRunnable.RunnableEvent runnableEvent);
}
