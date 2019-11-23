package zhengwei.designpattern.observer;

/**
 * 观察者
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/22 19:36
 */
public abstract class Observer {
	public Observer(Subject subject) {
		this.subject = subject;
		subject.attach(this);
	}

	protected Subject subject;
	public abstract void update();
}
