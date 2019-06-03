package zhengwei.jvm;

import org.junit.jupiter.api.Test;

import java.util.Objects;

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
}
class C{
    static {
        System.out.println("C static block");
    }
}