package zhengwei.jvm.bytecode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 从字节码角度理解动态代理
 * 动态代理只会代理Object中的toString,equals和hashCode方法，Object中其余的方法将不会被代理
 * @author zhengwei AKA Sherlock
 * @since 2019/6/29 19:50
 */
public class DynamicProxy {
	public static void main(String[] args) {
		//设置系统属性sun.misc.ProxyGenerator.saveGeneratedFiles为true，Java会把生成的代理类class写入到本地磁盘中
		System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles","true");
		ReadSubject readSubject=new ReadSubject();
		//3.而InvocationHandler实例实际指向的是我们编写的动态代理的类，被代理的类被构造方法传入进去
		InvocationHandler invocationHandler=new DynamicSubject(readSubject);
		Class<?> clazz=readSubject.getClass();
		//实际所指向的类是zhengwei.jvm.bytecode.$Proxy0
		//zhengwei.jvm.bytecode.$Proxy0是Proxy的子类
		Subject subject= (Subject) Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),invocationHandler);
		//1.调用request方法，实际调用的是$Proxy0中的request方法
		//2.而$Proxy0中的方法调用的是父类中的InvocationHandler实例中的request方法
		subject.request();
		System.out.println(subject.getClass());
		System.out.println(subject.getClass().getSuperclass());
	}
}
interface Subject{
	void request();
}
class ReadSubject implements Subject{
	@Override
	public void request() {
		System.out.println("from read subject");
	}
}
class DynamicSubject implements InvocationHandler {
	private Object subject;//需要代理的对象

	public DynamicSubject(Object subject) {
		this.subject = subject;
	}
	//4.在$Proxy0中的request的方法中，会传入三个参数super.h.invoke(this, m3, (Object[])null); 分别对应request所需要的三个参数
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("before calling:"+method);
		//5.这里面的method对象其实是那个被代理的接口的方法：public abstract void zhengwei.jvm.bytecode.Subject.request()
		//6.但是method中的invoke的方法中的第一个参数是要指定调用哪个对象的方法，第二个参数是这个方法所需要的参数
		//7.最后通过反射，传入要实际调用的对象即通过构造方法传进来的实际实现了接口方法的对象，实现方法的调用
		method.invoke(subject,args);
		System.out.println("after calling:"+method);
		return null;
	}
}