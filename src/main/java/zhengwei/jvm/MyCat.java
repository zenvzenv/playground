package zhengwei.jvm;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/6/10 18:14
 */
public class MyCat {
	public MyCat(){
		System.out.println("MyCat is loaded by :"+this.getClass().getClassLoader());
		/*
		如果删除MySample.class文件，那么MySample的class对象将会由我们自定义的类加载器MyClassLoader加载
		又MySample的构造方法中实例化了MyCat对象，那么MyCat类将也会被加载
		因为双亲委托机制，那么MyClassLoader将会委托它的父加载器去加载MyCat类对象
		因为MyCat的class文件存在于classpath中，那么AppClassLoader将能够加载MyCat类
		MyCat类加载完毕之后，程序又引用了MySample类
		又子加载器所加载的类能够访问父加载器加载的类，父加载器加载的类访问不到子加载器加载的类
		所以AppClassLoader访问不到MyClassLoader所加载的MySample类对象
		所以AppClassLoader会再去加载MySample类对象，但是classpath中的MySample.class被我们删了
		所以AppClassLoader加载不了MySample类，那么就会报java.lang.NoClassDefFoundError: zhengwei/jvm/MySample，java.lang.ClassNotFoundException: zhengwei.jvm.MySample
		 */
//		System.out.println("from MyCat "+MySample.class);
	}
}
