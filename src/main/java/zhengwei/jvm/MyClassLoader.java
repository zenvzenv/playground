package zhengwei.jvm;

import java.io.*;

/**
 * 自定义的CLassLoader
 * @author zhengwei AKA Sherlock
 * @since 2019/6/1 17:26
 */
public class MyClassLoader extends ClassLoader {
    private String classLoaderName;
    private final String fileExtension=".class";
    public MyClassLoader(String classLoaderName){
        super();//默认应用类加载器作为父加载器
        this.classLoaderName=classLoaderName;
    }
    public MyClassLoader(ClassLoader parent,String classLoaderName){
        super(parent);//显示指定该类的父加载器
        this.classLoaderName=classLoaderName;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data=loadClassData(name);
        return super.defineClass(name,data,0,data.length);
    }

    @Override
    public String toString() {
        return "["+this.classLoaderName+"]";
    }
    private byte[] loadClassData(String name) {
        InputStream is = null;
        byte[] data=null;
        ByteArrayOutputStream baos = null;
        try{
            this.classLoaderName=this.classLoaderName.replaceAll(".", File.pathSeparator);
            is=new FileInputStream(new File(name+this.fileExtension));
            baos=new ByteArrayOutputStream();
            int ch=0;
            while (-1!=(ch=is.read())){
                baos.write(ch);
            }
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
    public static void test(ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> clazz=classLoader.loadClass("zhengwei.jvm.TestClassLoader");
        Object o = clazz.newInstance();
        System.out.println(o);
    }
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        MyClassLoader loader=new MyClassLoader("loader");
        test(loader);
    }
}
