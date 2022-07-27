package zhengwei.asm.methodvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 统计方法的执行时间
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/21 20:39
 */
public class MethodTimerVisitor extends ClassVisitor {
    private String owner;
    private boolean isInterface;

    public MethodTimerVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 记录类名
        owner = name;
        // 判断是不是接口
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (!isInterface && null != mv && !"<inti>".equals(name) && !"<clinit>".equals(name)) {
            final boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            final boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod && !isNativeMethod) {
                mv = new MethodTimerAdapter(api, mv);
            }
        }
        return mv;
    }

    /**
     * 添加静态成员变量 timer 来记录时间
     */
    @Override
    public void visitEnd() {
        if (!isInterface) {
            final FieldVisitor fv = super.visitField(ACC_PUBLIC | ACC_STATIC, "timer", "J", null, null);
            if (null != fv) {
                fv.visitEnd();
            }
        }
        super.visitEnd();
    }

    private final class MethodTimerAdapter extends MethodVisitor {
        public MethodTimerAdapter(int api, MethodVisitor mv) {
            super(api, mv);
        }

        /**
         * 方法进入时 timer -= System.currentTimeMillis()
         */
        @Override
        public void visitCode() {
            // 首先，处理自己的代码逻辑
            super.visitFieldInsn(GETSTATIC, owner, "timer", "J");
            super.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            super.visitInsn(LSUB);
            super.visitFieldInsn(PUTSTATIC, owner, "timer", "J");

            super.visitCode();
        }

        /**
         * 退出方法时 timer += System.currentTimeMillis()
         */
        @Override
        public void visitInsn(int opcode) {
            // 首先，处理自己的代码逻辑
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                super.visitFieldInsn(GETSTATIC, owner, "timer", "J");
                super.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                super.visitInsn(LADD);
                super.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
            }

            super.visitInsn(opcode);
        }
    }
}
