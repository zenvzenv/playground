package zhengwei.designpattern.thread.activeobjects;

/**
 * 对应ActiveObjects中的每一个方法
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:42
 */
public abstract class MethodRequest<E> {
	protected final Servant servant;
	protected final FutureResult<E> futureResult;

	public MethodRequest(Servant servant, FutureResult<E> futureResult) {
		this.servant = servant;
		this.futureResult = futureResult;
	}

	public abstract void execute();
}
