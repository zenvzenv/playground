package zhengwei.asm.classwriter;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import zhengwei.asm.utils.FileUtils;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/6/30
 */
public class HelloWorldGenerateCore {
    public static void main(String[] args) {
        final String relativePath = "zhengwei/asm/sample/HelloWorld.class";
        final String filePath = FileUtils.getFilePath(relativePath);
        final byte[] dump = dump();
        FileUtils.writeBytes(filePath, dump);
    }

    private static byte[] dump() {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                V1_8,
                ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
                "zhengwei/asm/sample/HelloWorld",
                null,
                "java/lang/Object",
                new String[]{"java/lang/Cloneable"}
        );

        {
            final FieldVisitor fv1 = cw.visitField(
                    ACC_PUBLIC | ACC_FINAL | ACC_STATIC,
                    "LESS",
                    "I",
                    null,
                    -1);
            fv1.visitEnd();
        }

        {
            final FieldVisitor fv2 = cw.visitField(
                    ACC_PUBLIC | ACC_FINAL | ACC_STATIC,
                    "EQUAL",
                    "I",
                    null,
                    0
            );
            fv2.visitEnd();
        }

        {
            final FieldVisitor fv3 = cw.visitField(
                    ACC_PUBLIC | ACC_FINAL | ACC_STATIC,
                    "GRATER",
                    "I",
                    null,
                    1
            );
            fv3.visitEnd();
        }

        {
            final MethodVisitor mv1 = cw.visitMethod(
                    ACC_PUBLIC | ACC_ABSTRACT,
                    "compareTo",
                    "(Ljava/lang/Object;)I",
                    null,
                    null
            );
            mv1.visitEnd();
        }

        cw.visitEnd();

        return cw.toByteArray();
    }
}
