package zhengwei.jvm.classloader;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ServiceLoader;

/**
 * 当前类加载器(Current ClassLoader)
 * 每个类都会使用自己的类加载器(即加载自身的类加载器)来去加载其他类(指这个类所依赖的类)
 * 如果CLassX引用了ClassY，那么ClassX的类加载器就会去加载ClassY(前提是ClassY尚未被加载)
 *
 * 线程上下文类加载器(Context ClassLoader)
 * 线程上线文类加载器由JDK1.2开始提供，类Thread中提供getContextClassLoader()和setContextClassLoader(ClassLoader cl)方法
 * 来获取和是指当前线程的上下文类加载器
 * 如果没有通过setContextClassLoader(ClassLoader cl)进行设置的话，线程将继承其父线程的上下文类加载器。
 * Java应用运行时的初始线程的上下文类加载器是系统类加载器(应用类加载器).在线程中运行的代码可以通过该类加载器来加载类和资源。
 *
 * this.loader = Launcher.AppClassLoader.getAppClassLoader(var1);//应用类加载器
 * Launcher.java中进行设置Thread.currentThread().setContextClassLoader(this.loader);
 *
 * 线程上下文的重要性：
 * SPI(Service Provider Interface)：标准由Java来提供接口，具体实现由各个厂商来实现
 *
 * 父ClassLoader可以使用当前线程Thread.currentThread().getContextClassLoader()所指定的ClassLoader加载的类。
 * 这就改变了父类加载器不能使用子类加载器或是其他没有直接父子关系的类加载器加载的类的情况，即改变了双亲委托模型。
 *
 * 线程上下文类加载器就是当前线程的Current ClassLoader
 *
 * 在双亲委托某型下，类加载是由上到下的，即下层的类加载器会委托上层的类加载器进行加载。但是对于SPI来说，有些接口是由Java核心库提供的，
 * 而Java核心库是由启动类加载器去加载的，而这些接口的实现来源于不同jar包(是由不同的厂商来实现的)，Java的启动类加载器是不会去加载其他
 * 来源的jar包的，这样传统的双亲委托机制就无法满足SPI的要求。而通过给当前线程设置上下文类加载器，就可以由设置的类加载器来实现对于接口
 * 实现类的加载。
 *
 * 线程上下文类加载器的一般使用方式(获取 - 使用 - 还原)
 * ClassLoader cl=Thread .currentThread().getContextClassLoader()
 * try {
 *     Thread.currentThread().setContextClassLoader(targetTccl);
 *     myMethod();//调用Thread.currentThread().getContextClassLoader()获取当前线程的上下文类加载器做某些事
 * } finally {
 *     Thread.currentThread.setContextClassLoader(cl);
 * }
 * 如果一个类由类加载器A加载，那么这个类的依赖类也是由相同的类加载器加载的，(如果这个依赖类没被加载的话)
 * ContextClassLoader就是为了破坏双亲委托模型。
 * 当高层提供了统一的接口让底层去实现，同时又要在高层去加载(或实例化)底层的类时，那必须通过线程上下文加载器来帮助高层的ClassLoader找到
 * 并加载类。
 * @author zhengwei AKA Sherlock
 * @since 2019/6/15 8:48
 */
public class ThreadContextClassLoader implements Runnable{
	private Thread thread;
	public ThreadContextClassLoader(){
		this.thread=new Thread(this);
		thread.start();
	}
	@Override
	public void run() {
		ClassLoader contextClassLoader = this.thread.getContextClassLoader();
		this.thread.setContextClassLoader(contextClassLoader);
		System.out.println("class:" + contextClassLoader.getClass());
		System.out.println("parent:"+ contextClassLoader.getParent().getClass());
	}
	@Test
	void test1(){
		System.out.println(Thread.currentThread().getContextClassLoader());
		System.out.println(Thread.class.getClassLoader());
	}
	@Test
	void threadContextClassLoader(){
		new ThreadContextClassLoader();
	}

	/**
	 * 由于ServiceLoader类是由启动类加载器去加载的，所以启动类加载器回去加载ServiceLoader所依赖的所有类
	 * 但是由于SPI的所有的厂商的jar都在classpath下，所以启动类加载器是加载不到实现了上层接口的厂商实现类的
	 * 由于线程上下文类加载器的存在，使得加载这些实现类成为可能
	 * 线程上下文类加载器默认是应用类加载器
	 * ServiceLoader的load(Class c)方法中会获取当前线程的上下文类加载器，然后会用这个线程的上线文类加载器去加载相关的类
	 * 如果设置了当前类上下文类加载器为ExtClassLoader，那么将不会加载到Mysql的驱动类。
	 */
	@Test
	void testServiceLoader(){
//		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader().getParent());
		ServiceLoader<Driver> loader=ServiceLoader.load(Driver.class);
		for (Driver driver : loader) {
			System.out.println("driver:" + driver.getClass() + ",loader:" + driver.getClass().getClassLoader());
		}
		System.out.println("current thread class loader :"+Thread.currentThread().getContextClassLoader());
		System.out.println("ServiceLoader class loader :"+ServiceLoader.class.getClassLoader());
	}

	/**
	 * 根据jdbc的实现来看线程上下文类加载器的作用
	 * @throws Exception 异常
	 */
	@Test
	void testJDBC() throws Exception {
		/*
		起始在JDK1.6版本以后我们就可以不用再写这行代码了。
		由于ServiceLoader的存在，ServiceLoader会去当前线程上下文类加载器去加载类
		这个线程上下文类加载器一般是应用类加载器，应用类加载器会去classpath下寻找相应的类并加载
		在MySQL的jar包中，有这样一个文件夹/META-INF/services，这里面存放一个文件
		这个文件名字就是需要实现的接口，这个文件里面的内容是实现了这个接口的实现类的二进制名字
		这样ServiceLoader就可以自己寻找到需要加载的类了，然后去加载。
		DriverManager.isDriverAllowed(Driver driver, ClassLoader classLoader)->result = ( aClass == driver.getClass() ) ? true : false;
		判断是否时同一个类加载器加载。
		 */
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("xxx", "username", "password");
	}
}
