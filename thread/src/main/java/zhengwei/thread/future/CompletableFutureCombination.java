package zhengwei.thread.future;

import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * CompletableFuture的组合操作，组合操作完了之后将会返回Void
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/31 12:40
 */
public class CompletableFutureCombination {
    @SneakyThrows
    public static void main(String[] args) {
//        testAcceptBoth();
        testRunAfterBoth();
        Thread.currentThread().join();
    }

    /**
     * {@link CompletableFuture#thenAcceptBoth(CompletionStage other, BiConsumer action)}
     * 接受另外一个CompletableFuture
     * <p>
     * 并接受一个action，接受前两个CompletableFuture的结果进行处理
     */
    private static void testAcceptBoth() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("start to supply");
            sleep(5);
            System.out.println("end to supply");
            return "testAcceptBoth";
        }).thenAcceptBoth(
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("start to thenAcceptBoth");
                    sleep(2);
                    System.out.println("end to thenAcceptBoth");
                    return 66;
                }),
                (s, i) -> System.out.printf("first completable future result -> %s,second completable future result -> %d\n", s, i)
        );
    }

    /**
     * {@link CompletableFuture#acceptEither(CompletionStage other, Consumer action)}
     * 接受另一个CompletableFuture，这两个CompletableFuture的返回值类型需要相同。
     * 随后接受这两个CompletableFuture的其中一个的结果，进行处理
     */
    private static void testAcceptEither() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("start to supply");
            sleep(5);
            System.out.println("end to supply");
            return "supply";
        }).acceptEither(
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("start to accept either");
                    sleep(3);
                    System.out.println("end to accept either");
                    return "either";
                }),
                result -> System.out.println("either completable future result -> " + result));
    }

    /**
     * {@link CompletableFuture#runAfterBoth(CompletionStage other, Runnable cmd)}
     * 在两个CompletableFuture执行结束之后执行cmd
     */
    private static void testRunAfterBoth() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("start to supply");
            sleep(10);
            System.out.println("end to supply");
            return "supply";
        }).runAfterBoth(CompletableFuture.supplyAsync(() -> {
            System.out.println("start to run after both");
            sleep(3);
            System.out.println("end to run after both");
            return 88;
        }), () -> System.out.println("both completable future is finished"));
    }

    /**
     * {@link CompletableFuture#thenCombine(CompletionStage other, BiFunction combine)}
     * 将两个CompletableFuture的结果进行组合操作，combine的BiFunction的两个入参就是前面两个CompletableFuture的结果，返回值是对这两结果进行操作之后的返回值。
     * {@link CompletableFuture#whenComplete(BiConsumer action)}与{@link CompletableFuture#thenCombine(CompletionStage other, BiFunction combine)}
     * 有点类似，但是whenComplete没有返回值，只是对结果和异常进行一些消费操作，而thenCombine则是有返回值的，对之前两个CompletableFuture的结果进行一些操作后的结果
     */
    private static void testCombine() {
        CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("start to supply");
                    sleep(2);
                    System.out.println("end to supply");
                    return "supply";
                })
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> {
                            System.out.println("start to combine");
                            sleep(3);
                            System.out.println("end to combine");
                            return 100;
                        }),
                        (s, i) -> s.length() > i)
                .whenComplete((v, t) -> System.out.println(v));
    }

    private static void testCompose() {
        CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("start to supply");
                    sleep(3);
                    System.out.println("end to supply");
                    return "supply";
                })
                .thenCompose(s -> CompletableFuture.supplyAsync(//s为上一个CompletableFuture的输出的结果
                        () -> {
                            {
                                System.out.println("start to compose");
                                sleep(4);
                                System.out.println("end to compose");
                                return s.length();
                            }
                        })
                )
                .thenAccept(System.out::println);
    }

    @SneakyThrows
    static void sleep(long sleep) {
        TimeUnit.SECONDS.sleep(sleep);
    }
}
