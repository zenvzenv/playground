package zhengwei.jvm.bytecode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 从字节码角度理解动态代理
 * @author zhengwei AKA Sherlock
 * @since 2019/6/29 19:50
 */
public class DynamicProxy {
	public static void main(String[] args) {
		System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles","true");
		ReadSubject readSubject=new ReadSubject();
		InvocationHandler invocationHandler=new DynamicSubject(readSubject);
		Class<?> clazz=readSubject.getClass();
		Subject subject= (Subject) Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),invocationHandler);
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

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("before calling:"+method);
		method.invoke(subject,args);
		System.out.println("after calling:"+method);
		return null;
	}
}