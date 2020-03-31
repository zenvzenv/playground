package zhengwei.jvm.gc;

/**
 * -verbose:gc：详细输出gc信息
 * -Xms20m：表示堆的初始大小
 * -Xmx20m：表示堆的最大大小
 * -Xmn10m：表示堆中的新生代大小
 * -XX:+PrintGCDetails：打印垃圾回收的详细信息
 * -XX:SurvivorRatio=8
 *
 * 1. 当新生代无法容纳下一个待分配的对象时，那么该对象将直接在老年代分配内存空间
 * 2. 对于新生代的回收，回收的内存空间由两部分组成：分别是确实是被清除的对象和晋升到老年代的对象
 * 3. full gc会导致业务线程停止，知道gc完成，业务线程才会继续执行
 *
 * @author zhengwei AKA Awei
 * @since 2020/2/6 20:54
 */
public class ObjectCreateAtEden {
    public static void main(String[] args) throws InterruptedException {
        int size = 1024 * 1024;
        byte[] allocate1 = new byte[2 * size];
        byte[] allocate2 = new byte[2 * size];
        byte[] allocate3 = new byte[2 * size];
        byte[] allocate4 = new byte[4 * size];
        System.out.println("hello world");
        Thread.currentThread().join();
    }
}
