package zhengwei.thread.executors;

import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 关闭ThreadPoolExecutor
 * 假设现在的ThreadPoolExecutor的情况为:coreSize=10,maxSize=20,workQueueSize=10
 * 1.shutdown
 * 打断空闲线程(有空闲线程说明队列中已经空了，如果队列没空，那么线程会去队列中取任务去执行，打断空闲线程无可厚非)，如果已经提交的任务将会继续执行
 * <p>
 * 2.shutdownNow
 * 打断所有线程(包括正在运行的、休眠的和阻塞的线程)，然后将工作队列中的线程全部取出返回
 * 只会取出work queue中还未执行的任务，若已经被取走的任务，将不会返回(即执行到一半的任务不会返回)
 * 如果想要取出未执行的任务再去执行一遍的话，那么shutdownNow返回的runnable集合将不是靠谱的，需要我们自己包装runnable，在其内部加一个是否完成的标志位
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/30 13:28
 */
public class ThreadPoolExecutorDemo2 {
    public static void main(String[] args) throws InterruptedException {
        final ExecutorService executors = new ThreadPoolExecutor(
                5,
                10,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                r -> {
                    Thread thread = new Thread(r);
                    //设为守护线程
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        //execute是非阻塞方法
        IntStream.rangeClosed(1, 20).forEach(i -> executors.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println(Thread.currentThread().getName() + " [ " + i + " ] finished done");
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }));
        //20个任务都会执行完毕，提交了20个任务，5个核心线程，5个非核心线程，队列长度为10，
        //即shutdown指挥打断空闲的线程，只要队列中还有任务，就会把队列中的任务都执行完毕，但不会再接受新的任务
        /*executors.shutdown();
        executors.awaitTermination(20, TimeUnit.SECONDS);
        System.out.println("======over======");*/
        executors.shutdownNow();

    }
}
