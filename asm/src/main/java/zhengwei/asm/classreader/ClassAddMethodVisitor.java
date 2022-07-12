package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/12
 */
public class ClassAddMethodVisitor extends ClassVisitor {
    private final int methodAccess;
    private final String methodName;
    private final String methodDesc;
    private final String methodSignature;
    private final String[] methodExceptions;
    private boolean isMethodPresent;

    public ClassAddMethodVisitor(int i, ClassVisitor classVisitor, int methodAccess, String methodName, String methodDesc, String methodSignature, String[] methodExceptions) {
        super(i, classVisitor);
        this.methodAccess = methodAccess;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.methodSignature = methodSignature;
        this.methodExceptions = methodExceptions;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(methodName) && desc.equals(methodDesc)) {
            isMethodPresent = true;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        if (!isMethodPresent) {
            final MethodVisitor mv = super.visitMethod(methodAccess, methodName, methodDesc, methodSignature, methodExceptions);
            if (null != mv) {
                // 建议使用 ASMPrint 工具类生成代码
                mv.visitCode();
                mv.visitVarInsn(ILOAD, 1);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(IMUL);
                mv.visitInsn(IRETURN);
                // 使用 ClassWriter.COMPUTE_FRAMES 之后需要显示调用，但里面的值无所谓，asm 会自动计算
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
        }
        super.visitEnd();
    }
}
