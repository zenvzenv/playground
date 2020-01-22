package zhengwei.jvm.memory;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * 死锁
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/16 19:34
 */
public class DeadLock {
    private static final class A {
        @SneakyThrows
        public static synchronized void m() {
            System.out.println("method from A");
            TimeUnit.SECONDS.sleep(1);
            B.m();
        }
    }

    private static final class B {
        @SneakyThrows
        public static synchronized void m() {
            System.out.println("method from B");
            TimeUnit.SECONDS.sleep(1);
            A.m();
        }
    }

    public static void main(String[] args) {
        new Thread(A::m, "T1").start();
        new Thread(B::m, "T2").start();
    }
}
