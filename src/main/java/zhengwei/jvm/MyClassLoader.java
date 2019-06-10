package zhengwei.jvm;

import java.io.*;

/**
 * 自定义的CLassLoader
 * @author zhengwei AKA Sherlock
 * @since 2019/6/1 17:26
 */
public class MyClassLoader extends ClassLoader {
    private String classLoaderName;
    private String path;
    private final String fileExtension=".class";

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 构造方法：指定加载器的名字
     * @param classLoaderName 加载器的名字
     */
    public MyClassLoader(String classLoaderName){
        super();//默认应用类加载器作为父加载器
        this.classLoaderName=classLoaderName;
    }

    /**
     * 构造方法：显示指定父加载器，并指定加载器的名字
     * @param parent
     * @param classLoaderName
     */
    public MyClassLoader(ClassLoader parent,String classLoaderName){
        super(parent);//显示指定该类的父加载器
        this.classLoaderName=classLoaderName;
    }

    /**
     * 寻找指定二进制名字的类
     * 子类必须重写findClass，ClassLoader中的findClass方法体重只是抛出了一个异常
     * 我们不直接调用findClass这个方法，而是由ClassLoader中的loadClass方法进行调用
     * @param binaryName 类的二进制名字
     * @return 给定的二进制名字的类的class对象
     * @throws ClassNotFoundException 异常
     */
    @Override
    protected Class<?> findClass(String binaryName) throws ClassNotFoundException {
        System.out.println("findClass invoked:"+binaryName);
        System.out.println("class loader name:"+this.classLoaderName);
        byte[] data=loadClassData(binaryName);
        return super.defineClass(binaryName,data,0,data.length);
    }

    @Override
    public String toString() {
        return "["+this.classLoaderName+"]";
    }

    /**
     * 读取class文件
     * @param binaryName 类的二进制名字
     * @return 指定的类的字节码的文件的字节数组
     */
    private byte[] loadClassData(String binaryName) {
        InputStream is = null;
        byte[] data=null;
        ByteArrayOutputStream baos = null;
        try{
            binaryName=binaryName.replace(".", "/");
            System.out.println(this.path+binaryName+this.fileExtension);
            is=new FileInputStream(new File(this.path+binaryName+this.fileExtension));
            baos=new ByteArrayOutputStream();
            int ch;
            while (-1!=(ch=is.read())){
                baos.write(ch);
            }
            data=baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert baos !=null;
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        //实例化自定义类加载器并命名为loader，默认系统类加载器(AppClassLoader)为其父加载器
        MyClassLoader loader1=new MyClassLoader("loader1");
        /*
         * 1.loader的父加载器是系统类加载器，系统类加载器会去加载classpath下的类
         * 2.当要加载的类在工程目录中时，我们自定义的类加载器会先去委托它的父类加载器去加载，父类加载器即系统类加载器能够加载的话就不会去调用我们重写的findClass方法
         * 3.如果要加载的类不在工程目录下时，我们自定义的类加载器也还是会先去委托父类加载器去加载，父加载器即系统类加载器发现要加载的类不在classpath下，
         *   那么加载类的任务会回到自定义的类加载器上，因为我们指定了类的绝对路径，那么自定义类是可以加载的，在loadCLass时也就会调用我们重写的findClass方法
         */
//        loader.setPath("target/classes/zhengwei/jvm");
        loader1.setPath("E:/temp/");
        Class<?> clazz1 = loader1.loadClass("zhengwei.jvm.TestClassLoader");
        System.out.println("class hashcode:"+clazz1.hashCode());
        Object o1 = clazz1.newInstance();
        System.out.println("object1:"+o1);
        System.out.println("object1 classloader"+o1.getClass().getClassLoader());
        System.out.println("=====================================================");

        /*
         * +XX:+TraceClassUnloading:监控被卸载的类
         */
        loader1=null;//gc helper
        clazz1=null;//ge helper
        o1=null;//gc helper
        System.gc();//第一次被加载的TestClassLoader被卸载了

        loader1=new MyClassLoader("loader1");
        loader1.setPath("E:/temp/");
        clazz1 = loader1.loadClass("zhengwei.jvm.TestClassLoader");
        System.out.println("class hashcode:"+clazz1.hashCode());
        o1 = clazz1.newInstance();
        System.out.println("object1:"+o1);
        System.out.println("object1 classloader"+o1.getClass().getClassLoader());
        System.out.println("=====================================================");

        /*MyClassLoader loader2=new MyClassLoader(loader1,"loader2");
        loader2.setPath("E:/temp/");
        Class<?> clazz2 = loader2.loadClass("zhengwei.jvm.TestClassLoader");
        System.out.println("class hashcode:"+clazz2.hashCode());
        Object o2 = clazz2.newInstance();
        System.out.println("object2:"+o2);
        System.out.println("object2 classloader"+o2.getClass().getClassLoader());
        System.out.println("=====================================================");
        MyClassLoader loader3=new MyClassLoader("loader3");
        loader3.setPath("E:/temp/");
        Class<?> clazz3 = loader3.loadClass("zhengwei.jvm.TestClassLoader");
        System.out.println("class hashcode:"+clazz3.hashCode());
        Object o3 = clazz2.newInstance();//class对象的实例
        System.out.println("object3:"+o3);
        System.out.println("object3 classloader"+o3.getClass().getClassLoader());
        System.out.println("=====================================================");*/
        /*
         * 总结：
         *		每个类加载器的命名空间是不同的。
         * 		在不同的命名空间中，相同的类可以被加载多次即在不同的命名空间中相同的类可以同时被加载(例如loader1和loader3)
         * 		在相同的命名空间中相同的类只会被加载一次
         *		如果一个类已经被加载完毕之后，那么下次将不会再去加载这个类，而是直接返回之前加载好的class(loadClass会先调用findLoadedClass(name)去寻找已经加载好的类)
         *      双亲委托机制即一个类加载器会首先去委托它的父类加载器，如果父类加载器加载不了的话，才会由自身类加载器去加载。
         */
    }
}
