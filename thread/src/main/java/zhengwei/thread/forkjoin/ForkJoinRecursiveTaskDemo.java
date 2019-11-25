package zhengwei.thread.forkjoin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

/**
 * ForkJoin-分而治之
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/25 12:39
 */
public class ForkJoinRecursiveTaskDemo {
    private final static int MAX_THREAD = 3;

    public static void main(String[] args) {
        final ForkJoinPool forkJoinPool = new ForkJoinPool();
        final ForkJoinTask<Integer> future = forkJoinPool.submit(new CalculateRecursiveTask(0, 10));
        try {
            System.out.println(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static class CalculateRecursiveTask extends RecursiveTask<Integer> {
        private final int start;
        private final int end;

        public CalculateRecursiveTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= MAX_THREAD) {
                return IntStream.rangeClosed(start, end).sum();
            } else {
                int mid = (start + end) >> 1;
                final CalculateRecursiveTask leftTask = new CalculateRecursiveTask(start, mid);
                final CalculateRecursiveTask rightTask = new CalculateRecursiveTask(mid + 1, end);
                leftTask.fork();
                rightTask.fork();
                return leftTask.join() + rightTask.join();
            }
        }
    }
}
