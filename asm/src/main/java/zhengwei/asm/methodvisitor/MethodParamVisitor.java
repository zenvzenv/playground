package zhengwei.asm.methodvisitor;

import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 使用 Type 类来进行进入和退出方法添加代码的操作
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/20 19:49
 */
public class MethodParamVisitor extends ClassVisitor {
    public MethodParamVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (null != mv && !"<inti>".equals(name)) {
            // 判断方法是不是抽象方法
            final boolean isAbstract = (access & ACC_ABSTRACT) == ACC_ABSTRACT;
            // 判断方法是不是本地方法
            final boolean isNative = (access & ACC_NATIVE) == ACC_NATIVE;
            if (!isAbstract && !isNative) {
                mv = new MethodParamAdapter(api, mv, access, name, desc);
            }
        }
        return mv;
    }

    private static final class MethodParamAdapter extends MethodVisitor {
        private final int methodAccess;
        private final String methodName;
        private final String methodDesc;

        public MethodParamAdapter(int api, MethodVisitor mv, int methodAccess, String methodName, String methodDesc) {
            super(api, mv);
            this.methodAccess = methodAccess;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        @Override
        public void visitCode() {
            // 是否是静态方法
            final boolean isStatic = (methodAccess & ACC_STATIC) != 0;
            // 如果是静态方法，operand stack 的第一位存放的是参数，否则存放的是 this
            int slotIndex = isStatic ? 0 : 1;
            printMessage("message enter:" + methodName + methodDesc);
            // 得到方法描述符
            final Type methodType = Type.getMethodType(methodDesc);
            // 得到方法接收的参数类型
            final Type[] argumentTypes = methodType.getArgumentTypes();
            for (Type argumentType : argumentTypes) {
                // 参数类型
                final int sort = argumentType.getSort();
                // 参数所占的 slot 的大小，4个字节占用1个 slot，8个字节占用2个 slot
                final int size = argumentType.getSize();
                // 参数描述
                final String descriptor = argumentType.getDescriptor();
                // 得到合适的加载此参数的指令
                final int opcode = argumentType.getOpcode(ILOAD);
                // 加载到 operand stack 中去
                super.visitVarInsn(opcode, slotIndex);
                if (sort == Type.BOOLEAN) {
                    printBoolean();
                } else if (sort == Type.CHAR) {
                    printChar();
                } else if (sort == Type.BYTE || sort == Type.SHORT || sort == Type.INT) {
                    printInt();
                } else if (sort == Type.FLOAT) {
                    printFloat();
                } else if (sort == Type.LONG) {
                    printLong();
                } else if (sort == Type.DOUBLE) {
                    printDouble();
                } else if (sort == Type.OBJECT && "Ljava/lang/String;".equals(descriptor)) {
                    printString();
                } else if (sort == Type.OBJECT) {
                    printObject();
                } else {
                    printMessage("No Support");
                }
                slotIndex += size;
            }
            super.visitCode();
        }

        // 方法退出时需要进行的操作
        @Override
        public void visitInsn(int opcode) {
            //方法正常退出和异常退出
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                printMessage("method exit:");
                if (opcode == IRETURN) {
                    super.visitInsn(DUP);
                    printInt();
                } else if (opcode == FRETURN) {
                    super.visitInsn(DUP);
                    printFloat();
                } else if (opcode == LRETURN) {
                    super.visitInsn(DUP2);
                    printLong();
                } else if (opcode == DRETURN) {
                    super.visitInsn(DUP2);
                    printDouble();
                } else if (opcode == ARETURN) {
                    super.visitInsn(DUP);
                    printObject();
                } else if (opcode == RETURN) {
                    printMessage("    return void");
                } else {
                    printMessage("    abnormal return");
                }
            }
            super.visitInsn(opcode);
        }

        private void printBoolean() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
        }

        private void printChar() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(C)V", false);
        }

        private void printInt() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        }

        private void printFloat() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
        }

        private void printLong() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(DUP_X2);
            super.visitInsn(POP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
        }

        private void printDouble() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(DUP_X2);
            super.visitInsn(POP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(D)V", false);
        }

        private void printString() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }

        private void printObject() {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
        }

        private void printMessage(String str) {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitLdcInsn(str);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
    }
}
