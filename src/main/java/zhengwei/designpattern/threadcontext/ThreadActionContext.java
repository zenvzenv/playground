package zhengwei.designpattern.threadcontext;

/**
 * 单例
 * 需要注意的是如果是使用的线程池的情况下，ThreadLocal可能会遗留上个线程的一些数据
 * 因为虽然线程是相同的，但是要执行的内容是不同的
 * 所以在线程池的情况下，需要先对ThreadLocal中的数据先进行清理工作，再去使用，以面发生意外
 * @author zhengwei AKA Awei
 * @since 2019/8/3 8:20
 */
public final class ThreadActionContext {
	private static final ThreadLocal<ThreadContext> threadLocal = ThreadLocal.withInitial(ThreadContext::new);

	private static class ContextHolder {
		private static final ThreadActionContext actionContext = new ThreadActionContext();
	}

	public static ThreadActionContext getInstance() {
		return ContextHolder.actionContext;
	}

	public ThreadContext getContext() {
		return threadLocal.get();
	}

	/**
	 * 在线程池模式下清理上个相同线程遗留数据
	 */
	public void cleanThreadLocal(){
		threadLocal.remove();
	}
}
