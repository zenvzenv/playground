package zhengwei.asm.methodvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 移除 NOP 指令
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/27 20:56
 */
public class MethodRemoveNopVisitor extends ClassVisitor {
    public MethodRemoveNopVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null && !"<init>".equals(name) && !"<clinit>".equals(name)) {
            boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod && !isNativeMethod) {
                mv = new MethodRemoveNopAdapter(api, mv);
            }

        }
        return mv;
    }

    private static final class MethodRemoveNopAdapter extends MethodVisitor {
        public MethodRemoveNopAdapter(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            // 如果不是 NOP 指令则继续向后传递指令否则将 NOP 拦截
            if (opcode != NOP) {
                super.visitInsn(opcode);
            }
        }
    }
}
