package zhengwei.jvm.bytecode;

/**
 * 类的动态分派
 * 一个重要的概念：方法接收者
 * invokevritual字节码指令的多态查找流程：
 *  1.找到操作数栈顶的第一个元素，它所指向的实际的类型，
 *  2.如果在实际类型中找到了与常量池中相同的全限定名和相同的方法名称
 *  3.如果有相同的方法则直接调用直接类型的方法，没有的话则一层一层的向上寻找，如果都没有则抛出异常
 *
 * 比较方法重载(overload)和方法重写(override)，我们可以知道：方法重载是静态的，是编译期行为；方法重写是动态的，是运行期行为
 * @author zhengwei AKA Sherlock
 * @since 2019/6/29 8:39
 */
public class ClassDynamicDispatch {
	public static void main(String[] args) throws InterruptedException {
		/*
		new关键字的作用：
			1.为对象在堆上开辟一块内存空间；
			2.去执行构造方法；
			3.将执行完构造方法之后，在堆上的地址返回
		 */
		Fruit apple=new Apple();
		Fruit orange=new Orange();
		System.out.println(apple.getClass().getClassLoader());
		System.out.println(orange.getClass().getClassLoader());
		apple.test();
		orange.test();

		apple=new Orange();
		Thread.sleep(1000*5L);
		apple.test();
	}
}
class Fruit{
	public void test(){
		System.out.println("fruit");
	}
}
class Apple extends Fruit{
	@Override
	public void test() {
		System.out.println("apple");
	}
}
class Orange extends Fruit{
	@Override
	public void test() {
		System.out.println("orange");
	}
}