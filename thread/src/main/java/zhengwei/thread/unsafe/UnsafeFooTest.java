package zhengwei.thread.unsafe;

import lombok.Data;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 用unsafe做一些邪恶的事情^_^
 * 除了使用系统的类加载器(根加载器，扩展类加载器和应用类加载器)或者自定义的类加载器去加载类
 * 还可以使用unsafe去加载类，而且不用经过初始化
 *
 * @author zhengwei AKA Awei
 * @since 2019/10/19 11:22
 */
public class UnsafeFooTest {
    @Data
    static class Simple {
        private long l;

        public Simple() {
            this.l = 1;
            System.out.println("=======");
        }
    }

    @Test
    void testUnsafeCreateSimple() throws Exception {
        //是对类的主动使用，会初始化类
//        Simple simple = Simple.class.newInstance();
        //不是对类的主动使用，不会初始化类
//        Class<?> aClass = Class.forName("zhengwei.thread.unsafe.UnsafeFooTest.Simple");

        //通过unsafe可以绕过对类的初始化而创建一个类的实例
        Unsafe unsafe = UnsafeTest.getUnsafe();
        Simple simple = (Simple) unsafe.allocateInstance(Simple.class);
        //结果输出为0，并没有调用构造器去初始化一个实例
        //unsafe绕过了初始化的步骤
        System.out.println(simple.getL());
        System.out.println(simple.getClass().getClassLoader());
    }

    static class Guard {
        private int access_allow = 1;

        private boolean isAllow() {
            return 42 == access_allow;
        }

        private void work() {
            if (isAllow()) {
                System.out.println("i am allowed to do something");
            } else {
                System.out.println("i can not do anything");
            }
        }
    }

    @Test
    void testGuard1() {
        Guard guard = new Guard();
        guard.work();
    }

    @Test
    void testGuard2() throws NoSuchFieldException {
        Guard guard = new Guard();
        Field access_allow = Guard.class.getDeclaredField("access_allow");
        Unsafe unsafe = UnsafeTest.getUnsafe();
        //直接使用unsafe对字段进行赋值，导致权限被放开，从而执行了相关的代码
        unsafe.putInt(guard, unsafe.objectFieldOffset(access_allow), 42);
        guard.work();
    }

    @Test
    void testLoadClassByUnsafe() {
        Unsafe unsafe = UnsafeTest.getUnsafe();
        final byte[] classContent = loadClassContent("");
//        unsafe.defineClass(null,classContent,0,classContent.length);
    }

    private static byte[] loadClassContent(String classFilePath) {
        File file = new File(classFilePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            final byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
