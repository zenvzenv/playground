package zhengwei.jvm;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/5/29 10:16
 */
public class TestClassLoader3 {
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
}
class C{
    static {
        System.out.println("C static block");
    }
}