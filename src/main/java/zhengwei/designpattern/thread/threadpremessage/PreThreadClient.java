package zhengwei.designpattern.thread.threadpremessage;

import java.util.stream.IntStream;

/**
 * 使用线程池来配合使用
 * @author zhengwei AKA Awei
 * @since 2019/8/10 15:08
 */
public class PreThreadClient {
	public static void main(String[] args) {
		final MessageHandler messageHandler = new MessageHandler();
		IntStream.rangeClosed(1, 10).forEach(i -> messageHandler.request(new Message("message-" + i)));
	}
}
