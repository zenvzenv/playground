package zhengwei.thread.future;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 对于方法名中含有Async后缀的方法而言
 * 只是方法本身是异步的，比如
 * supplyAsync->thenAccept
 * 对于主调线程来说，supplyAsync方法是异步的，不会阻塞住，会立即去执行thenAccept方法；但对于thenAccept方法来说，它需要去消费supplyAsync的结果，thenAccept会阻塞住，等待supplyAsync返回结果
 * supplyAsync->thenAcceptAsync
 * 对于主调线程来说，supplyAsync和thenAcceptAsync都是异步的，都不会阻塞住，但如果主调线程去获取其中Future的值时就会阻塞住
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/25 20:14
 */
public class CompetableFutureIntermate {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        test();
        test2();
        Thread.currentThread().join();
    }

    private static void test() throws ExecutionException, InterruptedException {
        //内置了ForkJoinPool线程池
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "hello world");//此任务已被提交到线程池中去执行了
        //v->结果
        //whenComplete只对结果进行消费，BiConsumer
        /*future.whenComplete((v, t) -> {
            try {
                System.out.println("111111111111---" + v);
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });*/
        //方法异步执行，想要有异步效果，需要将Future分开分别执行方法
        /*future.whenCompleteAsync((v, t) -> {
            try {
                System.out.println("222222222222---"+v);
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });*/
        //thenApply可以改变返回结果的类型，但是不能处理异常,Function
        /*System.out.println(future.thenApply(String::length));
        System.out.println(future.thenApplyAsync(String::length));*/
        //handle可以改变返回结果的类型，也可以处理异常
        /*final CompletableFuture<Integer> handleFuture1 = future.handle((v, t) -> {
            Optional.of(t).ifPresent(System.out::println);
            if (Objects.isNull(v)) return 0;
            return v.length();
        });*/
        final CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "hello world");
        final CompletableFuture<Integer> handleFuture2 = future2.handleAsync((v, t) -> {
//            System.out.println(v);
            Optional.of(v).ifPresent(System.out::println);
//            Optional.of(t).ifPresent(e-> System.out.println("error"));
            return v.length();
        });
        /*future.thenAccept(System.out::println);
        future.thenAcceptAsync(System.out::println);
        System.out.println(future.get());//得到hello world*/
        System.out.println(handleFuture2.get());
    }

    private static void test2() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello world";
        });
        System.out.println("supply async will not block");
        final CompletableFuture<Integer> handle = future.handleAsync((s, t) -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s.length();
        });
        System.out.println("handle async will not block");
        System.out.println("first result ->" + future.get());
        System.out.println("final result ->" + handle.get());
    }
}
