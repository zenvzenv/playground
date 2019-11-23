package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:04
 */
public class DisplayStringRequest<E> extends MethodRequest<E> {
	private final String text;
	public DisplayStringRequest(Servant servant, String text) {
		super(servant, null);
		this.text = text;
	}

	@Override
	public void execute() {
		this.servant.displayString(text);
	}
}
