package zhengwei.thread.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/13 12:59
 */
public class ThreadPoolExecutorAPI1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        testInvokeAny();
        testInvokeAll();
//        testSubmit();
    }

    /**
     * 获取随机一个任务的结果，其余任务会取消执行
     * {@link java.util.concurrent.ThreadPoolExecutor#invokeAny(Collection tasks)}
     */
    private static void testInvokeAny() throws ExecutionException, InterruptedException {
        final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                final int random = ThreadLocalRandom.current().nextInt(10);
                TimeUnit.SECONDS.sleep(random);
                System.out.println(random);
                return random;
            });
        }
        final Integer integer = service.invokeAny(tasks);
        System.out.println("invoke any -> " + integer);
    }

    /**
     * {@link ThreadPoolExecutor#invokeAll(Collection tasks)}
     * invokeAll返回回来的Future集合中，是乱序的，我们不知道集合中的任务执行完毕的先后顺序，
     * 这样在我们去获取结果的时候会很被动
      * @throws InterruptedException 主调线程去Future中get结果的中断异常
     */
    private static void testInvokeAll() throws InterruptedException {
        ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                final int random = ThreadLocalRandom.current().nextInt(200);
                System.out.println(random);
                return random;
            });
        }
        final List<Future<Integer>> results = service.invokeAll(tasks);
        results.forEach(r -> {
            try {
                System.out.println(r.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * {@link ExecutorService#submit(Runnable runnable, Object result)}
     * submit接受一个runnable，如果没有指定第二个result参数的话，那么待runnable执行完毕之后，future得到的结果为null
     * 如果执行了result的话，那么future中的得到结果将为result的值
     * 如果需要有返回值的场景的话，建议还是使用Callable
     */
    private static void testSubmit() throws ExecutionException, InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<?> result = executorService.submit(() -> System.out.println("qqqqqq"), "finish");
        //get方式为block，程序会阻塞住
        System.out.println(result.get());
        executorService.shutdown();
    }

    /**
     * {@link ThreadPoolExecutor#getQueue()}
     * 如果线程池中没有活动的线程的话，我们往work queue队列中加入任务的时候，线程池将不会有任何反应
     * 若线程池中有活动的线程，那么往work queue中添加任务时，任务将会被执行
     */
    private static void testAddQueue() {
        final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        executorService.execute(() -> System.out.println("I am called by execute"));
        executorService.getQueue().add(() -> System.out.println("I am added by queue"));
    }
}
