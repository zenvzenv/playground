package zhengwei.asm.classwriter;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import zhengwei.asm.utils.FileUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/6/30
 */
public class HelloWorldCLassGen {
    public static void main(String[] args) {
        final String relativePath = "zhengwei/asm/sample/HelloWorld.class";
        final String filePath = FileUtils.getFilePath(relativePath);
        final byte[] dump = dump();
        FileUtils.writeBytes(filePath, dump);
    }

    private static byte[] dump() {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                "zhengwei/asm/sample/HelloWorld",
                null,
                "java/lang/Object",
                null
        );

        final MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        return cw.toByteArray();
    }
}
