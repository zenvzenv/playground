package zhengwei.jvm.classloader;

import java.io.*;

/**
 * 打破双亲委托机制的类加载器
 * 即便我们打破了双亲委托机制的类加载机制，但是仍然无法加载我们自定义的java.lang包的内容
 * 这在Java是不被允许的，会报这样的错：java.lang.SecurityException: Prohibited package name: java.lang
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/28 12:59
 */
public class BreakParentClassLoader extends ClassLoader {
	private static final String DEFAULT_CLASS_DIR = "e:/temp/";
	private String dir = DEFAULT_CLASS_DIR;
	private String name;

	public BreakParentClassLoader() {
		super();
	}

	public BreakParentClassLoader(ClassLoader parent, String name) {
		super(parent);
		this.name = name;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String classPath = name.replace(".", "/");
		File classFile = new File(dir, classPath + ".class");
		if (!classFile.exists()) {
			throw new ClassNotFoundException("The class " + name + " not found under " + dir);
		}
		byte[] classBytes = loadClassBytes(classFile);
		assert classBytes != null;
		if (classBytes.length == 0) {
			throw new ClassNotFoundException("Load the class " + name + " failed");
		}
		return this.defineClass(name, classBytes, 0, classBytes.length);
	}

	private byte[] loadClassBytes(File classFile) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     FileInputStream fis = new FileInputStream(classFile)) {
			int data;
			while ((data = fis.read()) != -1) {
				baos.write(data);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 为了打破双亲委托机制的类加载机制而重写loadClass方法
	 *
	 * @param name
	 * @param resolve
	 * @return
	 * @throws ClassNotFoundException
	 */
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = null;
		//如果是Java系统自带的jar包还是让系统类加载器去加载
		//如果想要实验加载自定义的java.lang.String的话，需要把下面这段代码注释掉
		if (name.startsWith("java.")) {
			try {
				ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
				clazz = systemClassLoader.loadClass(name);
				if (clazz != null) {
					if (resolve) resolveClass(clazz);
					return clazz;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		//除了Java系统的jar包，其余的class由我们的类加载器去加载
		try {
			clazz = findClass(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//如果自定义类加载器加载不了的话，由父类加载器去加载
		if (null == clazz && getParent() != null) {
			getParent().loadClass(name);
		}
		return clazz;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) throws ClassNotFoundException {
//		EncryptUtils.doEncrypt("E:/temp/zhengwei/jvm/classloader/MySimpleObj.class","E:/temp/zhengwei/jvm/classloader/MySimpleObj.class1");
		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		BreakParentClassLoader breakParentClassLoader=new BreakParentClassLoader(systemClassLoader,"break");
//		Class<?> clazz = breakParentClassLoader.loadClass("zhengwei.jvm.classloader.MySimpleObj");
		//我们自定义的类加载器是加载不了java.lang.String的
		// 即便java.lang.String也是我们自定义的，也是加载不了的
//		Class<?> clazz = breakParentClassLoader.loadClass("java.lang.String");
		//系统类加载器是可以去加载java.lang.String的，当用系统类加载器去加载的时候，他将会遵循双亲委托机制去加载类
		Class<?> clazz = systemClassLoader.loadClass("java.lang.String");
		System.out.println(clazz);
		System.out.println(clazz.getClassLoader());
	}
}
