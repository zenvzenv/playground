package zhengwei.designpattern.thread.threadpremessage;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 消息处理器
 * 接收一个请求就开启一个线程去处理它
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/10 15:01
 */
public class MessageHandler {
	private static final Random random = new Random(System.currentTimeMillis());
	private static final Executor executor = Executors.newFixedThreadPool(5);

	void request(Message message) {
		executor.execute(()->{
			try {
				String msg = message.getMessage();
				System.out.println("the message will handle by => " + Thread.currentThread().getName());
				Thread.sleep(random.nextInt(1000));
				System.out.println("the message is handle over by -> " + Thread.currentThread().getName());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		/*new Thread(() -> {
			try {
				String msg = message.getMessage();
				System.out.println("the message will handle by => " + Thread.currentThread().getName());
				Thread.sleep(random.nextInt(1000));
				System.out.println("the message is handle over by -> " + Thread.currentThread().getName());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();*/
	}
}
