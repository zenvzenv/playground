package zhengwei.designpattern.thread.countdown;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * 简单模拟实现CountDownLatch
 * 等待之前的线程运行结束之后，再去执行下面的程序
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/10 14:39
 */
public class MyCountDown {
	private final int total;
	private int counter = 0;
	private static final Random random = new Random(System.currentTimeMillis());

	public MyCountDown(int total) {
		this.total = total;
	}

	public void down() {
		synchronized (this) {
			counter++;
			this.notifyAll();
		}
	}

	public void await() throws InterruptedException {
		synchronized (this) {
			while (counter != total) {
				this.wait();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		final MyCountDown countDown = new MyCountDown(5);
		IntStream.rangeClosed(1,5).forEach(i->new Thread(()->{
			try {
				System.out.println(Thread.currentThread().getName() + " is working.");
				Thread.sleep(random.nextInt(1000));
				countDown.down();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start());
		countDown.await();
		//第二阶段
		System.out.println("多线程任务结束，准备第二阶段");
		System.out.println("......");
		System.out.println("finish");
	}
}
