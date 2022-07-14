package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

/**
 * 在方法开始处添加代码
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/14 21:27
 */
public class ModifyMethodEnterClass extends ClassVisitor {
    public ModifyMethodEnterClass(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (null != mv && !"<init>".equals(name)) {
            mv = new MethodEnterAdapter(api, mv);
        }
        return mv;
    }

    private static final class MethodEnterAdapter extends MethodVisitor {
        public MethodEnterAdapter(int i, MethodVisitor methodVisitor) {
            super(i, methodVisitor);
        }

        /**
         * 在方法开始的地方添加代码，即在 visitCode 处进行添加，visitCode 代表方法开始
         */
        @Override
        public void visitCode() {
            // 首先，处理自己的代码逻辑
            super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitLdcInsn("Method Enter...");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            super.visitCode();
        }
    }
}
