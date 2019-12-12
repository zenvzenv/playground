package zhengwei.thread.test;

import org.junit.jupiter.api.Test;

/**
 * @author zhengwei AKA Awei
 * @since 2019/12/12 16:46
 */
public class ThreadProjectTest {
    @Test
    void test() {
        System.out.println(Integer.toBinaryString(-1 << (Integer.SIZE - 3)));
        System.out.println(Integer.toBinaryString(0 << (Integer.SIZE - 3)));
        System.out.println(Integer.toBinaryString(1 << (Integer.SIZE - 3)));
        System.out.println(Integer.toBinaryString(2 << (Integer.SIZE - 3)));
        System.out.println(Integer.toBinaryString(3 << (Integer.SIZE - 3)));
        System.out.println(Integer.toBinaryString(~(Integer.SIZE - 3)));
        System.out.println(Integer.toBinaryString((Integer.SIZE - 3)));
    }
}
