package zhengwei.jvm.bytecode;

import java.util.Date;

/**
 * 加深对方法的重载和方法的重写的理解
 * 方法的重载和方法的重写本质上的不同就是调用者的不同
 * 在调用重载的方法的时候，调用者是相同的一个对象(实际类型相同)，所有的重载方法都被定义在这个对象当中
 * 再调用重写方法的时候，调用者是不同的对象(实际类型不同)，类之间存在父子的层级关系，子类中重写父类的方法，在不同的对象调用重写方法的时候，JVM会去寻找实际类型中的重写方法
 *
 * 虚方法表
 *  针对于方法调用动态分派的过程，JVM会在类的方法区建立一个虚方法表的数据结构(virtual method table,简称vtable)，在类加载的链接阶段进行初始化
 *      描述了方法调用的入口地址，如果子类没有去重写父类的方法，那么入口地址将直接指向父类的方法入口
 *      如果子类重写的父类的方法，那么方法在方法表的索引与父类中的方法在方法表中的索引应该是相同的
 *  针对与invokeinferface指令来说，JVM会建立一个叫做接口方法表的数据结构(interface method table,简称itable)
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/6/29 10:29
 */
public class DeepenStaticAndDynamicDispatch {
	public static void main(String[] args) {
		Animal animal=new Animal();
		Animal dog=new Dog();
		Dog dog2=new Dog();
		/*
		动态分派
		以下代码调用的对象不同(实际类型的不同)，JVM会去寻找实际类型中的重写方法。
		 */
		animal.test("hello");
		dog.test("hello");
		/*
		静态分派
		以下代码是调用的Animal中的重载方法
		重载方法是静态过程，在编译期间就会确定调用哪个方法
		所以JVM只会根据参数的静态类型去寻找要执行的方法，而不是根据参数的实际类型去寻找要执行的方法
		即animal和dog的静态类型都是Animal，所以都会去执行public void test(Animal animal)方法
		如果想要执行public void test(Dog dog)方法，需要声明静态类型为Dog的对象。
		 */
		animal.test(dog);
		animal.test(dog2);
		animal.test(animal);
	}
}
class Animal{
	public void test(String str){
		System.out.println("Animal str->"+str);
	}
	public void test(Date date){
		System.out.println("Animal date->"+date);
	}
	public void test(Dog dog){
		System.out.println("Animal type->"+dog);
	}
	public void test(Animal animal){
		System.out.println("Animal 666->"+animal);
	}
}
class Dog extends Animal{
	@Override
	public void test(String str) {
		System.out.println("Dog str->"+str);
	}

	@Override
	public void test(Date date) {
		System.out.println("Dog date->"+date);
	}
}