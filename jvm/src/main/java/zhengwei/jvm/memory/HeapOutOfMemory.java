package zhengwei.jvm.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * Java对象创建的过程(new一个对象)
 * 1. 在堆内存中创建对象的实例
 * 1. 指针碰撞：前提是JVM的堆内存空间通过一个指针进行分割，一侧是已经被占用的内存，一侧是没有被占用的内存空间
 * 2. 空闲列表：前提是JVM的堆内存空间中已被使用和未使用的内存空间交织在一起了，这时JVM需要一个列表来记录哪些空间可以使用，那么空间不可使用，
 * 接下来找出可以容纳下新对象的且为未使用的内存空间，在此空间存放内存，同时还要修改l列表上的记录。
 * 2. 为对象的实例成员变量赋初始值
 * 3. 将对象的引用返回
 * <p>
 * 对象在内存中的布局：
 * 1. 对象头
 * 2. 实例对象(即我们在一个类中声明的各项信息)
 * 3. 对象填充(可选)
 * <p>
 * 引用访问对象的方式
 * 1. 使用句柄的方式
 * 即在栈帧中的reference所指向的不是真正的实例对象，而是指向一个句柄，这个句柄同时指向实例对象和对象在元数据区的地址
 * 2. 使用直接指针的方式
 * 即在栈帧中的reference所指向的是真正的实例对象，并且队中的对象还会指向元数据区的地址
 * <p>
 * 本案例中使用的JVM参数：
 * 设置堆的最小大小：-Xms5m
 * 设置堆的最大大小：-Xmx5m
 * 设置堆出现溢出时转储文件：-XX:+HeapDumpOnOutOfMemoryError
 *
 * @author zhengwei AKA Awei
 * @since 2019/12/30 10:31
 */
public class HeapOutOfMemory {
    public static void main(String[] args) {
        List<HeapOutOfMemory> list = new ArrayList<>();
        while (true) {
            list.add(new HeapOutOfMemory());
            //添加gc之后，将不会出现OOM错误
            //显示调用JVM的gc，JVM会尝试进行一次gc
            System.gc();
        }
    }
}
