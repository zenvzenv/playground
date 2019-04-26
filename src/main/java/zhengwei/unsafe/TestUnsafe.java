package zhengwei.unsafe;

import sun.misc.Unsafe;

/**
 * @author zhengwei
 */
public class TestUnsafe {
    public static void main(String[] args) {
        Unsafe unsafe = Unsafe.getUnsafe();
        long address=unsafe.allocateMemory(100);
        unsafe.putLong(address,1);
        System.out.println(unsafe.getLong(address));
    }
}
