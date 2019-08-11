package zhengwei.designpattern.thread.guardedsuspension;

import java.util.LinkedList;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/30 12:53
 */
public class RequestQueue {
	private static final LinkedList<Request> queue = new LinkedList<>();

	/**
	 * 我们有时间去处理了，去队列中取出任务来处理
	 *
	 * @return 需要处理的请i去
	 */
	public Request getRequest() {
		synchronized (queue) {
			while (queue.size() <= 0) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
//					e.printStackTrace();
					return null;
				}
			}
			return queue.removeFirst();
		}
	}

	/**
	 * 把请求放到队列中去，等到我们有时间了再去处理它
	 *
	 * @param request 需要处理的请求
	 */
	public void putRequest(Request request) {
		synchronized (queue) {
			queue.addLast(request);
			queue.notifyAll();
		}
	}
}
