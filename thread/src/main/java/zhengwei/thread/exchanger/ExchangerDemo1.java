package zhengwei.thread.exchanger;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exchanger，顾名思义就是**两个**现场之间交换数据
 * 线程必须成对出现，否则将会有线程结束不了
 *
 * @author zhengwei AKA Awei
 * @since 2019/11/18 13:42
 */
class ExchangerDemo1 {
    /**
     * V r = exchange(V x)方法
     * 1. x代表的是当前现场交换给另外一个现场的线程，即发送出去的数据
     * 2. r代表当前线程从另外一个现场交换而来的数据，即接收到的数据
     * 3. 如果线A程已经到达交换点，而线程B没有到达交换点，那线程A将会阻塞住，等待线程B到达交换点，然后交换数据，交换完数据之后，两个线程执行后续操作
     * 4. 如果多个线程去调用exchange的话，将会有问题，exchange只适用于两个线程去交换
     * 5. 使用时，线程必须成对出现，如果有落单的线程的话，那么那个落单的线程将结束不了
     */
    @Test
    void testExchangeMethod() throws InterruptedException {
        final Exchanger<String> exchanger = new Exchanger<>();
        new Thread(() -> {
            try {
                System.out.printf("%s start.\n", Thread.currentThread().getName());
                //交换数据的线程会阻塞
                TimeUnit.SECONDS.sleep(10);
                String message = exchanger.exchange("The message from T1");
                System.out.printf("The message content -> [ %s ]\n", message);
                System.out.printf("%s end\n", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T1").start();
        /*new Thread(() -> {
            try {
                System.out.printf("%s start.\n", Thread.currentThread().getName());
                String message = exchanger.exchange("The message from T1_1");
                System.out.printf("The message content -> [ %s ]\n", message);
                System.out.printf("%s end\n", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T1_1").start();*/
        new Thread(() -> {
            try {
                System.out.printf("%s start.\n", Thread.currentThread().getName());
                String message = exchanger.exchange("The message from T2");
                System.out.printf("The message content -> [ %s ]\n", message);
                System.out.printf("%s end.\n", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T2").start();
        Thread.currentThread().join();
    }

    /**
     * exchange交换的数据本质上一同一份数据(即指向的是同一个堆内存区域)
     */
    @Test
    void testVarAddressConsistent() {
        final Exchanger<Object> exchanger = new Exchanger<>();
        new Thread(() -> {
            try {
                Object aObj = new Object();
                System.out.println(Thread.currentThread().getName() + " will send the obj -> " + aObj);
                Object rObj = exchanger.exchange(aObj);
                System.out.println(Thread.currentThread().getName() + " receive the obj  -> " + rObj);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T1").start();

        new Thread(() -> {
            try {
                Object bObj = new Object();
                System.out.println(Thread.currentThread().getName() + " will send the obj -> " + bObj);
                Object rObj = exchanger.exchange(bObj);
                System.out.println(Thread.currentThread().getName() + " receive the obj  -> " + rObj);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T2").start();
    }

    /**
     * exchange可循环使用
     */
    @Test
    void testCycleUseExchange() throws InterruptedException {
        final Exchanger<Integer> exchanger = new Exchanger<>();
        new Thread(() -> {
            try {
                AtomicReference<Integer> value = new AtomicReference<>(1);
                while (true) {
                    value.set(exchanger.exchange(value.get()));
                    System.out.printf("%s has value %d\n", Thread.currentThread().getName(), value.get());
                    TimeUnit.SECONDS.sleep(3);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T1").start();
        new Thread(() -> {
            try {
                AtomicReference<Integer> value = new AtomicReference<>(2);
                while (true) {
                    value.set(exchanger.exchange(value.get()));
                    System.out.printf("%s has value %d\n", Thread.currentThread().getName(), value.get());
                    TimeUnit.SECONDS.sleep(2);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "T2").start();
        Thread.currentThread().join();
    }
}
