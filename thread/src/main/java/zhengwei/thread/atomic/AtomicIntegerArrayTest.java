package zhengwei.thread.atomic;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 原子整型数组
 *
 * @author zhengwei AKA Awei
 * @since 2019/10/11 19:58
 */
class AtomicIntegerArrayTest {
    private static AtomicIntegerArray array;

    @BeforeAll
    static void testCreateAtomicIntegerArray() {
        array = new AtomicIntegerArray(10);
        System.out.println(array.length());
    }

    @Test
    void testSetAndGet() {
        for (int i = 0; i < array.length(); i++) {
            //延迟赋值，最终会赋值
            array.lazySet(i, new Random().nextInt(1000));
            //立即赋值
            array.set(i, new Random().nextInt(1000));
        }
        for (int i = 0; i < array.length(); i++) {
            System.out.println(array.get(i));
        }
    }

    @Test
    void testGetAndSet() {
        int[] originalArray = new int[10];
        originalArray[5] = 5;
        array = new AtomicIntegerArray(originalArray);
        System.out.printf("old value->%d\n", array.getAndSet(5, 10));
        System.out.printf("new value->%d\n", array.get(5));
    }
}
