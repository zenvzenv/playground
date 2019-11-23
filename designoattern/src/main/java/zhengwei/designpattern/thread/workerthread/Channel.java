package zhengwei.designpattern.thread.workerthread;

import java.util.Arrays;

/**
 * 传送带
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/21 13:35
 */
public class Channel {
	private final static int MAX_REQUEST = 100;
	private final Request[] requestQueue;
	private int head;
	private int tail;
	private int count;
	private final WorkerThread[] workerPool;

	public Channel(int workers) {
		this.requestQueue = new Request[MAX_REQUEST];
		this.head = 0;
		this.tail = 0;
		this.count = 0;
		this.workerPool = new WorkerThread[workers];
		this.init();
	}

	private void init() {
		for (int i = 0; i < workerPool.length; i++) {
			workerPool[i] = new WorkerThread("Worker-" + i, this);
		}
	}

	/**
	 * 启动
	 * push switch to start all of worker to work
	 */
	public void startWorker() {
		Arrays.asList(workerPool).forEach(WorkerThread::start);
	}

	/**
	 * 放任务进队列中
	 *
	 * @param request 任务
	 */
	public synchronized void put(Request request) {
		//如果队列中的任务数量已达到上线的话，则让线程进入wait set中
		while (count >= workerPool.length) {
			try {
				this.wait();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		this.requestQueue[tail] = request;
		this.tail = (this.tail + 1) % this.requestQueue.length;
		count++;
		this.notifyAll();
	}

	/**
	 * 从任务队列中拿出任务
	 *
	 * @return 任务
	 */
	public synchronized Request take() {
		while (count <= 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		Request request = this.requestQueue[this.head];
		this.head = (this.head + 1) % this.requestQueue.length;
		this.count--;
		this.notifyAll();
		return request;
	}
}
