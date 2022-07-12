package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/12
 */
public class ClassRemoveMethodVisitor extends ClassVisitor {
    private final String methodName;
    private final String methodDesc;

    public ClassRemoveMethodVisitor(int i, ClassVisitor classVisitor, String methodName, String methodDesc) {
        super(i, classVisitor);
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    /**
     * 删除方法
     *
     * @param access     访问控制
     * @param name       方法名
     * @param desc       方法描述符
     * @param signature  泛型
     * @param exceptions 异常
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(methodName) && desc.equals(methodDesc)) {
            return null;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
