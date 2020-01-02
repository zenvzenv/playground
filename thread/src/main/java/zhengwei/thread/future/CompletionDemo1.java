package zhengwei.thread.future;

import java.util.Collection;
import java.util.concurrent.*;

/**
 * CompletionService内部有一个阻塞队列，这个阻塞队列将维护着所有已完成任务的结果
 * 与{@link ThreadPoolExecutor#invokeAll(Collection tasks)}不同的是，CompletionService中的队列维护着Future的结果
 * 越早执行完的任务，将会被放到队列的前边，后执行完的任务的结果将会顺延放到后面，
 * 这样我们在获取结果的时候，总时能够获取到最先执行完的结果，而不是随机的等待一个不知道需要执行多久的任务
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/19 19:42
 */
public class CompletionDemo1 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        testTake();
        testSubmit();
    }

    /**
     * {@link CompletionService#take()}从结果队列中获取值，会阻塞到得到一个结果为止
     * {@link CompletionService#poll()}从结果队列中获取结果，如果没有返回null
     * {@link CompletionService#poll(long time, TimeUnit unit)}从结果队列中获取结果，等待指定时间，如果到了指定时间后如果没有结果则返回null
     */
    private static void testTake() throws InterruptedException, ExecutionException {
        final ExecutorService service = Executors.newFixedThreadPool(5);
        final ExecutorCompletionService<Integer> executorCompletionService = new ExecutorCompletionService<>(service);
        executorCompletionService.submit(() -> {
            TimeUnit.SECONDS.sleep(5);
            return 5;
        });
        executorCompletionService.submit(() -> {
            TimeUnit.SECONDS.sleep(10);
            return 10;
        });
        TimeUnit.SECONDS.sleep(1);
        Future<Integer> future;
        while ((executorCompletionService.take()) != null) {
            //take会阻塞住
            System.out.println(executorCompletionService.take().get());
        }
    }

    /**
     * {@link CompletionService#submit(Runnable task, Object result)}submit可以指定一个待任务结束之后的默认的返回值
     */
    private static void testSubmit() throws InterruptedException, ExecutionException {
        final ExecutorService service = Executors.newFixedThreadPool(5);
        final ExecutorCompletionService<String> executorCompletionService = new ExecutorCompletionService<>(service);
        //submit可以提供一个默认返回值
        executorCompletionService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "done");
        System.out.println(executorCompletionService.take().get());
    }
}
