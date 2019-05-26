package zhengwei.jvm;

/**
 * 类加载过程:加载->连接(验证->准备->解析)->初始化->使用->卸载
 * 准备阶段给给类中的静态变量赋上默认值
 * 初始化阶段给类中的静态变量赋上指定的值
 * @author zhengwei AKA Sherlock
 * @since 2019/5/25 20:09
 */
public class TestClassLoader2 {
    public static void main(String[] args) {
        Singleton1 singleton1=Singleton1.getInstance();
        System.out.println("count1:"+ singleton1.count1);
        System.out.println("count2:"+ singleton1.count2);
        System.out.println("==========================");
        Singleton2 singleton2=Singleton2.getInstance();
        System.out.println("count1:"+ singleton2.count1);
        System.out.println("count2:"+ singleton2.count2);
    }
}
class Singleton1{
    public static int count1;
    public static int count2=0;
    private static Singleton1 singleton=new Singleton1();
    private Singleton1(){
        count1++;
        count2++;
    }
    public static Singleton1 getInstance(){
        return singleton;
    }
}
class Singleton2{
    public static int count1=1;

    private static Singleton2 singleton=new Singleton2();
    private Singleton2(){
        count1++;
        count2++;//准备阶段的重要意义
    }
    public static int count2=0;
    public static Singleton2 getInstance(){
        return singleton;
    }
}