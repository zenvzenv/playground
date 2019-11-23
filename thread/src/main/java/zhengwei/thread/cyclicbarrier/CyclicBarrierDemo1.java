package zhengwei.thread.cyclicbarrier;

import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * CyclicBarrier，与CountDownLatch类似，但有本质上的不同
 * CyclicBarrier中的await()相当于CountDownLatch中的countdown()，都会去让计数器减一
 * 相同点：
 * 1. 都是等待其余线程结束之后，再进行下一步操作
 * 不同点：
 * 1. CountDownLatch就像是一个大总管，所有线程在完成自己的工作之后都会去CountDownLatch那边countdown一下并退出当前线程结束生命周期，知道CountDownLatch中的count为0时，去执行接下来的操作
 * 2. CyclicBarrier则弱化了大总管的概念，而是将判断所有线程是否都准备完毕的工作交给了每个线程去完成，如果有的线程执行的快，那么此线程将不会退出结束生命周期而是会继续等待，直到所有线程都执行完毕的时候，大家一起退出线程结束生命周期
 * 3. CountDownLatch不能reset，count递减到0就不会再回去了
 * 4. CountDownLatch中工作线程互补关心
 * 5. CyclicBarrier中工作线程必须等到同一个共同的点才去执行接下来的某个动作
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/14 19:21
 */
public class CyclicBarrierDemo1 {
    /**
     * 当T1结束工作之后，T1并不会退出结束，而是会继续等待，等待T2的结束
     * 当T1和T都结束的时候，整个程序才会结束
     * 这就是与CountDownLatch的不同之处
     */
    @Test
    void useCyclicBarrier() throws InterruptedException, BrokenBarrierException {
        //接受一个Runnable的接口，等到所有线程都结束的时候回调该Runnable
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> System.out.println("All thread finished."));
        new Thread(() -> {
            try {
                System.out.println("[T1] start to work...");
                TimeUnit.SECONDS.sleep(10);
                System.out.println("[T1] finished.");
                System.out.println("[T1] wait for other threads to finish");
                cyclicBarrier.await();
                System.out.println("[T1] other threads are finished");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                System.out.println("[T2] start to work...");
                TimeUnit.SECONDS.sleep(20);
                System.out.println("[T2] finished.");
                cyclicBarrier.await();
                System.out.println("[T2] wait for other threads to finish");
                System.out.println("[T2] other threads are finished");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
        cyclicBarrier.await();
    }

    /**
     * 如还有线程在等待时调用了reset()方法，则会抛出java.util.concurrent.BrokenBarrierException异常
     */
    @Test
    void testCyclicBarrierReset() {
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(2, () -> System.out.println("zwzwzw"));
        new Thread(() -> {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
        //reset==init==finished
        cyclicBarrier.reset();
    }
}
