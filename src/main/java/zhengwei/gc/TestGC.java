package zhengwei.gc;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/4/1 14:38
 */
public class TestGC {
    private static final int _1MB = 1024*1024;

    public static void main(String[] args) {
//        testAllocation();
//        testPretenureSizeThreshold();
        testTenuringThreshold();
    }


    /**
     * 对象优先在Eden分配内存
     */
    private static void testAllocation(){
        //-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+UseSerialGC
        byte[] allocation1,allocation2,allocation3,allocation4;
        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        allocation4 = new byte[4 * _1MB];//出现一次Minor GC
    }

    /**
     * 大对象直接在老年代分配内存
     */
    private static void testPretenureSizeThreshold(){
        //-verbose:gc
        // -Xms20M Java堆大小，且不可扩展
        // -Xmx20M 新生代大小，Eden和From和To的大小比例时8:1:1
        // -Xmn10M 老年代大小
        // -XX:+PrintGCDetails 打印GC细节
        // -XX:SurvivorRatio=8 指定Eden和Survivor的大小比例
        // -XX:+UseSerialGC 使用SerialGC
        // -XX:PretenureSizeThreshold=3145728 当要分配的对象大于指定数值时，直接在老年代分配内存
        byte[] allocation;
        allocation = new byte[4 * _1MB];
    }

    /**
     * 长期存活的对象将进入老年代
     */
    private static void testTenuringThreshold(){
        byte[] allocation1,allocation2,allocation3;
        allocation1 = new byte[_1MB / 4];
        allocation2 = new byte[4 * _1MB];
        allocation3 = new byte[4 * _1MB];
        allocation3 = null;
        allocation3 = new byte[4 * _1MB];
    }
}
