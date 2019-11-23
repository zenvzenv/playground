package zhengwei.thread.chapter07;

import zhengwei.util.common.SysUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 简易版线程池优化版
 * 增加动态增长和减少线程数量
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/18 18:36
 */
public class SimpleThreadPool3 extends Thread {
	//线程池大小
	private int threadSize;
	//任务队列大小
	private int taskSize;
	//默认任务队列的大小
	private static final int DEFAULT_TASK_SIZE = 2000;
	//线程编号
	private static volatile int seq = 0;
	//线程池中线程名的前缀
	private static final String THREAD_NAME_PREFIX = "SIMPLE_THREAD_POOL_";
	//线程组
	private static final ThreadGroup GROUP = new ThreadGroup("PoolGroup");
	//活跃线程集合
	private static final List<SimpleThreadPool3.WorkerTask> THREAD_QUEUE = new ArrayList<>();
	//任务队列
	private static final LinkedList<Runnable> TASK_QUEUE = new LinkedList<>();
	//拒绝策略
	private RejectionStrategy rejectionStrategy;

	private int min;
	private int max;
	private int active;
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

	public SimpleThreadPool3(int min, int active, int max, int taskSize, RejectionStrategy rejectionStrategy) {
		this.min = min;
		this.active = active;
		this.max = max;
		this.taskSize = taskSize;
		this.rejectionStrategy = rejectionStrategy;
		init();
	}

	/**
	 * 构造函数
	 * 如果没有传入相关的值，则赋予默认值
	 */
	public SimpleThreadPool3() {
		this(4, 8, 12, DEFAULT_TASK_SIZE, DEFAULT_REJECT_STRATEGY);
	}

	private void init() {
		for (int i = 0; i < this.min; i++) {
			createWorkTask();
		}
		this.threadSize = min;
//		this.start();
	}

	@Override
	public void run() {
		/*
		 * 1.当最小线程数min不足以应付任务时，扩充线程至active个
		 * 2.当活动线程数active不足以应付任务时，扩充线程至max个
		 * 3.当任务队列为空，将那些空闲的线程杀死，减小至min个
		 * 任务队列为空或执行任务的线程少于最小线程数量，总是维护最小线程数量的线程
		 */
		while (!isDestroy) {
			try {
				System.out.printf("ThreadPool@Min:%d,Active:%d,Max:%d,Current:%d,Queue:%d\n",
						this.min, this.active, this.max, this.threadSize, TASK_QUEUE.size());
				Thread.sleep(5_000L);
				if (TASK_QUEUE.size() > active && this.threadSize < active) {
					for (int i = this.threadSize; i < active; i++) {
						createWorkTask();
					}
					System.out.println("The ThreadPool incremented to active");
					//更新当前线程池中线程的大小
					this.threadSize = active;
				} else if (TASK_QUEUE.size() > this.threadSize && this.threadSize < this.max) {
					for (int i = this.threadSize; i < this.max; i++) {
						createWorkTask();
					}
					System.out.println("The ThreadPool incremented to max");
					//更新当前线程池中线程的大小
					this.threadSize = this.max;
				}
				synchronized (TASK_QUEUE) {
					if (TASK_QUEUE.isEmpty() && this.threadSize > this.active) {
						int releaseSize = this.threadSize - this.active;
						for (Iterator<WorkerTask> iter = THREAD_QUEUE.iterator(); iter.hasNext(); ) {
							if (releaseSize <= 0) break;
							WorkerTask workerTask = iter.next();
							//这里要进行一次判断，只对BLOCKED状态的线程进行关闭，要不然会有报错InterruptException:sleep interrupt
							if (workerTask.workerState.equals(WorkerState.BLOCKED)) {
								workerTask.close();
								workerTask.interrupt();
								iter.remove();
								releaseSize--;
							}
						}
						this.threadSize = this.active;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建工作线程，指定ThreadGroup和Thread的名字
	 */
	private void createWorkTask() {
		SimpleThreadPool3.WorkerTask task = new SimpleThreadPool3.WorkerTask(GROUP, THREAD_NAME_PREFIX + (seq++));
//		System.out.println(seq);
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
				for (Iterator<SimpleThreadPool3.WorkerTask> iter = THREAD_QUEUE.iterator(); iter.hasNext(); ) {
					SimpleThreadPool3.WorkerTask taskThread = iter.next();
					if (taskThread.workerState.equals(SimpleThreadPool3.WorkerState.BLOCKED)) {
						//把工作的线程置为DEAD
						taskThread.close();
						//打断线程，让线程捕获到中断信号，跳出while循环
						taskThread.interrupt();
						threadCount--;
						iter.remove();
					} else {
						Thread.sleep(100L);
					}
				}
			}
			GROUP.activeCount();
			//线程池销毁标识置为true，标识线程池被销毁了不再接受新的任务和线程
			isDestroy = true;
			System.out.println("SimpleThreadPool3 is shutdown");
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
		void rejection() throws SimpleThreadPool3.RejectionStrategyException;
	}

	//线程
	private static class WorkerTask extends Thread {
		private volatile SimpleThreadPool3.WorkerState workerState = SimpleThreadPool3.WorkerState.FREE;

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
		public SimpleThreadPool3.WorkerState getWorkerState() {
			return this.workerState;
		}

		/**
		 * 关闭线程，并不是实际意义上的关闭
		 */
		public void close() {
			this.workerState = SimpleThreadPool3.WorkerState.DEAD;
		}

		@Override
		public void run() {
			OUTER:
			while (this.workerState != SimpleThreadPool3.WorkerState.DEAD) {
				Runnable runnable;
				//需要注意锁的范围，要不然会造成一个线程长时间占有锁，导致效率降低
				synchronized (TASK_QUEUE) {
					while (TASK_QUEUE.isEmpty()) {
						try {
							this.workerState = SimpleThreadPool3.WorkerState.BLOCKED;
							TASK_QUEUE.wait();
						} catch (InterruptedException e) {
//							e.printStackTrace();
							System.out.println("close not active thread");
							break OUTER;
						}
					}
					//取出任务队列中的第一个任务
					runnable = TASK_QUEUE.removeFirst();
				}
				if (SysUtils.isNotNull(runnable)) {
					this.workerState = SimpleThreadPool3.WorkerState.RUNNING;
					runnable.run();
					this.workerState = SimpleThreadPool3.WorkerState.FREE;
				}
			}
		}
	}

	public static void main(String[] args) {
		SimpleThreadPool3 simpleThreadPool = new SimpleThreadPool3();
		simpleThreadPool.start();
		IntStream.rangeClosed(0, 40)
				.forEach(i -> simpleThreadPool.submit(() -> {
					try {
						System.out.println("The runnable " + i + " be service by " + Thread.currentThread().getName() + " start");
						/*
						因为代码1处的休眠时间有点短，shutdown的时候去打断线程的时候，捕获到了中断信号
						 */
						Thread.sleep(10_000L);
						System.out.println("The runnable " + i + " be service by " + Thread.currentThread().getName() + " end");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}));

		try {
			//1
			Thread.sleep(10_000L);
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
