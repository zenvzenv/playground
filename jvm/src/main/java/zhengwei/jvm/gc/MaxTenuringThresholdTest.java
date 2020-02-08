package zhengwei.jvm.gc;

import java.util.concurrent.TimeUnit;

/**
 * -verbose:gc
 * -Xmx200m
 * -Xmn50m
 * -XX:TargetSurvivorRatio=60
 * -XX:+PrintTenuringDistribution
 * -XX:+PrintGCDetails
 * -XX:+PrintGCDateStamps
 * -XX:+UseConcMarkSweepGC
 * -XX:+UseParNewGC
 * -XX:MaxTenuringThreshold=3
 * </p>
 * 经历多次Minor GC后，存活的对象会在From Survivor和To Survivor之间来回存放，而这一前提就是两个空间有足够多的空间来存放这些数据，在GC算法中，
 * 会计算每个对象的年龄，如果空间中某个年龄的对象已经占据了Survivor空间的50%(这个比例可以自定义)，那么这时JVM会自动调整阈值(调整之后的阈值不会比MaxTenuringThreshold大)
 * 阈值会波动，不在使用默认的晋升阈值去将新生代中的对象晋升到老年代，
 * 因为默认的晋升阈值会会导致Survivor空间不足，所以需要调整阈值，让这些存活的老对象尽快晋升到老年代。
 *
 * @author zhengwei AKA Awei
 * @since 2020/2/8 15:54
 */
public class MaxTenuringThresholdTest {
    public static void main(String[] args) throws InterruptedException {
        byte[] b1 = new byte[512 * 1024];
        byte[] b2 = new byte[512 * 1024];
        newByteArray();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("111111");

        newByteArray();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("222222");

        newByteArray();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("333333");

        newByteArray();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("444444");

        byte[] b3 = new byte[1024 * 1024];
        byte[] b4 = new byte[1024 * 1024];
        byte[] b5 = new byte[1024 * 1024];

        newByteArray();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("55555");

        newByteArray();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("666666");

        System.out.println("hello world");
    }

    private static void newByteArray() {
        for (int i = 0; i < 50; i++) {
            byte[] temp = new byte[1024 + 1024];
        }
    }
}
