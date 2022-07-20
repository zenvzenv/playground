package zhengwei.asm.methodvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

/**
 * 在方法开始和结束的地方插入代码
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/20 19:38
 */
public class MethodAroundVisitor extends ClassVisitor {
    public MethodAroundVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        // 如果不为空并且不是构造函数
        if (null != mv && !"<init>".equals(name)) {
            // 判断方法是不是抽象方法
            final boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
            // 判断方法是不是本地方法
            final boolean isNative = (access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE;
            if (!isAbstract && !isNative) {
                mv = new MethodAroundAdapter(api, mv);
            }
        }
        return mv;
    }

    private static class MethodAroundAdapter extends MethodVisitor {
        public MethodAroundAdapter(int api, MethodVisitor mv) {
            super(api, mv);
        }

        // 进入方法时的梳理逻辑
        @Override
        public void visitCode() {
            // 首先，处理自己的代码逻辑
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitVarInsn(ALOAD, 1);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitVarInsn(ILOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitVarInsn(LLOAD, 3);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitVarInsn(ALOAD, 5);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);

            //其次巧用父类构造器
            super.visitCode();
        }

        // 方法退出时处理逻辑
        @Override
        public void visitInsn(int opcode) {
            // 首先，处理自己的代码逻辑
            // 非异常退出程序
            if (opcode == Opcodes.ATHROW || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                super.visitVarInsn(ILOAD, 6);
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            }

            super.visitInsn(opcode);
        }
    }
}
