package zhengwei.thread.future;

import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * CompletableFuture的一些终结API
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/1 10:02
 */
public class CompletableFutureTerminal {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        getNow();
//        completableExceptionally();
//        obtrudeException();
        System.out.println(errorHandler().get());
        Thread.currentThread().join();
    }

    /**
     * {@link CompletableFuture#exceptionally(Function)}
     * 接管在处理过程中出现的异常
     */
    private static CompletableFuture<String> errorHandler() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            sleep(5);
            System.out.println("end");
            return "hello";
        });
        //接下来可以继续执行
        future
                .thenApplyAsync(s -> {
                    System.out.println(Integer.parseInt(s));
                    System.out.println("---keep going---");
                    return s + " world";
                })
                .exceptionally(Throwable::getMessage)
                .thenAcceptAsync(System.out::println);
        return future;
    }

    /**
     * {@link CompletableFuture#obtrudeException(Throwable e)}不管future是否执行结束，在get的时候都直接抛出异常
     */
    private static void obtrudeException() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(5);
            System.out.println("end");
            return "hello";
        });
        TimeUnit.SECONDS.sleep(6);
        future.obtrudeException(new NullPointerException("fuck off"));
        System.out.println(future.get());
    }

    /**
     * {@link CompletableFuture#completeExceptionally(Throwable e)}指定一个在未完成情况下获取值的异常。
     * 当future还未执行完毕的时候，如果这时去{@link CompletableFuture#get()}的时候，将会直接抛出指定的异常，不会让get阻塞住
     */
    private static void completableExceptionally() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(5);
            System.out.println("end");
            return "hello";
        });
        future.completeExceptionally(new NullPointerException());
        System.out.println(future.get());
    }

    /**
     * {@link CompletableFuture#join()}
     * 和{@link CompletableFuture#get()}的作用一样，获取CompletableFuture的结果，但是join不会抛出异常，而get会抛出异常，
     * 通常使用在lambda中
     */
    private static void join() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(5);
            System.out.println("end");
            return "hello";
        });
        String result = future.join();
        System.out.println(result);
    }

    /**
     * {@link CompletableFuture#complete(Object result)}
     * 1.如果complete方法先于future执行的话，那么future将会被cancel掉，在获取future的值时会返回complete设定的结果
     * 2.如果future先于complete之前已经获取到了结果，那么在get future结果的时候就不会获取complete设定值
     * 3.如果future已经在执行中，但是还没有结果，在去获取future的值的时候还是会获取到complete设定的值，future还是会继续执行
     * 这适用于缓存机制，不让线程在get值的时候阻塞住。
     */
    private static void complete() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(5);
            System.out.println("end");
            return "hello";
        });
        sleep(2);
        boolean finished = future.complete("world");
        System.out.println(finished);
        System.out.println(future.get());
    }

    /**
     * {@link CompletableFuture#getNow(Object sign)}
     * 切断CompletableFuture的级联操作并返回一个值
     */
    private static void getNow() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(5);
            return "hello";
        });
        String result = future.getNow("world");
        System.out.println(result);
        System.out.println(future.get());
    }

    @SneakyThrows
    private static void sleep(long time) {
        TimeUnit.SECONDS.sleep(10);
    }
}
