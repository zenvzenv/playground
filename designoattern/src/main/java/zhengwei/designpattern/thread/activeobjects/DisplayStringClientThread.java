package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:33
 */
public class DisplayStringClientThread<E> extends Thread {
	private final ActiveObjects<E> activeObjects;

	public DisplayStringClientThread(String name, ActiveObjects<E> activeObjects) {
		super(name);
		this.activeObjects = activeObjects;
	}

	@Override
	public void run() {
		for (int i = 0; true; i++) {
			try {
				String text = Thread.currentThread().getName() + " => " + i;
				activeObjects.displayString(text);
				Thread.sleep(200L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
