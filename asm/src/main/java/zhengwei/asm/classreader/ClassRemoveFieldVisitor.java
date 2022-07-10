package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;

/**
 * 删除类中的一个字段
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/10 16:29
 */
public class ClassRemoveFieldVisitor extends ClassVisitor {
    private final String fieldName;
    private final String fieldDesc;

    public ClassRemoveFieldVisitor(int i, ClassVisitor classVisitor, String fieldName, String fieldDesc) {
        super(i, classVisitor);
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
    }

    /**
     * 删除字段
     *
     * @param access    访问控制
     * @param name      字段名
     * @param desc      字段类型
     * @param signature 泛型
     * @param value     字段值
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // 返回 null 代表删除字段
        if (name.equals(fieldName) && desc.equals(fieldDesc)) {
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }
}
