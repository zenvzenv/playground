package zhengwei.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author zhengwei
 */
public class TestUnsafe2 {
    public static void main(String[] args) {
        Field f= null;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe= (Unsafe) f.get(null);
            long address=unsafe.allocateMemory(100);
            unsafe.putLong(address,1);
            System.out.println(unsafe.getLong(address));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
