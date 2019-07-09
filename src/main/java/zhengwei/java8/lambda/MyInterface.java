package zhengwei.java8.lambda;

/**
 * 因为Object是所有类的父类，那么接口也是继承自Object类
 * 再函数式接口中声明与Object重名的方法，会被编译器认为是重写Object中的方法
 * 而除了Object中的方法的其他方法，将会被认为是子类自己要是实现的抽象方法
 * @author zhengwei AKA Sherlock
 * @since 2019/7/8 12:35
 */
@FunctionalInterface
public interface MyInterface {
	void test();
	//如果接口中复写了Object类中的方法，那么是不会导致抽象方法的个数加一的
//	String myString();
	String toString();
}
