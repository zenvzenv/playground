package zhengwei.thread.chapter10;

/**
 * 读写锁的共享数据
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/26 13:30
 */
public class SharedData {
	private final char[] buffer;
	private final ReadWriteLock lock = new ReadWriteLock();

	public SharedData(int size) {
		this.buffer = new char[size];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = '*';
		}
	}

	public char[] read() {
		try {
			lock.readLock();
			return this.doRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} finally {
			lock.readUnlock();
		}
	}

	public void write(char c) {
		try {
			lock.writeLock();
			this.doWrite(c);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.writeUnlock();
		}
	}

	private void doWrite(char c) {
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = c;
		}
		slowly(10);
	}

	private char[] doRead() {
		char[] newChar = new char[buffer.length];
		System.arraycopy(buffer, 0, newChar, 0, newChar.length);
		slowly(50);
		return newChar;
	}

	private void slowly(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
