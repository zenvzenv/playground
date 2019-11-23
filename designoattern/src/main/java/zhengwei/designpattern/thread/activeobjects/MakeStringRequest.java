package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:57
 */
public class MakeStringRequest<E> extends MethodRequest<E> {
	private final int count;
	private final char fillChar;

	public MakeStringRequest(Servant servant, FutureResult<E> futureResult, int count, char fillChar) {
		super(servant, futureResult);
		this.count = count;
		this.fillChar = fillChar;
	}

	@Override
	public void execute() {
		Result result = servant.makeString(count, fillChar);
		futureResult.setResult(result);
	}
}
