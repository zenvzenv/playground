package zhengwei.designpattern.thread.activeobjects;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 20:39
 */
public class MakeStringClientThread<E> extends Thread {
	private final ActiveObjects<E> activeObjects;
	private final char fillChar;

	public MakeStringClientThread(ActiveObjects<E> activeObjects, String name) {
		super(name);
		this.activeObjects = activeObjects;
		this.fillChar = name.charAt(0);
	}

	@Override
	public void run() {
		try {
			for (int i = 0; true; i++) {
				Result<E> result = activeObjects.makeString(i + 1, fillChar);
				Thread.sleep(100);
				E resultValue = result.getResultValue();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
