package zhengwei.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author zhengwei
 */
public class OffheapIntArray {
    //此list分配的空间
    private long address;
    //默认分配空间大小
    private static final int defaultSize=1024;
    private OffheapIntArray(Integer size) throws NoSuchFieldException, IllegalAccessException {
        if (size == null){
            address=alloc(defaultSize * 4 * 8);
        } else {
            address=alloc(size * 4 *8);
        }
    }
    private Unsafe getUnsafe() throws IllegalAccessException, NoSuchFieldException {
        Field f=Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }
    private long alloc(int size) throws NoSuchFieldException, IllegalAccessException {
        return getUnsafe().allocateMemory(size);
    }
    private int get(int index) throws NoSuchFieldException, IllegalAccessException {
        return getUnsafe().getInt(address + index * 4 *8);
    }
    private void set(int index, int value) throws NoSuchFieldException, IllegalAccessException {
        getUnsafe().putInt(address + index * 4 * 8, value);
    }
    private void free() throws NoSuchFieldException, IllegalAccessException {
        if (address==0){
            return;
        }
        getUnsafe().freeMemory(address);
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        OffheapIntArray offheapIntArray=new OffheapIntArray(10);
        offheapIntArray.set(0,111);
        offheapIntArray.set(1,222);
        offheapIntArray.set(2,333);
        offheapIntArray.set(3,444);
        System.out.println(offheapIntArray.get(0));
        System.out.println(offheapIntArray.get(1));
        System.out.println(offheapIntArray.get(2));
        System.out.println(offheapIntArray.get(3));
        offheapIntArray.free();
    }
}
