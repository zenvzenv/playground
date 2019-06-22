package zhengwei.jvm.classloader;

import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * -XX:+TraceClassLoading->用于类的加载信息并打印出来
 *
 * -XX:(+/-)[option].表示开启/关闭option
 * -XX:[option]=[value],表示给选项赋值
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/5/24 9:07
 */
public class TestClassLoader {
    /**
     * 对于静态字段来说，只有直接定义了该字段的类才会被初始化
     * 虽然是Child调用了Parent的str静态字段，但是str位于Parent中，所以只有Parent类被初始化了，Child不会初始化
     * 当一个类子类被初始化时，jvm要求其所有父类都要初始化完毕
     * 静态代码块会在初始化阶段执行
     */
    @Test
    void testSubClassStaticVar(){
        System.out.println(Child.str2);
    }

    /**
     * 对于常量而言，常量在编译阶段就会存入到调用这个方法所在的类的常量池中
     * 本质上，调用该类并没有直接引用到定义常量的类，因此不会触发定义常量类的初始化
     * 即在编译阶段Parent中的常量就已经存入了TestClassLoader的常量池中，此时TestClassLoader与Parent再也没有任何关系了，所以在TestClassLoader调用Parent的常量不会直接使用到Parent类，所以Parent不会被初始化
     * 我们甚至可以删除Parent的class文件，这样也不会影响程序的正常运行
     * 助记符：
     *      ldc表示将int,float或String类型的常量从常量池中推送至栈顶
     *      bipush表示将单字节(-128~127)的常量从常量池中推送至栈顶
     *      sipush表示将一个短整型常量值(-32768~32767)的常量从常量池中推送
     *      iconst_m1~iconst_5表示将int类型的数字-1~5推送至栈顶
     * 当一个常量的值并非编译期可以确定的，那么其值就不会被放到调用类的常量池中。
     * 这时程序运行时，会导致主动使用这个常量所在的类，显然会导致这个类被jvm初始化。
     *
     */
    @Test
    void testStaticVar(){
        System.out.println(Parent.str1);//ldc
        System.out.println(Parent.s);//bipush
        System.out.println(Parent.i1);//iconst_1
        System.out.println(Parent.i2);//iconst_5
        System.out.println(Parent.i3);//ldc
        System.out.println(Parent.i4);//sipush
        System.out.println(Parent.uuid);
    }
    /**
     * jvm只会在类第一次主动使用的时候去初始化该类
     * 如果已经时第二次主动使用那么将不去再去实例化该类
     */
    @Test
    void testNewObject(){
        Parent parent=new Parent();
        System.out.println("=====");
        Parent parent2=new Parent();
    }

    /**
     * 对于数组实例来说，其类型是由jvm在运行期间动态生成的，一维数组表示为[Lzhengwei.jvm.Parent，二维数组表示为：[[Lzhengwei.jvm.Parent
     * 这种形式，动态生成的类型，其父类就是Object
     * 对于数组而言，JavaDoc经常将构成数组的元素称为Component，实际上就是将一个数据降低一个纬度的类型。
     * 助记符：
     *      anewarray表示创建一个引用类型的(如类，接口，数组)数组，并将其引用值压入栈顶
     *      newarray表示创建一个指定的原始类型(如int、float、char等)的数组，并将其引用值压入栈顶
     */
    @Test
    void testNewArray(){
        Parent[] parents=new Parent[1];
        System.out.println(parents.getClass());
        System.out.println(parents.getClass().getSuperclass());
        Parent[][] parents1=new Parent[1][1];
        System.out.println(parents1.getClass());
        System.out.println(parents1.getClass().getSuperclass());
    }

    /**
     * 当一个接口初始化时，并不要求其父接口都初始化完成->是因为接口中的都是常量(public static final修饰的)，就算别的类中引用了子类中的常量，子类的常量在编译阶段已经在调用类的常量池中，所以不需要父接口初始化
     * 虽然JVM会加载ChildInterface和ParentInterface但并不代表着JVM会去初始化这两个接口
     * 只有真正使用到父接口的时候(如引用父接口中定义的常量时)才会初始化
     */
    @Test
    void testInterfaceInit(){
        System.out.println(ChildInterface.bc);
    }
}
class Parent{
    public static String str = "parent str";
    public static final String str1 = "parent";
    public static final short s=127;
    public static final int i1=1;
    public static final int i2=5;
    public static final int i3=32768;
    public static final int i4=32767;
    public static final String uuid= UUID.randomUUID().toString();
    static {
        System.out.println("parent static block");
    }
    {
        System.out.println("parent block");
    }
}
class Child extends Parent implements ChildInterface{
    public static /*final*/ String str2 = "child";
    static {
        System.out.println("child static block");
    }
    {
        System.out.println("child block");
    }
}

/**
 * 注意：Java中允许非静态代码块的出现
 * 区别就在于静态代码块只有在类初始化的时候执行一次，下次再去初始化类的时候不会再去执行了
 * 而非静态代码块在类每次初始化的时候都会去执行一遍，非静态代码块在构造函数之前执行
 */
interface ParentInterface{
    /**
     * 如果ParentInterface被初始化了的话，那么thread这个常量就会赋值
     * 那么线程中的代码块就会被执行，从而可以证明相关结论
     */
    Thread threadParent=new Thread(){
        {
            System.out.println("ParentInterface invoke");
        }
    };
    int ap=5;
}
interface ChildInterface extends ParentInterface{
    int ac=6;
    String bc=UUID.randomUUID().toString();
    Thread threadChild=new Thread(){
        {
            System.out.println("ChildInterface invoke");
        }
    };
}