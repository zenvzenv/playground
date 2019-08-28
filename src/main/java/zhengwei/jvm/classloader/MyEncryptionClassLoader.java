package zhengwei.jvm.classloader;

import java.io.*;

/**
 * 加密类加载器
 * 在加载加密之后的class文件的时候
 * 需要先对class文件进行加密操作，利用一种约定好的一种加密方式对文件进行加密
 * 然后在我们自定义的ClassLoader中把度进来的class文件二进制流进行解密操作
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/27 19:16
 */
public class MyEncryptionClassLoader extends ClassLoader {
	private static final String DEFAULT_DIR = "e:/";
	private String dir = DEFAULT_DIR;

	public MyEncryptionClassLoader() {
		super();
	}

	public MyEncryptionClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String classPath = name.replace(".", "/");
		File classFile = new File(this.dir, classPath + ".class");
		if (!classFile.exists()) {
			throw new ClassNotFoundException("The Class " + name + " not found under dir " + this.dir);
		}
		byte[] classBytes = loadClassBytes(classFile);
		if (null == classBytes || classBytes.length == 0) {
			throw new ClassNotFoundException("The Class " + name + " unsuccessful");
		}
		//可以读取多个class文件
		return this.defineClass(name, classBytes, 0, classBytes.length);
	}

	private byte[] loadClassBytes(File classFile) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     FileInputStream fis = new FileInputStream(classFile)) {
			int data;
			while ((data = fis.read()) != -1) {
				baos.write(data ^ EncryptUtils.ENCRYPT_FACTORY);
			}
			baos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		//对class文件加密
//		EncryptUtils.doEncrypt("E:/zhengwei/jvm/classloader/MySimpleObj.class","E:/zhengwei/jvm/classloader/MySimpleObj.class1");
		MyEncryptionClassLoader myEncryptionClassLoader=new MyEncryptionClassLoader();
		Class<?> aClass = myEncryptionClassLoader.findClass("zhengwei.jvm.classloader.MySimpleObj");
		System.out.println(aClass);
		//类需要用public修饰，否则在反射的时候会有权限问题
		Object o = aClass.newInstance();
	}
}

/**
 * 对class文件进行加密的工具类
 */
final class EncryptUtils {
	static final byte ENCRYPT_FACTORY = ((byte) 0xff);

	//私有化构造器，不让外部系统实例化工具类
	private EncryptUtils() {
	}

	/**
	 * 简单加密算法
	 *
	 * @param source 需要加密的字符串
	 * @param target 加密之后的字符串
	 */
	public static void doEncrypt(String source, String target) {
		try (FileInputStream fis = new FileInputStream(source);
		     FileOutputStream fos = new FileOutputStream(target)) {
			int data;
			while ((data = fis.read()) != -1) {
				fos.write(data ^ ENCRYPT_FACTORY);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class MySimpleObj {
	static {
		System.out.println("MySimpleObj is init...");
	}
}