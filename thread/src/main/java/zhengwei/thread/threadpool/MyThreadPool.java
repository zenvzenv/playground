package zhengwei.thread.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/28 18:49
 */
public class MyThreadPool implements Executor {
    private String name;
    private int coreSize;
    private int maxSize;
    private BlockingQueue<Runnable> taskQueue;
    private RejectPolicy rejectPolicy;
    private AtomicInteger seq = new AtomicInteger(0);
    private AtomicInteger runningCount = new AtomicInteger(0);

    public MyThreadPool(String name, int coreSize, int maxSize, BlockingQueue<Runnable> taskQueue, RejectPolicy rejectPolicy) {
        this.name = name;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.taskQueue = taskQueue;
        this.rejectPolicy = rejectPolicy;
    }

    @Override
    public void execute(Runnable task) {
        //首先获取正在工作的线程
        int count = runningCount.get();
        //工作线程小于核心线程
        if (count < this.coreSize) {
            //注意，这里不一定要添加成功,addWorker里面会在进行一次判断是否真的小区核心线程数
            if (addWorker(task, true)) {
                return;
            }
        }
        //如果添加核心线程失败，进入下面的逻辑
        //如果达到核心线程数量，先尝试将任务入队列
        //这里之所以使用offer()，是因为队列满了之后调用offer之后会立即返回false
        if (this.taskQueue.offer(task)) {
            //如果任务入队成功则什么都不做
        } else {
            //如果入队失败，说明队列满了，那就添加一个非核心线程
            if (!addWorker(task, false)) {
                //如果添加非核心线程失败，则启用拒绝策略
                this.rejectPolicy.reject(task);
            }
        }
    }

    private boolean addWorker(Runnable newTask, boolean core) {
        //自旋判断是否真的可以创建一个线程
        for (; ; ) {
            int count = runningCount.get();
            //是否是核心线程
            int maxSize = core ? coreSize : this.maxSize;
            //当前正在运行的线程已达到上限
            if (count >= maxSize) {
                return false;
            }
            if (runningCount.compareAndSet(count, count + 1)) {
                String threadName = core ? "core_" : "non_core_" + this.name + this.seq.incrementAndGet();
                new Thread(() -> {
                    System.out.println("thread name: " + Thread.currentThread().getName());
                    Runnable task = newTask;
                    while (task != null || (task = getTask()) != null) {
                        try {
                            task.run();
                        } finally {
                            task = null;
                        }
                    }
                }, threadName).start();
                break;
            }
        }
        return true;
    }

    private Runnable getTask() {
        try {
            return this.taskQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
