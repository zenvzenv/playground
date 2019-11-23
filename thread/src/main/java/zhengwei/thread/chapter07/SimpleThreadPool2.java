package zhengwei.thread.chapter07;

import zhengwei.util.common.SysUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 线程池优化版
 * 相比较第一版增加了拒绝策略和关闭线程池的方法
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/18 16:12
 */
public class SimpleThreadPool2 {
	//线程池大小
	private int threadSize;
	//任务队列大小
	private int taskSize;
	//默认线程池大小
	private static final int DEFAULT_THREAD_SIZE = 10;
	//默认任务队列的大小
	private static final int DEFAULT_TASK_SIZE = 2000;
	//线程编号
	private static volatile int seq = 0;
	//线程池中线程名的前缀
	private static final String THREAD_NAME_PREFIX = "SIMPLE_THREAD_POOL_";
	//线程组
	private static final ThreadGroup GROUP = new ThreadGroup("PoolGroup");
	//活跃线程集合
	private static final List<WorkerTask> THREAD_QUEUE = new ArrayList<>();
	//任务队列
	private static final LinkedList<Runnable> TASK_QUEUE = new LinkedList<>();
	//拒绝策略
	private RejectionStrategy rejectionStrategy;
	/*
	线程池状态，是否被销毁了
	true->被销毁了
	false->没有被销毁
	 */
	private volatile static boolean isDestroy = false;
	//默认拒绝策略
	public static final RejectionStrategy DEFAULT_REJECT_STRATEGY = () -> {
		throw new RejectionStrategyException("reject you");
	};

	public SimpleThreadPool2(int threadSize, int taskSize, RejectionStrategy rejectionStrategy) {
		this.threadSize = threadSize;
		this.taskSize = taskSize;
		this.rejectionStrategy = rejectionStrategy;
		init();
	}

	public SimpleThreadPool2(int threadSize, int taskSize) {
		this.threadSize = threadSize;
		this.taskSize = taskSize;
		new SimpleThreadPool2(this.threadSize, this.taskSize, DEFAULT_REJECT_STRATEGY);
	}

	/**
	 * 构造函数
	 * 如果没有传入相关的值，则赋予默认值
	 */
	public SimpleThreadPool2() {
		this(DEFAULT_THREAD_SIZE, DEFAULT_TASK_SIZE, DEFAULT_REJECT_STRATEGY);
	}

	private void init() {
		for (int i = 0; i < this.threadSize; i++) {
			createWorkTask();
		}
	}

	/**
	 * 创建工作线程，指定ThreadGroup和Thread的名字
	 */
	private void createWorkTask() {
		WorkerTask task = new WorkerTask(GROUP, THREAD_NAME_PREFIX + (seq++));
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
			/*
			 * 拒绝策略，如果任务队列过多，则启用拒绝策略
			 */
			if (TASK_QUEUE.size() > this.taskSize) {
				try {
					this.rejectionStrategy.rejection();
				} catch (RejectionStrategyException e) {
					e.printStackTrace();
				}
			}
			//添加任务到任务队列，并唤醒等待任务的线程
			TASK_QUEUE.addLast(runnable);
			TASK_QUEUE.notifyAll();
		}
	}

	/**
	 * 关闭线程池
	 *
	 * @throws InterruptedException 中断异常
	 */
	public void shutdown() throws InterruptedException {
		//让线程池处于销毁状态，不再对外提供服务，不能再提交任务
		if (isDestroy) {
			throw new RuntimeException("Thread pool is destroyed and not allow to submit any task any more");
		}
		//如果任务队列中还有任务待执行，就等待等待任务都执行完，直到任务队列为空
		while (!TASK_QUEUE.isEmpty()) {
			Thread.sleep(10L);
		}
		synchronized (THREAD_QUEUE) {
			int threadCount = THREAD_QUEUE.size();
			while (threadCount > 0) {
				for (Iterator<WorkerTask> iter = THREAD_QUEUE.iterator(); iter.hasNext(); ) {
					WorkerTask taskThread = iter.next();
					if (taskThread.workerState.equals(WorkerState.BLOCKED)) {
						taskThread.close();
						taskThread.interrupt();
						threadCount--;
						iter.remove();
					} else {
						Thread.sleep(100L);
					}
				}
			}
			isDestroy = true;
			System.out.println("SimpleThreadPool2 is shutdown");
			System.out.println(THREAD_QUEUE.size());
		}
	}

	/**
	 * @return
	 */
	public int getThreadSize() {
		return threadSize;
	}

	public int getTaskSize() {
		return taskSize;
	}

	/**
	 * 线程状态枚举类
	 */
	private enum WorkerState {
		FREE, RUNNING, BLOCKED, DEAD
	}

	/**
	 * 拒绝策略异常
	 */
	private static class RejectionStrategyException extends Exception {
		public RejectionStrategyException(String message) {
			super(message);
		}
	}

	/**
	 * 拒绝策略接口
	 * 实现拒绝策略
	 */
	@FunctionalInterface
	public interface RejectionStrategy {
		/**
		 * 拒绝策略具体实现
		 *
		 * @throws RejectionStrategyException 异常
		 */
		void rejection() throws RejectionStrategyException;
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
				//需要注意锁的范围，要不然会造成一个线程长时间占有锁，导致效率降低
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
					//取出任务队列中的第一个任务
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
		SimpleThreadPool2 simpleThreadPool = new SimpleThreadPool2(6, 20, SimpleThreadPool2.DEFAULT_REJECT_STRATEGY);
		IntStream.rangeClosed(0, 40)
				.forEach(i -> simpleThreadPool.submit(() -> {
					try {
						System.out.println("The runnable " + i + " be service by " + Thread.currentThread().getName() + " start");
						Thread.sleep(5_000L);
						System.out.println("The runnable " + i + " be service by " + Thread.currentThread().getName() + " end");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}));
		try {
			simpleThreadPool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*for (int i=0;i<40;i++){
			simpleThreadPool.submit(() -> {
				try {
					System.out.println("The runnable "  + " be service by " + Thread.currentThread().getName() + " start");
					Thread.sleep(10_000L);
					System.out.println("The runnable "  + " be service by " + Thread.currentThread().getName() + " end");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}*/
	}
}
