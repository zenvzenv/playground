package zhengwei.thread.forkjoin;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/25 13:26
 */
public class ForkJoinRecursiveActionDemo {
    private static final int MAX_THRESHOLD = 3;
    private static final AtomicLong SUM = new AtomicLong();

    public static void main(String[] args) throws InterruptedException {
        final ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(()->{
            System.out.println("-------------");
        },new CalculateRecursiveAction(0, 10));
        forkJoinPool.awaitTermination(10, TimeUnit.SECONDS);
        Optional.of(SUM).ifPresent(System.out::println);
    }

    private static class CalculateRecursiveAction extends RecursiveAction {
        private final int start;
        private final int end;

        CalculateRecursiveAction(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - start <= MAX_THRESHOLD) {
                SUM.addAndGet(IntStream.rangeClosed(start, end).sum());
            } else {
                int middle = (start + end) >> 1;
                final CalculateRecursiveAction leftTask = new CalculateRecursiveAction(start, middle);
                final CalculateRecursiveAction rightTask = new CalculateRecursiveAction(middle + 1, end);
                leftTask.fork();
                rightTask.fork();
            }
        }
    }
}
