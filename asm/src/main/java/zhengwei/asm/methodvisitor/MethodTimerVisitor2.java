package zhengwei.asm.methodvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 统计方法的执行时间，统计每个方法的执行时间
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/21 20:39
 */
public class MethodTimerVisitor2 extends ClassVisitor {
    private String owner;
    private boolean isInterface;

    public MethodTimerVisitor2(int api, ClassVisitor cv) {
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
        if (!isInterface && null != mv && !"<init>".equals(name) && !"<clinit>".equals(name)) {
            final boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            final boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod && !isNativeMethod) {
                // 每遇到一个合适的方法都会生成
                final FieldVisitor fv = super.visitField(ACC_PUBLIC | ACC_STATIC, getFieldName(name), "J", null, null);
                if (null != fv) {
                    fv.visitEnd();
                }
                mv = new MethodTimerAdapter2(api, mv, this.owner, name);
            }
        }
        return mv;
    }

    /**
     * 每遇到一个方法都生成一个 timer 变量来记录所消耗的时间
     *
     * @param method 方法名
     * @return timer 计时器
     */
    private String getFieldName(String method) {
        return "timer_" + method;
    }

    private final class MethodTimerAdapter2 extends MethodVisitor {
        private final String owner;
        private final String methodName;

        public MethodTimerAdapter2(int api, MethodVisitor mv, String owner, String methodName) {
            super(api, mv);
            this.owner = owner;
            this.methodName = methodName;
        }

        /**
         * 方法进入时 timer -= System.currentTimeMillis()
         */
        @Override
        public void visitCode() {
            // 首先，处理自己的代码逻辑

            // 注意字段名要对应
            super.visitFieldInsn(GETSTATIC, owner, getFieldName(this.methodName), "J");
            super.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            super.visitInsn(LSUB);
            // 注意字段名要对应
            super.visitFieldInsn(PUTSTATIC, owner, getFieldName(this.methodName), "J");

            super.visitCode();
        }

        /**
         * 退出方法时 timer += System.currentTimeMillis()
         */
        @Override
        public void visitInsn(int opcode) {
            // 首先，处理自己的代码逻辑
            // 检测到退出指令，正常退出或异常退出
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                // 注意字段名要对应
                super.visitFieldInsn(GETSTATIC, owner, getFieldName(this.methodName), "J");
                super.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                super.visitInsn(LADD);
                // 注意字段名要对应
                super.visitFieldInsn(PUTSTATIC, owner, getFieldName(this.methodName), "J");
            }

            super.visitInsn(opcode);
        }
    }
}
