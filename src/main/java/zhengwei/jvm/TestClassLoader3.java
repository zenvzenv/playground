package zhengwei.jvm;

import com.sun.crypto.provider.AESKeyGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/5/29 10:16
 */
public class TestClassLoader3 {
    static {
        System.out.println("TestClassLoader3 init...");
    }
    public static void main(String[] args) throws ClassNotFoundException {
        /*
         * 如果String是被Bootstrap Classloader加载的，那么它的类加载器将返回null
         * 由类加载器加载类时，不是对类的主动使用，所有不会触发类的初始化过程
         */
        Class<?> clazz1=Class.forName("java.lang.String");
        System.out.println(clazz1.getClassLoader());
        /*
         * C类由AppClassLoader加载器加载
         * 由反射加载类时，是对类的主动使用，所以会触发类的初始化过程
         */
        Class<?> clazz2=Class.forName("zhengwei.jvm.C");
        System.out.println(clazz2.getClassLoader());
    }

    /**
     * JVM中类加载器的层次关系：Bootstrap ClassLoader->ExtClassLoader->AppClassLoader
     * 在有些实现中BootstrapClassLoader会用null表示
     */
    @Test
    void testParentClassLoader(){
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        System.out.println(classLoader);
        while (!Objects.isNull(classLoader)){
            classLoader=classLoader.getParent();
            System.out.println(classLoader);
        }
    }

    /**
     * CLassLoader JavaDoc
     * 数组的class对象不是由ClassLoader创建的，而是由JVM在运行期间自动创建的，
     */
    @Test
    void testArrayClassLoader(){
        String[] strings=new String[2];
        System.out.println(strings.getClass().getClassLoader());//根加载器->null
        System.out.println("--------");
        C[] cs=new C[2];
        System.out.println(cs.getClass().getClassLoader());
        System.out.println("--------");
        int[] ints=new  int[2];
        System.out.println(ints.getClass().getClassLoader());//没有classloader->null
    }

    /**
     * 根类加载器会去C:\Program Files\Java\jdk1.8.0_181\jre\classes目录下去加载类
     * 扩展类加载器会去C:\Windows\Sun\Java\lib\ext目录下去加载
     * 如果我们把类放到C:\Program Files\Java\jdk1.8.0_181\jre\classes目录中的一个，将会由根类加载器去加载，那么加载类的类加载器将会是null
     * 在HotSpot虚拟机中用null来表示根类加载器
     */
    @Test
    void testBootClassLoader() throws ClassNotFoundException {
        System.out.println(System.getProperty("sun.boot.class.path"));
        System.out.println(System.getProperty("java.ext.dirs"));
        System.out.println(System.getProperty("java.class.path"));
        MyClassLoader loader1=new MyClassLoader("loader1");
        loader1.setPath("e:/temp/");
        Class<?> clazz = loader1.loadClass("zhengwei.jvm.TestClass");
        System.out.println("class : "+clazz.hashCode());
        System.out.println("class loader : "+clazz.getClassLoader());

        AESKeyGenerator aesKeyGenerator=new AESKeyGenerator();
        System.out.println(aesKeyGenerator.getClass().getClassLoader());//ExtClassLoader
    }

    /**
     * 类只会被加载一次，下次如果再去加载已加载过的类的话，那么将会直接返回之前加载好的类对象。
     * @throws Exception 异常
     */
    @Test
    void testClassLoader() throws Exception {
        MyClassLoader loader1=new MyClassLoader("loader1");
        MyClassLoader loader2=new MyClassLoader("loader2");

        Class<?> clazz1 = loader1.loadClass("zhengwei.jvm.MySample");
        Class<?> clazz2 = loader2.loadClass("zhengwei.jvm.MySample");
        //输出true，系统类加载器加载了MySample，下次加载只会返回已经加载好的类对象
        System.out.println(clazz1==clazz2);

        Object o1 = clazz1.newInstance();
        Object o2 = clazz2.newInstance();
        //方法名，方法需要传出的参数类型
        Method method = clazz1.getMethod("setMySample", Object.class);
        method.invoke(o1,o2);
    }

    /**
     * 前提：删除MySample.class文件
     * 1.每个类加载器都有自己的命名空间，命名空间由该类加载器及其所有父类加载器的类所组成
     * 2.在同一个命名空间中，不会出现类的完整名字(包括类中的包名)相同的两个类
     * 3.在不同的命名空间中，有可能出现类的完整名字相同的类
     * 4.同一个命名空间中类是相互可见的
     * 5.子类加载器的命名空间包含所有的父类加载器。因此子类加载器加载的类能够看到父类加载器加载的类，例如系统类加载器加载的类能够看到根类加载器加载的类
     * 6.父类加载器加载的类看不到子类加载器加载的类
     * 7.如果两个加载器没有直接或间接的关系，那么两个加载器各自加载的类互不可见
     * java.lang.ClassCastException: zhengwei.jvm.MySample cannot be cast to zhengwei.jvm.MySample
     * @throws Exception 异常
     *
     * 使用双亲委托机制的好处
     *  1.可以确保Java核心类库的类型安全：所有Java应用都至少会引用java.lang.Object类，也就是说或在运行期间，java.lang.Object会被加载到JVM中，
     *    如果这个加载过程由Java应用自己的类加载器去完成，那么很有可能在JVM内存中存在多个版本的java.lang,.Object类，而且这些类是不兼容的，相互不可见的(命名空间在起作用)
     *    借助于双亲委托机制，Java核心类库中的类加载工作由启动类加载器去完成加载，从而确保Java应用所使用的都是统一版本的Java类库。
     *  2.可以确保Java核心类库不会被自定义的类所替代
     *  3.不同的类加载器可以为相同的名称(binary name)的类创建额外的命名空间，相同名称的类可以并存在JVM中，只要用不同的类加载器去加载它们即可。不同的类加载器加载器的类是不兼容的，
     *    这就相当于在JVM中创建了一个又一个相互隔离的Java类空间，这类技术在很多框架中都的得到实际应用。
     */
    @Test
    void testClassLoaderNamespace() throws Exception{
        MyClassLoader loader1=new MyClassLoader("loader1");
        MyClassLoader loader2=new MyClassLoader("loader2");
        loader1.setPath("e:/temp/");
        loader2.setPath("e:/temp/");
        Class<?> clazz1 = loader1.loadClass("zhengwei.jvm.MySample");
        Class<?> clazz2 = loader2.loadClass("zhengwei.jvm.MySample");
        //输出false，系统类加载器加载了MySample，下次加载只会返回已经加载好的类对象
        System.out.println(clazz1==clazz2);

        Object o1 = clazz1.newInstance();
        Object o2 = clazz2.newInstance();
        //方法名，方法需要传出的参数类型
        Method method = clazz1.getMethod("setMySample", Object.class);
        //第一个参数：调用对象的方法，第二个参数：方法所需参数
        method.invoke(o1,o2);
    }

    /**
     * 扩展类加载器需要把class文件打进jar包中才会去加载
     */
    @Test
    void testExtClassLoader(){
        System.out.println(TestClassLoader3.class.getClassLoader());
        System.setProperty("java.ext.paths","e:/temp");
        System.out.println(MyClassLoader.class.getClassLoader());
    }
}
class C{
    static {
        System.out.println("C static block");
    }
}