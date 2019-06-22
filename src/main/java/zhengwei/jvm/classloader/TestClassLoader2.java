package zhengwei.jvm.classloader;

/**
 * 类加载过程:加载->连接(验证->准备->解析)->初始化->使用->卸载
 * 准备阶段给给类中的静态变量赋上默认值
 * 初始化阶段给类中的静态变量赋上指定的值
 * 只有在对类的首次使用的时候才会触发初始化
 * 整个过程大致如下
 *  1.Singleton1中：
 *      a.在调用Singleton1.getInstance()之前，Singleton1是不没有初始化的,只是经历了加载->验证->准备->解析阶段，还没有到达初始化阶段
 *      b.那么在准备阶段中，JVM会为类中的静态变量进行赋默认值(注不是程序员指定的值)，其余的代码将不会执行.那么Singleton1中的count1和count2的值都是0
 *      c.当调用到Singleton1.getInstance()静态方法时，就会对Singleton1的主动使用，那么就会对Singleton1进行初始化操作
 *      d.初始化操作是从上到下开始执行代码的，count1没有指定值那么它的值还是0，count2指定了0，然后经过构造函数，count1和count2各加1之后count1和count2都编程了2
 *  2.Singleton2中：
 *      a.在调用Singleton1.getInstance()之前，Singleton1是不没有初始化的,只是经历了加载->验证->准备->解析阶段，还没有到达初始化阶段
 *      b.那么在准备阶段中，JVM会为类中的静态变量进行赋默认值(注不是程序员指定的值)，其余的代码将不会执行.那么Singleton1中的count1和count2的值都是0
 *      c.当调用到Singleton1.getInstance()静态方法时，就会对Singleton1的主动使用，那么就会对Singleton1进行初始化操作
 *      d.初始化操作是从上到下执行的，count1指定了初始值是1，然后执行new Singleton2()，会触发构造函数，此时的count1的值是1，count2的值还是准备阶段的默认值0，经过构造函数之后，count1变成2，count2的值变成1
 *      e.随后程序对count进行赋初始值，如果这时没有指定count2的初始值，那么count2的值还是经过构造函数的值，如果指定了初始值那么count2的值会被重新赋上指定的初始值
 * @author zhengwei AKA Sherlock
 * @since 2019/5/25 20:09
 */
public class TestClassLoader2 {
    public static void main(String[] args) {
        Singleton1 singleton1=Singleton1.getInstance();
        System.out.println("count1:"+ Singleton1.count1);
        System.out.println("count2:"+ Singleton1.count2);
        System.out.println("==========================");
        Singleton2 singleton2=Singleton2.getInstance();
        System.out.println("count1:"+ Singleton2.count1);
        System.out.println("count2:"+ Singleton2.count2);
    }
}
class Singleton1{
    //准备阶段的值是0
    public static int count1;
    //准备阶段的值是0
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
    public static int count2;
    public static Singleton2 getInstance(){
        return singleton;
    }
}