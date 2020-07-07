package zhengwei.thread.executors;

import java.util.concurrent.*;

/**
 * Future的一些缺点
 * 1. 虽然可以做到任务的同步执行，异步获取，但是在get的时候如果任务没有执行完的话，程序还是会卡住，缺少callback
 * 2. invokeAll的时候不一定能够先拿出最先执行完的线程的结果，导致程序阻塞住
 * Completion
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/19 12:56
 */
public class CompletionServiceDemo1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        testFutureGet();
    }

    private static void testFutureGet() throws InterruptedException, ExecutionException {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<Integer> future = executorService.submit(() -> {
            try {
                System.out.println("start");
                TimeUnit.SECONDS.sleep(6);
                System.out.println("over");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });
        TimeUnit.SECONDS.sleep(5);
        System.out.println(future.get());
    }
}
