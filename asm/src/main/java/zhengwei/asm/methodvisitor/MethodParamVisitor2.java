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
public class MethodParamVisitor2 extends ClassVisitor {
    public MethodParamVisitor2(int api, ClassVisitor cv) {
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
                mv = new MethodParamAdapter2(api, mv, access, name, desc);
            }
        }
        return mv;
    }

    private static final class MethodParamAdapter2 extends MethodVisitor {
        private final int methodAccess;
        private final String methodName;
        private final String methodDesc;

        public MethodParamAdapter2(int api, MethodVisitor mv, int methodAccess, String methodName, String methodDesc) {
            super(api, mv);
            this.methodAccess = methodAccess;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        @Override
        public void visitCode() {
            // 首先，处理自己的代码逻辑
            boolean isStatic = ((methodAccess & ACC_STATIC) != 0);
            int slotIndex = isStatic ? 0 : 1;

            printMessage("Method Enter: " + methodName + methodDesc);

            Type methodType = Type.getMethodType(methodDesc);
            Type[] argumentTypes = methodType.getArgumentTypes();
            for (Type t : argumentTypes) {
                int sort = t.getSort();
                int size = t.getSize();
                String descriptor = t.getDescriptor();
                int opcode = t.getOpcode(ILOAD);
                super.visitVarInsn(opcode, slotIndex);
                if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                    String methodDesc = String.format("(%s)V", descriptor);
                    printValueOnStack(methodDesc);
                }
                else {
                    printValueOnStack("(Ljava/lang/Object;)V");
                }

                slotIndex += size;
            }

            // 其次，调用父类的方法实现
            super.visitCode();
        }

        // 方法退出时需要进行的操作
        @Override
        public void visitInsn(int opcode) {
            // 首先，处理自己的代码逻辑
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                printMessage("Method Exit: " + methodName + methodDesc);
                if (opcode >= IRETURN && opcode <= DRETURN) {
                    Type methodType = Type.getMethodType(methodDesc);
                    Type returnType = methodType.getReturnType();
                    int size = returnType.getSize();
                    String descriptor = returnType.getDescriptor();

                    if (size == 1) {
                        super.visitInsn(DUP);
                    }
                    else {
                        super.visitInsn(DUP2);
                    }
                    String methodDesc = String.format("(%s)V", descriptor);
                    printValueOnStack(methodDesc);
                }
                else if (opcode == ARETURN) {
                    super.visitInsn(DUP);
                    printValueOnStack("(Ljava/lang/Object;)V");
                }
                else if (opcode == RETURN) {
                    printMessage("    return void");
                }
                else {
                    printMessage("    abnormal return");
                }
            }

            // 其次，调用父类的方法实现
            super.visitInsn(opcode);
        }

        private void printMessage(String str) {
            super.visitLdcInsn(str);
            super.visitMethodInsn(INVOKESTATIC, "sample/ParameterUtils", "printText", "(Ljava/lang/String;)V", false);
        }

        private void printValueOnStack(String descriptor) {
            super.visitMethodInsn(INVOKESTATIC, "sample/ParameterUtils", "printValueOnStack", descriptor, false);
        }
    }
}
