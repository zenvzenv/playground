package zhengwei.asm.methodvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/27 21:02
 */
public class MethodEmptyBodyVisitor extends ClassVisitor {
    private String owner;
    // 需要清空方法体的方法名
    private final String methodName;
    // 需要清空方法体的方法的描述符
    private final String methodDesc;

    public MethodEmptyBodyVisitor(int api, ClassVisitor cv, String methodName, String methodDesc) {
        super(api, cv);
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 获取当前类名
        this.owner = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        // 查找指定的方法，如果找到那么就先把方法删除，再生成一个同名同返回值方法，新方法中无任何逻辑
        if (null != mv && methodName.equals(name) && methodDesc.equals(desc)) {
            final boolean isAbstract = (access & ACC_ABSTRACT) != 0;
            final boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstract && !isNativeMethod) {
                return null;
            }
        }
        return mv;
    }

    protected void genNewBody(MethodVisitor mv, String owner, int methodAccess, String methodName, String methodDesc) {
        // (1)
        final Type type = Type.getType(methodDesc);
        // 获取方法的参数类型列表
        final Type[] argumentTypes = type.getArgumentTypes();
        // 获取方法的返回类型
        final Type returnType = type.getReturnType();

        // (2)
        final boolean isStaticMethod = (methodAccess & ACC_STATIC) != 0;
        // 如果是静态方法 operand stack 第一位则不是 this，否则第一位是 this，占用一个位置
        int localSize = isStaticMethod ? 0 : 1;
        for (Type argType : argumentTypes) {
            localSize += argType.getSize();
        }
        final int stackSize = returnType.getSize();

        // (3) method body
        mv.visitCode();
        if (returnType.getSort() == Type.VOID) {
            mv.visitInsn(RETURN);
        }
        else if (returnType.getSort() >= Type.BOOLEAN && returnType.getSort() <= Type.DOUBLE) {
            mv.visitInsn(returnType.getOpcode(ICONST_1));
            mv.visitInsn(returnType.getOpcode(IRETURN));
        }
        else {
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(stackSize, localSize);
        mv.visitEnd();
    }
}
