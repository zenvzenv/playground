package zhengwei.thread.executors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/25 13:33
 */
public class CompletableFutureDemo2 {
    public static void main(String[] args) throws InterruptedException {
        testSupply();
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
}
