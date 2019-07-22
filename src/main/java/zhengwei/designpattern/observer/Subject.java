package zhengwei.designpattern.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/22 19:36
 */
public class Subject {
	private List<Observer> observers = new ArrayList<>();
	//发生变化的东西
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		if (state == this.state) {
			return;
		}
		this.state = state;
		notifyAllObserver();
	}

	public void attach(Observer observer) {
		observers.add(observer);
	}

	private void notifyAllObserver() {
		observers.forEach(Observer::update);
	}
}
