package zhengwei.designpattern.future;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/27 14:04
 */
public class AsyncFuture<T> implements Future<T> {
	//判断业务逻辑是否结束了
	private volatile boolean done = false;
	private T result;

	/**
	 * 业务逻辑执行结束，通知调用者
	 *
	 * @param result 最终结果
	 */
	public void done(T result) {
		synchronized (this) {
			this.result = result;
			this.done = true;
			this.notifyAll();
		}
	}

	@Override
	public T get() throws InterruptedException {
		synchronized (this) {
			while (!done) {
				this.wait();
			}
		}
		return result;
	}
}
