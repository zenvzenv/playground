package zhengwei.designpattern.thread.twophasetermination;

import java.util.Random;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/12 12:50
 */
public class CounterIncrement extends Thread {
	private volatile boolean termination = false;
	private int count = 0;
	private static final Random random = new Random(System.currentTimeMillis());

	@Override
	public void run() {
		try {
			while (!this.termination) {
				System.out.println(Thread.currentThread().getName() + " " + count++);
				Thread.sleep(random.nextInt(1000));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {//第二阶段，释放和清理资源
			this.clean();
		}
	}

	/**
	 * 第二阶段，释放和清理资源
	 */
	private void clean() {
		System.out.println("do some clean work for the second phase,current counter " + count);
	}

	public void close() {
		this.termination = true;
		this.interrupt();
	}
}
