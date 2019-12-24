package zhengwei.thread.executors;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Future的一些缺点
 * 1. 无法知道任务执行结束的先后顺序，在获取结果的时候比较被动----CompletionService
 * 2. Future在get结果的时候会block住-----CompletableFuture#whenComplete
 * 3. 无法级联操作----CompletableFuture
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/24 20:13
 */
public class CompletableFutureDemo1 {
    public static void main(String[] args) throws InterruptedException {
//        test1();
        test2();
        Thread.currentThread().join();
    }

    /**
     * {@link CompletableFuture#runAsync(Runnable task)}异步运行
     * 默认线程都是守护线程，caller thread结束以后执行任务的线程也就结束了
     */
    private static void test1() throws InterruptedException {
        final ExecutorService service = Executors.newFixedThreadPool(10);
        CompletableFuture
                .runAsync(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, service)
                .whenComplete((v, t) -> System.out.println("done"));//当线程将任务执行结束之后将回调此方法
        System.out.println("i am not blocked");
        Thread.currentThread().join();
    }

    /**
     * {@link CompletableFuture#supplyAsync(Supplier)}
     * {@link CompletableFuture#thenAccept(Consumer)}
     * {@link CompletableFuture#whenComplete(BiConsumer)}
     * 真正的异步执行代码
     */
    private static void test2() {
        IntStream.rangeClosed(1, 10)
                .forEach(
                        callable -> CompletableFuture.supplyAsync(CompletableFutureDemo1::get)
                                .thenAccept(CompletableFutureDemo1::display)
                                .whenComplete((v, t) -> System.out.println("done"))
                );
    }

    private static int get() {
        int value = ThreadLocalRandom.current().nextInt(100);
        try {
            System.out.println(Thread.currentThread().getName() + " will sleep " + value);
            TimeUnit.SECONDS.sleep(value);
            System.out.println(Thread.currentThread().getName() + " execute done " + value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }

    private static void display(int data) {
        System.out.println(Thread.currentThread().getName() + " execute done " + data);
    }
}
