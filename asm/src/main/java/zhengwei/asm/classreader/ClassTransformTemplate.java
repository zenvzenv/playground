package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import zhengwei.asm.utils.FileUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/10 15:24
 */
public class ClassTransformTemplate {
    public static void main(String[] args) {
        // zhengwei/asm/classread/HelloWord01.class
        String relativePath = args[0];
        final String filePath = FileUtils.getFilePath(relativePath);
        final byte[] bytes = FileUtils.readBytes(filePath);

        final ClassReader cr = new ClassReader(bytes);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        int api = Opcodes.ASM5;
        // 在此处实现不同的业务逻辑
        final ClassVisitor cv = new ClassChangeVersionVisitor(api, cw);

        int parsingOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
        cr.accept(cv, parsingOptions);

        final byte[] newClassBytes = cw.toByteArray();

        FileUtils.writeBytes(filePath, newClassBytes);
    }
}
