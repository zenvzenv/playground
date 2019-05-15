package zhengwei.designpattern.singleton;

/**
 * 静态内部类的方式
 * @author zhengwei AKA Sherlock
 * @since 2019/5/15 12:58
 */
public class Singleton03 {
    private static Singleton03 INSTANCE;
    private Singleton03() {}
    static class Singleton04Handler {
        public static Singleton03 getInstance(){
            return INSTANCE=new Singleton03();
        }
    }

    public static void main(String[] args) {
        Singleton03 instance = Singleton04Handler.getInstance();
    }
}
