package zhengwei.designpattern.singleton;

/**
 * 静态内部类的方式
 * @author zhengwei AKA Sherlock
 * @since 2019/5/15 12:58
 */
public class Singleton03 {

    private Singleton03() {}
    private static class Singleton04Handler {
        private static Singleton03 INSTANCE = new Singleton03();
    }
    public static Singleton03 getInstance() {
        return Singleton04Handler.INSTANCE;
    }
}
