package zhengwei.thread.chapter07;

import zhengwei.util.common.SysUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 简易版线程池
 * 1.任务队列：待执行的任务队列
 * 2.拒绝策略：抛出异常，直接丢弃，阻塞，临时队列
 * 3.初始化值init(MIN)
 * 4.活跃进程(Active)
 * 4.最大线程值(MAX)
 * MIN<=Active<=MAX
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/17 18:16
 */
public class SimpleThreadPool {
	private int size;
	private static final int DEFAULT_SIZE = 10;
	//线程编号
	private static volatile int seq = 0;
	private static final String THREAD_PREFIX = "SIMPLE_THREAD_POOL_";
	//线程组
	private static final ThreadGroup GROUP = new ThreadGroup("PoolGroup");
	private static final List<WorkerTask> THREAD_QUEUE = new ArrayList<>();
	//任务队列
	private static final LinkedList<Runnable> TASK_QUEUE = new LinkedList<>();

	public SimpleThreadPool(int size) {
		this.size = size;
		init();
	}

	public SimpleThreadPool() {
		this(DEFAULT_SIZE);
	}

	private void init() {
		for (int i = 0; i < this.size; i++) {
			createWorkTask();
		}
	}

	private void createWorkTask() {
		WorkerTask task = new WorkerTask(GROUP, THREAD_PREFIX + (seq++));
		System.out.println(seq);
		task.start();
		THREAD_QUEUE.add(task);
	}

	/**
	 * 对外提供提交任务的方法
	 *
	 * @param runnable 提交的任务
	 */
	public void submit(Runnable runnable) {
		synchronized (TASK_QUEUE) {
			//添加任务到任务队列，并唤醒等待任务的线程
			TASK_QUEUE.addLast(runnable);
			TASK_QUEUE.notifyAll();
		}
	}

	/**
	 * 线程状态枚举类
	 */
	private enum WorkerState {
		FREE, RUNNING, BLOCKED, DEAD
	}

	//线程
	private static class WorkerTask extends Thread {
		private volatile WorkerState workerState = WorkerState.FREE;

		/**
		 * 构造函数，引入ThreadGroup
		 *
		 * @param threadGroup 指定父ThreadGroup
		 * @param name        线程的名字
		 */
		public WorkerTask(ThreadGroup threadGroup, String name) {
			super(threadGroup, name);
		}

		/**
		 * 获取线程状态
		 *
		 * @return 线程状态
		 */
		public WorkerState getWorkerState() {
			return this.workerState;
		}

		/**
		 * 关闭线程，并不是实际意义上的关闭
		 */
		public void close() {
			this.workerState = WorkerState.DEAD;
		}

		@Override
		public void run() {
			OUTER:
			while (this.workerState != WorkerState.DEAD) {
				Runnable runnable;
				synchronized (TASK_QUEUE) {
					while (TASK_QUEUE.isEmpty()) {
						try {
							this.workerState = WorkerState.BLOCKED;
							TASK_QUEUE.wait();
						} catch (InterruptedException e) {
//							e.printStackTrace();
							break OUTER;
						}
					}
					runnable = TASK_QUEUE.removeFirst();
				}
				if (SysUtils.isNotNull(runnable)) {
					this.workerState = WorkerState.RUNNING;
					runnable.run();
					this.workerState = WorkerState.FREE;
				}
			}
		}
	}

	public static void main(String[] args) {
		SimpleThreadPool simpleThreadPool = new SimpleThreadPool();
		IntStream.rangeClosed(0, 40)
				.forEach(i -> simpleThreadPool.submit(() -> {
					try {
						System.out.println("The runnable " + i + " be service by " + Thread.currentThread().getName() + " start");
						Thread.sleep(10_000L);
						System.out.println("The runnable " + i + " be service by " + Thread.currentThread().getName() + " end");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}));
	}
}
