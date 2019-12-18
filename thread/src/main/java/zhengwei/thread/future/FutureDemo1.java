package zhengwei.thread.future;

import java.util.concurrent.*;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/18 12:50
 */
public class FutureDemo1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
//        testGet();
//        testGetTimeout();
//        testCancel();
        testCancel2();
    }

    /**
     * @throws InterruptedException future的get方法将抛出InterruptedException
     */
    private static void testGet() throws ExecutionException, InterruptedException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Future<?> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        //main thread
        final Thread callerThread = Thread.currentThread();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                //打断主线程，主线程在get的时候阻塞住了，打断它将不会去获取值了
                callerThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        /*
         * 主调线程会阻塞住，而不是future阻塞住
         * 如果打断了主调线程的话，那么将不会获取到结果
         * 但是线程池内的线程将不会收到影响
         */
        final Object result = future.get();
        System.out.println(result);
    }

    /**
     * {@link Future#get(long time, TimeUnit unit)}
     */
    private static void testGetTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Future<?> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
                System.out.println("===========");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        //虽然get的时候timeout了，到了指定的时间超时了，但是线程池中的线程依然会去执行
        final Object result = future.get(5, TimeUnit.SECONDS);
        System.out.println(result);
    }

    /**
     * {@link Future#isDone()}
     * Future会在一下几种情况是done状态
     * normal termination 正常结束
     * exception 出现异常
     * cancellation 被cancel
     */
    private static void testIsDone() throws ExecutionException, InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<?> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(future.get());
        boolean done = future.isDone();
        System.out.println(done);
    }

    /**
     * {@link Future#cancel(boolean mayInterruptIfRunning)}
     * cancel会在一下集中情况下失败
     * 1. task已经执行完毕了
     * 2. 这个task已经被cancel过一次了
     * <p>
     * cancel一般配合Thread.interrupted()使用
     *
     * 在task被取消成功之后，如果再去get该future的值时，将会报CancellationException，不再让我们获取该task的值了
     */
    private static void testCancel() throws ExecutionException, InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<Integer> future = executorService.submit(() -> {
            //cancel不了，没有地方执行中断
            /*while (true) {

            }*/
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*while (!Thread.interrupted()) {
                System.out.println("I am not be canceled");
            }*/
            System.out.println("I will be out");
            return 10;
        });
        //会去打断线程
        System.out.println(future.cancel(true));
        //已经被cancel过一次了，再cancel就会失败
        System.out.println(future.cancel(true));
        System.out.println(future.isDone());
        System.out.println(future.isCancelled());
        //cancel之后将不会再获取到结果
//        System.out.println(future.get());
    }

    /**
     * 就算task被cancel了之后，但是线程还是会继续向下执行，但是最终的结果我们将获取不到了
     */
    private static void testCancel2() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        Future<Integer> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
                System.out.println("=================");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("1111111111111111");
            return 10;
        });
        TimeUnit.MILLISECONDS.sleep(10);
        future.cancel(true);

    }
}
