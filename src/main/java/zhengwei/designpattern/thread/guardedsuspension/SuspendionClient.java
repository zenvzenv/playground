package zhengwei.designpattern.thread.guardedsuspension;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/30 13:44
 */
public class SuspendionClient {
	public static void main(String[] args) {
		final RequestQueue queue = new RequestQueue();
		new ClientThread(queue, "Awei");
		new ServerThread(queue).start();
	}
}
