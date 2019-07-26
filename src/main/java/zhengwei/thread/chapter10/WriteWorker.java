package zhengwei.thread.chapter10;

import java.util.Random;

/**
 * 读线程
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/26 13:49
 */
public class WriteWorker extends Thread {
	private static final Random random = new Random(System.currentTimeMillis());
	private final SharedData sharedData;
	private final String filler;
	private int index;

	public WriteWorker(SharedData sharedData, String filler) {
		this.sharedData = sharedData;
		this.filler = filler;
	}

	@Override
	public void run() {
		try {
			while (true) {
				char c = nextChar();
				sharedData.write(c);
				Thread.sleep(1000L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private char nextChar() {
		char c = filler.charAt(index);
		index++;
		if (index >= filler.length()) {
			index = 0;
		}
		return c;
	}
}
