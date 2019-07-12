package zhengwei.thread.chapter05;

import org.junit.jupiter.api.Test;

/**
 * 关于synchronized关键字
 * 被synchronized所修饰的代码块中是单线程运行的，同一个时刻只有一个线程会持有锁
 * synchronized所修饰的代码块中，从字节码层面来看的话
 * 进入synchronized代码块对应于monitorenter
 * 不论是否正常退出synchronized所修饰的代码块，都会对应一个monitorexit
 * 一个monitorenter会对应多个monitorexit
 * 但是特别注意的是，如果synchronized修饰在方法上的话，那么字节码上将不会有monitorenter和monitorexit
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/12 12:41
 */
public class ThreadSynchronized {
	@Test
	void testThreadSynchronized1() {
		SaleTicketRunnable1 saleTicketRunnable1 = new SaleTicketRunnable1();
		Thread ticket1 = new Thread(saleTicketRunnable1);
		Thread ticket2 = new Thread(saleTicketRunnable1);
		Thread ticket3 = new Thread(saleTicketRunnable1);
		ticket1.start();
		ticket2.start();
		ticket3.start();
		/*System.out.println(ticket1.isDaemon());
		System.out.println(ticket2.isDaemon());
		System.out.println(ticket3.isDaemon());*/
	}

	@Test
	void testThreadSynchronized2() {
		SaleTicketRunnable2 saleTicketRunnable2 = new SaleTicketRunnable2();
		Thread ticket1 = new Thread(saleTicketRunnable2);
		Thread ticket2 = new Thread(saleTicketRunnable2);
		Thread ticket3 = new Thread(saleTicketRunnable2);
		ticket1.start();
		ticket2.start();
		ticket3.start();
		/*System.out.println(ticket1.isDaemon());
		System.out.println(ticket2.isDaemon());
		System.out.println(ticket3.isDaemon());*/
	}
}

class SaleTicketRunnable1 implements Runnable {
	private static final Object LOCK = new Object();
	private int index = 0;
	private static final int MAX = 500;

	@Override
	public void run() {
		while (true) {
			//如果synchronized修饰的代码块的话，那么synchronized锁定的是所指定的自定义对象
			synchronized (LOCK) {
				if (index > MAX) break;
				/*try {
					Thread.sleep(1_000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				System.out.println(Thread.currentThread().getName() + "窗口销售了->" + index++ + "号票");
			}
		}
	}
}

class SaleTicketRunnable2 implements Runnable {
	private static final Object LOCK = new Object();
	private int index = 0;
	private static final int MAX = 500;

	//如果synchronized修饰的一个方法的话，那么synchronized锁定的是这个class对象
	@Override
	public synchronized void run() {
		while (true) {
			if (index > MAX) break;
			System.out.println(Thread.currentThread().getName() + "窗口销售了->" + index++ + "号票");
		}
	}
}