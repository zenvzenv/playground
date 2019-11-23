package zhengwei.thread.chapter05;

import java.util.stream.Stream;

/**
 * 生产者消费者
 * 单生产者，单消费者在此案例中是可以正常运行的
 * 但是多生产者，多消费者再次案例中会有问题，最后所有的线程都会wait住，放弃CPU的执行权，等待被notify
 * 因为我们并不知道调用notify之后，JVM会去唤醒哪个线程，最后出现的情况就是四个线程都wait了
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/13 9:56
 */
public class ProducerConsumerV1 {
	private int i = 0;
	private static final Object LOCK = new Object();
	//是否已经生产了
	private static volatile boolean isProduced = false;

	/**
	 * 生产
	 */
	private void produce() {
		synchronized (LOCK) {
			//如果已经生产了，那么就等待
			if (isProduced) {
				try {
					LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//如果没有生产或者已被消费，那么就去生产并通知消费者
			} else {
				i++;
				System.out.println(Thread.currentThread().getName() + " produce " + i);
				LOCK.notify();
				isProduced = true;
			}
		}
	}

	/**
	 * 消费
	 */
	private void consume() {
		synchronized (LOCK) {
			//如果生产者已经生产了，那么就去消费，并通知生产者生产
			if (isProduced) {
				System.out.println(Thread.currentThread().getName() + " consume " + i);
				//设置生产标志位为未生产
				isProduced = false;
				//通知生产者
				LOCK.notify();
			} else {
				try {
					//没有生产的话就等待
					LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		ProducerConsumerV1 pc = new ProducerConsumerV1();
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
		Stream.of("P1", "P2").forEach(p -> {
			new Thread(() -> {
				while (true) {
					pc.produce();
				}
			}, p).start();
		});
		//多消费者
		Stream.of("C1", "C2").forEach(c -> {
			new Thread(() -> {
				while (true) {
					pc.consume();
				}
			}, c).start();
		});
	}
}
