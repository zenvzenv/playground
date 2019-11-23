package zhengwei.jvm.classloader;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/6/10 18:17
 */
public class MySample {
	private MySample mySample;

	public void setMySample(Object mySample) {
		this.mySample = (MySample) mySample;
	}

	public MySample(){
		System.out.println("MySample is loaded by :"+this.getClass().getClassLoader());
		//注意：加载了MySample的类加载器会继续去加载MyCat，也会遵循双亲委托机制
		new MyCat();
		/*
		如果删除MySample.class文件，那么MySample的class对象将会由我们自定义的类加载器MyClassLoader加载
		因为MySample的构造方法中实例化了MyCat，那么JVM就会去加载MyCat类，因为加载MyClassLoader类的是MyClassLoader
		所以MyClassLoader将会尝试去加载MySample，因为有双亲委托机制的存在，那么MyClassLoader会委托给它的父加载器
		因为MyCat在classpath中，那么AppClassLoader是能够加载到MyCat类的
		AppClassLoader加载完MyCat之后，那么这个类对于AppClassLoader的子加载器都是可见的
		所以执行下面的这行代码是不会报错的
		 */
		System.out.println("from MySample :"+MyCat.class);
	}
}
