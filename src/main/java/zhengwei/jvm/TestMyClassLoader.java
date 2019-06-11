package zhengwei.jvm;

/**
 * 类加载器命名空间
 *  1.子加载器所加载的类能够访问父加载器加载的类
 *  2.父加载器加载的类访问不到子加载器加载的类
 * @author zhengwei AKA Sherlock
 * @since 2019/6/10 18:22
 */
public class TestMyClassLoader {
	public static void main(String[] args) throws Exception {
		MyClassLoader loader1=new MyClassLoader("loader1");//系统类加载器将会作为loader1的父加载器
		loader1.setPath("E:/temp/");
		Class<?> clazz = loader1.loadClass("zhengwei.jvm.MySample");
		System.out.println("class hash code is "+clazz.hashCode());
		/*
		如果注释掉这句话，那么就不会实例化MySample对象，即MySample就不会实例化，构造方法就不会被调用
		因此MyCat也不会被实例化，即没有对MyCat的主动使用，这里就不会加载MyCat.class
		 */
		Object o = clazz.newInstance();
	}
}
