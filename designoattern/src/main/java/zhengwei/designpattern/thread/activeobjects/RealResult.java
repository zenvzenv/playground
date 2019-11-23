package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:48
 */
public class RealResult<E> implements Result<E> {
	private final E resultValue;

	public RealResult(E resultValue) {
		this.resultValue = resultValue;
	}

	@Override
	public E getResultValue() {
		return this.resultValue;
	}
}
