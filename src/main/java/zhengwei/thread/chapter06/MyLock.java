package zhengwei.thread.chapter06;

import java.util.Collection;

/**
 * 自定义锁接口
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/15 12:45
 */
public interface MyLock {
	//锁定，允许被打断
	void lock() throws InterruptedException;

	//锁定，并知道等待时间，如果超过指定的等待时长则抛出超时异常，并允许被打断
	void lock(long mills) throws TimeOutException, InterruptedException;

	//解锁
	void unlock();

	//获取被阻塞线程的集合
	Collection<Thread> getBlockedThread();

	//获取阻塞的大小
	int getBlockedSize();
}

class TimeOutException extends Exception {
	public TimeOutException(String message) {
		super(message);
	}
}