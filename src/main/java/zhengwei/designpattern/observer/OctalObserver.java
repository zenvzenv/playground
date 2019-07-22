package zhengwei.designpattern.observer;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/22 19:44
 */
public class OctalObserver extends Observer {
	public OctalObserver(Subject subject) {
		super(subject);
	}

	@Override
	public void update() {
		System.out.println("Octal Number ->"+Integer.toOctalString(subject.getState()));
	}
}
