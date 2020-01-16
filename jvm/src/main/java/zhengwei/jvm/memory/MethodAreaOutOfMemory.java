package zhengwei.jvm.memory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * 从jdk1.8开始，废除了永久代，取而代之的是元空间，使用的是OS的本地内存，可以不连续。
 * 初始内存大小为21M，如果空间被占满，会进行GC，如果GC之后空间还是不够，则进行空间扩展，最大可达物理内存最大值。
 * 不过方法区出现OOM极其困难。
 * </p>
 * 方法区保存着Class的元信息。静态编译的类和运行时产生的类的元信息都会存放到元空间
 * </p>
 * -XX:MaxMetaspaceSize=10M
 *
 * @author zhengwei AKA Awei
 * @since 2020/1/16 19:51
 */
public class MethodAreaOutOfMemory {
    public static void main(String[] args) {
        while (true) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(MethodAreaOutOfMemory.class);
            enhancer.setUseCache(false);
            enhancer.setCallback((MethodInterceptor) (obj, method, arg1, proxy) -> proxy.invokeSuper(obj, arg1));
            System.out.println("hello world");
            enhancer.create();
        }
    }
}
