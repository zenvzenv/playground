package zhengwei.thread.chapter10;

/**
 * 读写锁
 * 读读操作不互斥
 * 读写操作互斥
 * 写写操作互斥
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/26 13:10
 */
public class ReadWriteLock {
	//当前有几个线程对其读操作
	private int readingReaders = 0;
	//线程想读但是读不了的线程个数
	private int waitingReaders = 0;
	//有几个线程正在写，最多有一个
	private int writingWriters = 0;
	private int waitingWriters = 0;
	private boolean preferWriter = true;

	public ReadWriteLock(boolean preferWriter) {
		this.preferWriter = preferWriter;
	}

	public ReadWriteLock() {
		this(true);
	}

	/**
	 * 读锁
	 * 当有线程进行写操作时就阻塞
	 * 没有线程进行写操作的时候或读锁被释放的时候就加一个读线程
	 *
	 * @throws InterruptedException 中断异常
	 */
	public synchronized void readLock() throws InterruptedException {
		this.waitingReaders++;
		try {
			while (this.writingWriters > 0 || preferWriter && waitingWriters > 0) {
				this.wait();
			}
			this.readingReaders++;
		} finally {
			this.waitingReaders--;
		}
	}

	/**
	 * 释放读锁
	 */
	public synchronized void readUnlock() {
		this.readingReaders--;
		this.notifyAll();
	}

	/**
	 * 写锁
	 * 只要有线程在读或有线程在写，都会被阻塞
	 *
	 * @throws InterruptedException 中断线程
	 */
	public synchronized void writeLock() throws InterruptedException {
		this.waitingWriters++;
		try {
			while (writingWriters > 0 || readingReaders > 0) {
				this.wait();
			}
			this.writingWriters++;
		} finally {
			this.waitingWriters--;
		}
	}

	/**
	 * 释放写锁
	 */
	public synchronized void writeUnlock() {
		this.writingWriters--;
		this.notifyAll();
	}
}
