package zhengwei.leetcode.daily;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhengwei AKA Awei
 * @since 2020/4/28 15:03
 */
public class LeetCode1114PrintInOrder {
    private static final class Foo {
        private static final Lock lock = new ReentrantLock();
        private static final Condition condition1 = lock.newCondition();
        private static final Condition condition2 = lock.newCondition();
        private final AtomicInteger stage = new AtomicInteger(1);

        public Foo() {

        }

        public void first(Runnable printFirst) throws InterruptedException {
            lock.lock();
            try {
                if (stage.get() == 1) {
                    printFirst.run();
                    stage.incrementAndGet();
                    condition1.signal();
                }
            } finally {
                lock.unlock();
            }
        }

        public void second(Runnable printSecond) throws InterruptedException {
            lock.lock();
            try {
                while (stage.get() != 2) {
                    condition1.wait();
                }
                printSecond.run();
                stage.incrementAndGet();
                condition2.signal();
            } finally {
                lock.unlock();
            }
        }

        public void third(Runnable printThird) throws InterruptedException {
            lock.lock();
            try {
                while (stage.get() != 3) {
                    condition2.wait();
                }
                printThird.run();

            } finally {
                lock.unlock();
            }
        }
    }

    public static int insertBits(int N, int M, int i, int j) {
        M <<= i;
        N &= (1 << j);
        return N |= M;
    }

    public static void main(String[] args) {
        System.out.println(insertBits(1024, 19, 2, 6));
    }
}
