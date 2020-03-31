package zhengwei.jvm.gc;

/**
 * CMS
 * -verbose:gc
 * -Xms20m
 * -Xmx20m
 * -Xmn10m
 * -XX:+PrintGCDetails
 * -XX:+UseConcMarkSweepGC
 * @author zhengwei AKA Awei
 * @since 2020/2/13 9:08
 */
public class WatchCMSGc {
    public static void main(String[] args) {
        int size = 1024 * 1024;
        byte[] b1 = new byte[4 * size];
        System.out.println(111111);
        byte[] b2 = new byte[4 * size];
        System.out.println(222222);
        byte[] b3 = new byte[4 * size];
        System.out.println(333333);
        byte[] b4 = new byte[2 * size];
        System.out.println(444444);
    }
}
