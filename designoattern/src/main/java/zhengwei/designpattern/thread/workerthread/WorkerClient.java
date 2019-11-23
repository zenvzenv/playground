package zhengwei.designpattern.thread.workerthread;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/21 19:19
 */
public class WorkerClient {
	public static void main(String[] args) {
		final Channel channel = new Channel(5);
		channel.startWorker();
		new TransportThread("zhengwei1", channel).start();
		new TransportThread("zhengwei2", channel).start();
		new TransportThread("zhengwei3", channel).start();

	}
}
