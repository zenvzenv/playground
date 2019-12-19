package zhengwei.thread.executors;

import java.util.concurrent.*;

/**
 * CompletionService内部有一个阻塞队列
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/19 19:42
 */
public class CompletionDemo1 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
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
}
