package zhengwei.thread.chapter05;

import java.util.stream.Stream;

/**
 * 多线程正常运行的生产者消费者
 * wait、notify和notifyAll都是Object提供的方法，而不是Thread中提供的方法
 * 注意：强烈建议wait()和while()进行搭配使用，而不要和if()进行配合使用
 *      因为while循环想要跳出循环时，会再进行一次判断，如果一个线程对条件进行了修改的话，再进行一次判断将不会出错；
 *      而if判断只进行一次判断，即使别的线程对要判断的值进行了修改，那么后续将不会对条件进行判断，会出错
 * JVM会未一个使用内部锁(synchronized)的对象维护两个集合，Entry Set和Wait Set
 * 对于Entry Set：如果线程A已经持有了对象锁，此时如果有别的线程想要获得锁的话，那么这个线程就会进入Entry Set，并且出去BLOCKED状态
 * 对于Wait Set：如果线程A调用了wait()方法，那么线程会释放持有的对象锁，并进入Wait Set，并且处于WAITING状态
 *
 * 如果线程B想要获取对象锁，一般情况下有两个先决条件
 *  1.对象锁已被释放(如曾经持有锁的线程执行完了synchronized的代码块或者调用了wait()方法)
 *  2.线程B已处于RUNNABLE状态
 *
 * 对于Entry Set中的线程，当对象释放锁的时候，JVM会唤醒Entry Set中的一个线程，整个线程的状态从BLOCKED变成RUNNABLE状态，等待CPU调度
 * 对于Wait Set中的线程，当对象的notify()方法被调用是，JVM会唤醒Wait Set中的某一个线程，这个线程的状态从WAITING变成BLOCKED状态，获得锁之后从BLOCKED变成RUNNABLE状态
 * 而notifyAll()方法，则是唤醒Wait Set中的所有线程，所有的线程都被移入到Entry Set中，状态从WAITING变成BLOCKED状态，然后所有线程去竞争锁，获得锁之后从BLOCKED状态变成RUNNABLE状态
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/13 10:49
 */
public class ProducerConsumerV2 {
	private int i = 0;
	private static final Object LOCK = new Object();
	//是否已经生产了
	private static volatile boolean isProduced = false;

	/**
	 * 生产
	 */
	private void produce() {
		synchronized (LOCK) {
			while (isProduced) {
				try {
					LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			i++;
			System.out.println(Thread.currentThread().getName() + " produce " + i);
			LOCK.notifyAll();
			isProduced = true;
		}
	}

	/**
	 * 消费
	 */
	private void consume() {
		synchronized (LOCK) {
			while (!isProduced) {
				try {
					//没有生产的话就等待
					LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println(Thread.currentThread().getName() + " consume " + i);
			//设置生产标志位为未生产
			isProduced = false;
			//通知生产者
			LOCK.notifyAll();
		}
	}

	public static void main(String[] args) {
		ProducerConsumerV2 pc = new ProducerConsumerV2();
		//单生产者
		/*new Thread(() -> {
			while (true) {
				pc.produce();
			}
		}, "Producer").start();*/
		//单消费者
		/*new Thread(() -> {
			while (true) {
				pc.consume();
			}
		}, "Consumer").start();*/
		//多生产者
		Stream.of("P1", "P2", "P3", "P4").forEach(p -> {
			new Thread(() -> {
				while (true) {
					pc.produce();
				}
			}, p).start();
		});
		//多消费者
		Stream.of("C1", "C2", "C3", "C4", "C5").forEach(c -> {
			new Thread(() -> {
				while (true) {
					pc.consume();
				}
			}, c).start();
		});
	}
}
