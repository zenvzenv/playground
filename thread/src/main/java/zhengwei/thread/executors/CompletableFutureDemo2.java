package zhengwei.thread.executors;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture内部维护着ForkJoinPool线程池，我们也可以向其中传入自定义的线程池(ExecutorService)
 * CompletableFuture内部API支持级联操作
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/25 13:33
 */
public class CompletableFutureDemo2 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        testSupply();
        /*Future<?> future = runAsync();
        future.get();*/
        /*Future<Void> future = completed("hello completable future");
        System.out.println(future.isDone());*/
//        anyOf();
        allOf();
        Thread.currentThread().join();
    }

    private static void testSupply() {
        CompletableFuture.supplyAsync(Object::new)
                .thenAcceptAsync(obj -> {
                    try {
                        System.out.println("obj->start");
                        TimeUnit.SECONDS.sleep(5);
                        System.out.println("obj->end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .runAfterBoth(
                        CompletableFuture
                                .supplyAsync(() -> "hello")
                                .thenAcceptAsync(s -> {
                                    try {
                                        System.out.println("string->start");
                                        TimeUnit.SECONDS.sleep(3);
                                        System.out.println("string->end");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }),
                        () -> System.out.println("finished")
                );
    }

    private static Future<?> runAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("runAsync---start");
                TimeUnit.SECONDS.sleep(10);
                System.out.println("runAsync---end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).whenComplete((v, t) -> System.out.println("---over---"));
    }

    private static Future<Void> completed(String data) {
        return CompletableFuture.completedFuture(data)
                .thenAccept(System.out::println);
    }

    /**
     * {@link CompletableFuture#anyOf(CompletableFuture[] tasks)}与{@link java.util.concurrent.ThreadPoolExecutor#invokeAny(Collection tasks)}有点类似
     * 两者都是获取一组任务中的某一个值，但是在anyOf中其余的任务还是会执行完毕，但是invokeAny则是获取其中一个结果之后将其余任务取消
     * Runnable返回的结果时null，Future返回的结果即为结果
     */
    private static void anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = CompletableFuture.anyOf(
                CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("1---start");
                        TimeUnit.SECONDS.sleep(5);
                        System.out.println("1---end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }),
                CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("2---start");
                        TimeUnit.SECONDS.sleep(10);
                        System.out.println("2---end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "2222";
                })
        );
        System.out.println("get the result >>>>>> "+future.get());
    }

    /**
     * {@link CompletableFuture#allOf(CompletableFuture[] tasks)}与{@link java.util.concurrent.ThreadPoolExecutor#invokeAll(Collection tasks)}类似
     * 并行的执行里面的全部任务
     */
    private static void allOf(){
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("1---start");
                        TimeUnit.SECONDS.sleep(5);
                        System.out.println("1---end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }),
                CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("2---start");
                        TimeUnit.SECONDS.sleep(10);
                        System.out.println("2---end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "2222";
                })
        );
    }
}
