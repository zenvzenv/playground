package zhengwei.jvm.bytecode;

/**
 * 小知识点：
 *  可重入锁：如果一个类中有两个同步方法，方法A和方法B，如果一个线程获取到了方法A的锁，如果方法A中同时引用了方法B的话，
 *      那么该线程也能够获取到方法B的锁，此时synchronized的计数器就变成了2.
 *  自旋锁：一个线程等待一个锁时，自己在那空转，即执行一段无意义的函数，执行完后再去获取锁，如果获取到了，那么就执行同步方法，
 *      如果没获取到那么再继续自旋。
 *
 * 一个类如果没有构造方法的话，那么编译器会为我们自动生成一个构造方法。
 * 类的非静态成员变量的初始化赋值是在构造方法中完成赋值的。
 * 如果我们自己定义了类的构造方法的话，那么类的非静态成员变量的赋值还是在构造方法中。
 * 不论有几个构造方法，所有的构造方法内的类得非静态成员变量的初始化赋值都是一样的，
 * 一个类中的静态成员变量和静态代码块在编译之后都会放置到一个<clinit>初始化方法中。
 * @author zhengwei AKA Sherlock
 * @since 2019/6/22 11:01
 */
public class TestByteCode2 {
	String str = "welcome";
	private int x = 5;
	public static Integer integer = 10;
	private final Object object = new Object();

	public static void main(String[] args) {
		TestByteCode2 testByteCode2 = new TestByteCode2();
		testByteCode2.setX(10);
		integer=20;
	}
	//构造方法一
	public TestByteCode2(){

	}
	//构造方法二
	public TestByteCode2(int x){
		this.x=x;
	}

	/**
	 * synchronized如果修饰的是实例方法的话，那么锁定的是这个class所对应的那个实例对象，也就是this上锁。
	 * synchronized申明在方法上的话，那么该方法体的字节码里面是体现不出monitorenter和monitorexit的。
	 */
	private synchronized void setX(int x) {
		this.x = x;
	}

	/**
	 * synchronized也可以自己指定要上锁的对象，这个对象可以是任意对象。
	 * 只有在synchronized修饰在方法体内部时，此时该方法体的字节码里面是能够体现monitorenter和monitorexit的。
	 */
	private void test(){
		synchronized (object){
			System.out.println("hello synchronized");
		}
	}

	/**
	 * synchronized如果修饰的是静态方法的话，那么锁的是对应的class对象，而不是实例对象this
	 */
	private synchronized static void test2(){
		System.out.println("do nothing");
	}
}
