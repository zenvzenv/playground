package zhengwei.jvm.gc;

/**
 * -verbose:gc
 * -Xms10m
 * -Xmx20m
 * -XX:+UseG1GC
 * -XX:MaxGCPauseMillis=200m
 * -XX:+PrintGCDetails
 *
 * @author zhengwei AKA Awei
 * @since 2020/2/29 16:27
 */
public class TestG1 {
    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) {
        byte[] b1 = new byte[_1MB];
        byte[] b2 = new byte[_1MB];
        byte[] b3 = new byte[_1MB];
        byte[] b4 = new byte[_1MB];
        System.out.println("hello g1");
    }
}
