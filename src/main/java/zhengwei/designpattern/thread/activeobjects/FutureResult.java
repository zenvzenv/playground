package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:51
 */
public class FutureResult<E> implements Result<E> {
	private Result<E> result;
	private boolean ready = false;

	public synchronized void setResult(Result<E> result) {
		this.result = result;
		this.ready = true;
		this.notifyAll();
	}

	@Override
	public synchronized E getResultValue() {
		while (!ready) {
			try {
				this.wait();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		return this.result.getResultValue();
	}
}
