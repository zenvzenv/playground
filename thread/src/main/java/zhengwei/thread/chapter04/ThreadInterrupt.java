package zhengwei.thread.chapter04;

import org.junit.jupiter.api.Test;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/7/6 14:24
 */
public class ThreadInterrupt {
	private static final Object MONITOR=new Object();
	public static void main(String[] args) throws InterruptedException {
		Thread t=new Thread(()->{
			try {
				Thread.sleep(1_000L);
				while (true) {
					//由我们来根据中断标志来判断线程接下来的操作
					if (Thread.interrupted()){
						System.out.println("thread is interrupted ");
						break;
					}
					System.out.println("thread is not interrupt");
				}
			} catch (InterruptedException e) {
				System.out.println("interrupt is "+Thread.interrupted());
				e.printStackTrace();
			}
		});
		t.start();
		System.out.println("interrupt status "+t.isInterrupted());
		Thread.sleep(20_000L);
		/*
		1.对于NEW和TERMINATED状态的线程来说，调用interrupt是没有任何意义的，Java会对这两种状态的线程忽略interrupt
		2.对于RUNNABLE状态的线程(没有获取到CPU执行权的线程)，调用interrupt之后，只是设置了该线程的中断标志，并不会让线程实际中断，想要发现线程是否真的中断需要用去程序去判断
		  那么问题来了，我们既然不能真正的中断，那么要我们要中断标志干嘛呢？
		  这里其实是把线程的生杀大权交给我们自己去判断，Java给我们一个标志位来让让我们自己去决策接下来要干嘛，我们可以判断标志位然后去中断我们的程序而不是强制的终止系统
		3.对于BLOCKED状态(竞争CPU执行权失败而被挂起的线程)的线程来说，发起interrupt方法只是会设置中断的标志位，并不会对线程造成影响
		4.WAITING和TIMED_WAITING状态(sleep,wait,join...)的线程，在触发interrupt的时候会抛出InterruptedException异常，同时会设置中断标志
		 */
		t.interrupt();
		System.out.println("interrupt status "+t.isInterrupted());
	}

	@Test
	void testInterrupt(){
		Thread currentThread = Thread.currentThread();
		Thread t1=new Thread(()->{
			try {
				System.out.println("11111111111");
				Thread.sleep(10_000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		Thread t2=new Thread(()->{
			try {
				Thread.sleep(5_000L);
				currentThread.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		t1.start();
		t2.start();
		try {
			//需要注意的是join这个方法，是当前线程(t1的父线程)去等待t1执行结束，要中断的话，需要中断t1父线程的interrupt
			t1.join();
		} catch (InterruptedException e) {
			System.out.println("t1 is interrupt");
			e.printStackTrace();
		}
	}
}
