package zhengwei.asm.classwriter;

import jdk.internal.org.objectweb.asm.ClassWriter;
import zhengwei.asm.utils.FileUtils;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/6/29
 */
public class HelloWorldInterfacedGen {
    public static void main(String[] args) {
        final String relativePath = "zhengwei/asm/sample/HelloWorld.class";
        final String filePath = FileUtils.getFilePath(relativePath);
        final byte[] dump = dump();
        FileUtils.writeBytes(filePath, dump);
    }

    private static byte[] dump() {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                V1_8,                                           //version
                ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,  //access
                "zhengwei/asm/sample/HelloWorld",            //name
                null,                                       //signature
                "java/lang/Object",                         //superName
                null                                    //interfaces
        );

        cw.visitEnd();
        return cw.toByteArray();
    }
}
