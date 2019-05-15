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

    public static void main(String[] args) {
        for (int i=0;i<100;i++){
            new Thread(()-> System.out.println(Singleton03.getInstance().hashCode())).start();
        }
    }
}
