package zhengwei.asm.classreader;

import jdk.internal.org.objectweb.asm.ClassVisitor;

/**
 * 为类添加一个实现接口，实现的接口放到数组中即可
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/10 15:29
 */
public class ClassCloneVisitor extends ClassVisitor {
    public ClassCloneVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

    @Override
    public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {
        super.visit(i, i1, s, s1, s2, new String[]{"java/lang/Cloneable"});
    }
}
