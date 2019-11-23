package zhengwei.designpattern.singleton;

/**
 * 枚举类型单例模式
 * 可以避免被饭序列化，真正意义上的单例模式
 */
public enum Singleton04 {
    INSTANCE;
    public void m(){
        System.out.println("111");
    }

    public static void main(String[] args) {
        Singleton04 instance = Singleton04.INSTANCE;
        instance.m();
    }
}
