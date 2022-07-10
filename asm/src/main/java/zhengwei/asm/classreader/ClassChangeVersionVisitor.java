package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

/**
 * 修改类的版本信息
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/10 15:20
 */
public class ClassChangeVersionVisitor extends ClassVisitor {

    public ClassChangeVersionVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    /**
     * 修改版本为 Java7
     *
     * @param version    Java 版本
     * @param access     访问控制
     * @param name       类名
     * @param signature  泛型
     * @param superName  父类名
     * @param interfaces 实现的接口
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(Opcodes.V1_7, access, name, signature, superName, interfaces);
    }
}
