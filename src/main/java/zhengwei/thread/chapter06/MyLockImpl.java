package zhengwei.thread.chapter06;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 自定义锁，synchronized表记在实例方法上，锁定的是this对象
 * 可以实现上锁
 * 上锁等待的时间
 * 解锁
 * 获取Blocked的Thread的集合
 * 获得Blocked的Thread的数量
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/15 12:51
 */
public class MyLockImpl implements MyLock {
	/*
	true->锁已经被线程获取
	false->锁没有被线程获取(默认)
	 */
	private boolean isLock = false;
	private Collection<Thread> blockedThreadCollection = new ArrayList<>();
	//标记是哪个线程持有锁，只有这个线程才能去释放锁
	private Thread currentThread;

	@Override
	public synchronized void lock() throws InterruptedException {
		while (isLock) {
			blockedThreadCollection.add(Thread.currentThread());
			this.wait();
		}
		if (blockedThreadCollection.contains(Thread.currentThread())){
			blockedThreadCollection.remove(Thread.currentThread());
		}
		isLock = true;
		//标记获得锁的线程
		this.currentThread = Thread.currentThread();
	}

	@Override
	public synchronized void lock(long mills) throws TimeOutException, InterruptedException {
		long end = System.currentTimeMillis() + mills;
		while (isLock) {
			long remain = end - System.currentTimeMillis();
			if (remain <= 0) {
				throw new TimeOutException(Thread.currentThread().getName()+" wait lock time out");
			}
			this.wait(mills);
		}
		//标记获得锁的线程
		this.currentThread = Thread.currentThread();
		this.isLock=true;
	}

	@Override
	public synchronized void unlock() {
		//判断是否是同一个线程去释放锁
		if (Thread.currentThread() == this.currentThread) {
			this.isLock = false;
			Optional.of(Thread.currentThread().getName() + " release the lock monitor").ifPresent(System.out::println);
			this.notifyAll();
		}
	}

	@Override
	public Collection<Thread> getBlockedThread() {
		return Collections.unmodifiableCollection(blockedThreadCollection);
	}

	@Override
	public int getBlockedSize() {
		return blockedThreadCollection.size();
	}
}
