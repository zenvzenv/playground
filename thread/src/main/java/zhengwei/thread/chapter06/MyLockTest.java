package zhengwei.thread.chapter06;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 测试自定义锁
 * @author zhengwei AKA Awei
 * @since 2019/7/15 13:14
 */
public class MyLockTest {
	private static void work(){
		System.out.println(Thread.currentThread().getName()+" is working");
		try {
			Thread.sleep(10_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MyLockImpl myLock=new MyLockImpl();
		Stream.of("T1","T2","T3").forEach(t->new Thread(()->{
			try {
				myLock.lock(5_000);
				Optional.of(Thread.currentThread().getName()+" is lock").ifPresent(System.out::println);
				work();
			} catch (InterruptedException | TimeOutException e) {
				e.printStackTrace();
			} finally {
				myLock.unlock();
			}
		},t).start());
	}
}
