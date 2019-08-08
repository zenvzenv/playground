package zhengwei.designpattern.consumerproductor;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/8 19:37
 */
public class ProducerAndConsumerClient {
	public static void main(String[] args) {
		final MessageQueue messageQueue=new MessageQueue();
		new ProducerThread(messageQueue,1).start();
		new ConsumerThread(messageQueue,1).start();
	}
}
