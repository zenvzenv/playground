package zhengwei.asm.classreader;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/10 13:34
 */
public class HelloWorld01 {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        final HelloWorld01 o = new HelloWorld01();
        final Object clone = o.clone();
        System.out.println(clone);
    }
}
