package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassReader;
import zhengwei.asm.utils.FileUtils;

import java.util.Arrays;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/10 13:35
 */
public class HelloWorldRun01 {
    public static void main(String[] args) {
        final String relativePath = "zhengwei/asm/classreader/HelloWorld01.class";
        String filePath = FileUtils.getFilePath(relativePath);
        final byte[] bytes = FileUtils.readBytes(filePath);

        final ClassReader cr = new ClassReader(bytes);

        // 获取访问标识权限
        final int access = cr.getAccess();
        System.out.println("access: " + access);

        // 获取类的全限定名
        final String className = cr.getClassName();
        System.out.println("class name: " + className);

        // 获取父类
        final String superName = cr.getSuperName();
        System.out.printf("super name: %s\n", superName);

        // 获取实现的接口
        final String[] interfaces = cr.getInterfaces();
        System.out.printf("interfaces: %s\n", Arrays.toString(interfaces));
    }
}
