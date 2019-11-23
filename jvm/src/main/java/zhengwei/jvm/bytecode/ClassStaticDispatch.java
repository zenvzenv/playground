package zhengwei.jvm.bytecode;

/**
 * 方法的静态分派
 * Grandpa g1=new Father();
 * 以上这行代码中，Grandpa是g1的静态类型，而g1的实际类型是(真正指向的类型)是Father
 * 我们可以得到这样一个结论：变量的静态类型是不会发生改变的，而变量的实际类型是可以发生改变的(即多态的体现)，实际类型在运行期才能确定
 * @author zhengwei AKA Sherlock
 * @since 2019/6/29 7:57
 */
public class ClassStaticDispatch {
	/*
	方法的重载对于JVM来说是一种静态的行为，在编译期就可以完全确定的。
	方法的重写对于JVM来说是一种动态的行为
	当调用重载方法的时候，JVM会根据参数的静态类型去寻找要执行的方法。而不是根据参数的实际类型去寻找要执行的方法。
	 */
	public void test(Grandpa grandpa){
		System.out.println("grandpa");
	}
	public void test(Father father){
		System.out.println("father");
	}
	public void test(Son son){
		System.out.println("son");
	}

	public static void main(String[] args) {
		Grandpa g1=new Father();
		Grandpa g2=new Son();
		Father f1=new Son();
		ClassStaticDispatch classStaticDispatch=new ClassStaticDispatch();
		/*
		JVM会根根据g1和g2的静态类型去TestByteCode5中的重载方法中去寻找要执行的方法
		由于g1和g2的静态类型是Grandpa，那么去TestByteCode5中寻找参数类型为Grandpa的方法
		 */
		classStaticDispatch.test(g1);
		classStaticDispatch.test(g2);
		classStaticDispatch.test(f1);
	}
}
class Grandpa {

}
class Father extends Grandpa {

}
class Son extends Father {

}