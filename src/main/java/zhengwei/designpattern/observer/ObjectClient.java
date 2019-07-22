package zhengwei.designpattern.observer;

/**
 * 观察者客户端
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/22 19:45
 */
public class ObjectClient {
	public static void main(String[] args) {
		Subject subject = new Subject();
		new BinaryObserver(subject);
		new OctalObserver(subject);
		System.out.println("=============");
		//subject自动去通知observer
		subject.setState(10);
		System.out.println("=============");
		//subject自动去通知observer
		subject.setState(11);
		System.out.println("=============");
		//subject自动去通知observer
		subject.setState(12);
	}
}
