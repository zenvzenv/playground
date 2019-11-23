package zhengwei.thread.chapter08;

import java.util.stream.IntStream;

/**
 * WaitSet：线程的休息室
 * 当线程调用wait()方法的时候，那么该线程就会进入到WaitSet中
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/20 8:18
 */
public class WaitSet {
	private static final Object LOCK=new Object();
	private static void work(){
		synchronized (LOCK){
			System.out.println("begin ...");
			try {
				System.out.println("enter ...");
				LOCK.wait();
				System.out.println("out ...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		new Thread(WaitSet::work).start();
		Thread.sleep(1_000L);
		synchronized (LOCK){
			LOCK.notify();
		}
		/*
		1.所有的对象都存在一个WaitSet，用来存放调用了该对象wait方法之后进入block状态
		2.线程被notify之后，不一定会得到立即执行，此时状态还是block，需要获得锁之后进入runnable状态等待CPU的调度
			3.线程从wait set中被唤醒的顺序是不一定的(即synchronized是非公平锁)
		4.线程从wait set中被唤醒后，必须重新获取锁，并且不会从synchronized代码块开头执行，而是从wait处继续执行
		 */
		/*IntStream.rangeClosed(1,10)
				.forEach(i->new Thread(()->{
					synchronized (LOCK){
						try {
							System.out.println(Thread.currentThread().getName()+" will enter wait set");
							LOCK.wait();
							System.out.println(Thread.currentThread().getName()+" will leave wait set");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start());
		try {
			Thread.sleep(2_000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		IntStream.rangeClosed(1,10)
				.forEach(i->{
					synchronized (LOCK){
						LOCK.notify();
						try {
							Thread.sleep(1_000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});*/
	}
}
