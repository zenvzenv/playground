package zhengwei.designpattern.thread.countdown;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * count down设计模式
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/10 14:28
 */
public class CountDown {
	private static final Random random = new Random(System.currentTimeMillis());

	public static void main(String[] args) throws InterruptedException {
		//门闩
		final CountDownLatch countDownLatch = new CountDownLatch(5);
		System.out.println("准备多线程处理任务");
		//第一阶段
		IntStream.rangeClosed(1, 5).forEach(i -> new Thread(() -> {
			try {
				System.out.println(Thread.currentThread().getName() + " is working.");
				Thread.sleep(random.nextInt(1000));
				countDownLatch.countDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, String.valueOf(i)).start());
		countDownLatch.await();
		//第二阶段
		System.out.println("多线程任务结束，准备第二阶段");
		System.out.println("......");
		System.out.println("finish");
	}
}
