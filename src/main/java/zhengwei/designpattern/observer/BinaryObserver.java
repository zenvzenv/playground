package zhengwei.designpattern.observer;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/22 19:40
 */
public class BinaryObserver extends Observer {
	public BinaryObserver(Subject subject) {
		super(subject);
	}

	@Override
	public void update() {
		System.out.println("Binary Number ->" + Integer.toBinaryString(subject.getState()));
	}
}
