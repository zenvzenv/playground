package zhengwei.jvm.classloader;

/**
 * 测试一个类是不是被加载就一定会被初始化
 * 一个类被加载了不一定会导致其初始化
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/20 7:26
 */
public class TestClassInit {
	public static void main(String[] args) {
		/*
		使用一个类的常量，不会导致类被加载
		 */
//		System.out.println(A.a);
		/*
		使用类变量(getstatic)会导致类的加载和初始化
		 */
//		System.out.println(A.b);
		/*
		[Loaded zhengwei.jvm.classloader.A from file:/C:/Users/zhengwei/Desktop/asiainfo_work/playground/target/classes/]
		数组是由JVM动态创建的，对应的类是[Lzhengwei.jvm.classloader.A，所以初始化的是[Lzhengwei.jvm.classloader.A类
		而不是初始化类zhengwei.jvm.classloader.A
		 */
		A[] as=new A[1];
		/*
		[Loaded zhengwei.jvm.classloader.A from file:/C:/Users/zhengwei/Desktop/asiainfo_work/playground/target/classes/]
		[Loaded zhengwei.jvm.classloader.B from file:/C:/Users/zhengwei/Desktop/asiainfo_work/playground/target/classes/]
		class A init...
		类B虽然被加载了，但是并没有去初始化类B
		所以一个类即使被类加载加载了，也不一定回去初始化它，触发类的初始化过程在JVM规范中有严格的定义，详情见JVM.md笔记
		只有是JVM规定的几种情况下去对类的首次使用，才会去初始化一个类
		 */
		System.out.println(B.b);
	}
}

class A {
	static {
		System.out.println("class A init...");
	}

	public static final int a = 1;
	public static int b = 1;
}
class B extends A{
	public static int c=3;
	static {
		System.out.println("class B init...");
	}
}