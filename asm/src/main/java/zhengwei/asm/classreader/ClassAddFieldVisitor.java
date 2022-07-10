package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/10 16:36
 */
public class ClassAddFieldVisitor extends ClassVisitor {
    private final int fieldAccess;
    private final String fieldName;
    private final String fieldDesc;
    private boolean isFieldPresent;

    public ClassAddFieldVisitor(int i, ClassVisitor classVisitor, int fieldAccess, String fieldName, String fieldDesc) {
        super(i, classVisitor);
        this.fieldAccess = fieldAccess;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (name.equals(fieldName)) {
            isFieldPresent = true;
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            final FieldVisitor fv = cv.visitField(fieldAccess, fieldName, fieldDesc, null, null);
            if (null != fv) {
                fv.visitEnd();
            }
        }
        super.visitEnd();
    }
}
