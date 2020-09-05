# JVM专栏(标题排序不分先后，积累到什么就记录什么)
## 一、TestGC-->学习JVM的时候测试GC的一些代码
## 二、zhengwei.jvm.classloader-->学习JVM中类加载过程时测试时写的实例代码，有代码的帮助，理解起来会更加的透彻
> 类的加载过程是:加载->链接->初始化->使用->卸载<br/>
> 其中链接分为:验证->准备->解析<br/>
>__需要特别注意的是准备和初始化是两个过程__<br/>
>就算一个类被加载了也不一定会出初始化这个类，只有这个类满足JVM所要求的对类的使用规范时，才会去触发类的初始化流程
1. 加载：加载是把.class文件加载进JVM之中，**在JVM规范中，并没明确规定类要在何时被加载，这点交由JVM具体实现来自友把控**
    1. 加载类的方式有从本地直接加载；通过网络下载.class文件；从jar中加载；从专有数据库中加载和将Java动态编译成.class文件
    2. 类加载器
        1. 类加载器是用来把类加载进JVM中的，从JDK1.2开始，类加载采用**双亲委托机制**，这种机制保证了JVM平台的安全性。在此委托机制中，
        除了JVM自带的根类加载器外，其余的类加载器都有且只有一个父类加载器。
           Java中所有的核心类库都将会由JVM自带的Bootstrap ClassLoader、ExtClassLoader和AppClassLoader进行加载，用户自定义的
           类加载器是没有机会去加载的，防止包含恶意核心类乎代码被加载。
        2. JVM自带的类加载器
            1. 根类加载器(**Bootstrap ClassLoader**)，无父类，最顶级的类，会去系统属性 `sun.boot.class.path` 所指定的路径下加载
            类库，由C++实现，不是ClassLoader的子类
            2. 扩展类加载器(**ExtClassLoader**),父加载器是**根类加载器**，负责加载Java平台中扩展功能的一些jar包，
            它从系统属性 `java.ext.dirs` 所指定的目录下加载类库，它是ClassLoader的子类
            3. 系统类加载器(**AppClassLoader**)，父加载器是**扩展类加载器**，负责加载classpath中所指定的jar包，
            从系统属性 `java.class.path` 所指定的目录下加载类，它是ClassLoader的子类
        3. 自定义类加载器
            1. 需要继承 `java.lang.ClassLoader` , `java.lang.ClassLoader` 是一个抽象类，但是没有抽象方法，不能够直接实例化，
            需要继承它，然后实例化，需要重写findClass方法。
            2. 用户自定义的类加载器的父类加载器是应用类加载器AppClassLoader
            3. 还有一种特殊的类加载器，它的存在就是为了打破双亲委托机制的局限性，为了使用SPI机制而存在的，
            那就是线程上下文类加载器 `Thread.currentThread().getContextClassLoader()`
                1. 特别注意的是：自从JDK1.6开始我们使用诸如jdbc、xml...等接口的实现时，即具体实现由厂商来实现的功能时，
                其实不需要再去显示的去调用 `Class.forName("xxx.class")`
                2. 因为有 `java.util.ServiceLoader` 类的存在
                    1. 一个重要的属性 `private static final String PREFIX = "META-INF/services/";` ，
                    ServiceLoader会加载去classpath下jar包中的META-INF/services/文件中所表明要加载的类的二进制名字，
                       但是ServiceLoader这个类是由BootstrapClassLoader去加载的，但是BootstrapClassLoader加载不了我们指定的厂商实现的jar包，
                       那么这时候就要用到线程上下文类加载器( `Thread.currentThread().getContextClassLoader()` )
                       代码如下面的load方法
                    2. 一个重要的方法 `public static <S> ServiceLoader<S> load(Class<S> service)`，具体代码如下
                    ```java
                    public static <S> ServiceLoader<S> load(Class<S> service) {
                            //获取线程上下文类加载器
                            ClassLoader cl = Thread.currentThread().getContextClassLoader();
                            return ServiceLoader.load(service, cl);
                    }
                    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
                         return new ServiceLoader<>(service, loader);
                    }
                    private ServiceLoader(Class<S> svc, ClassLoader cl) {
                         service = Objects.requireNonNull(svc, "Service interface cannot be null");
                         //如果线程上下文类加载器为空则使用应用类加载器作为加载器去加载类
                         loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
                         acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
                         reload();
                   }
                   ```
                   `java.sql.DriverManager` loadInitialDrivers会去调用 `ServiceLoader.load` 方法去加载jdbc相关的驱动类
                   ```java
                    private static void loadInitialDrivers() {
                            String drivers;
                            try {
                                drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                                    public String run() {
                                        return System.getProperty("jdbc.drivers");
                                    }
                                });
                            } catch (Exception ex) {
                                drivers = null;
                            }
                            // If the driver is packaged as a Service Provider, load it.
                            // Get all the drivers through the classloader
                            // exposed as a java.sql.Driver.class service.
                            // ServiceLoader.load() replaces the sun.misc.Providers()
                    
                            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                                public Void run() {
                                    //去加载jdbc的驱动类
                                    ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                                    Iterator<Driver> driversIterator = loadedDrivers.iterator();
                    
                                    /* Load these drivers, so that they can be instantiated.
                                     * It may be the case that the driver class may not be there
                                     * i.e. there may be a packaged driver with the service class
                                     * as implementation of java.sql.Driver but the actual class
                                     * may be missing. In that case a java.util.ServiceConfigurationError
                                     * will be thrown at runtime by the VM trying to locate
                                     * and load the service.
                                     *
                                     * Adding a try catch block to catch those runtime errors
                                     * if driver not available in classpath but it's
                                     * packaged as service and that service is there in classpath.
                                     */
                                    try{
                                        while(driversIterator.hasNext()) {
                                            driversIterator.next();
                                        }
                                    } catch(Throwable t) {
                                    // Do nothing
                                    }
                                    return null;
                                }
                            });
                    
                            println("DriverManager.initialize: jdbc.drivers = " + drivers);
                    
                            if (drivers == null || drivers.equals("")) {
                                return;
                            }
                            String[] driversList = drivers.split(":");
                            println("number of Drivers:" + driversList.length);
                            for (String aDriver : driversList) {
                                try {
                                    println("DriverManager.Initialize: loading " + aDriver);
                                    Class.forName(aDriver, true,
                                            ClassLoader.getSystemClassLoader());
                                } catch (Exception ex) {
                                    println("DriverManager.Initialize: load failed: " + ex);
                                }
                            }
                        }
                    ```
        4. 类加载器并不会等到某个类被**首次主动使用**的时候再去加载它。JVM规范允许加载器在预料到某个类要被使用的时候就预先加载它，
           如果在预先加载过程中遇到了.class文件缺失或存在错误，类加载器必须在**程序首次主动**使用该类时才报告错误(Linkage Error)，
           如果这个类一直没有被**主动使用**，那么类加载器将不会报告此错误。
        5. 获取ClassLoader的几种方式
            1. 获取当前类加载器: `clazz.getClassLoader();`
            2. 获取当前线程的上下文类加载器: `Thread.currentThread().getContextClassLoader();`
            3. 获取系统的类加载器: `ClassLoader.getSystemClassLoader();`
            4. 获得调用者的类加载器: `DirverManager.getCallerClassLoader();`
        6. 值得注意的是：各个类加载器之间的关系并**不是继承关系**，而是**包含关系**，形成一种**树形结构**。
        除了根类加载器，其余的类加载器都有且只有一个父类加载器。
        7. 类加载器的**命名空间**：
            1. 每个类加载器都有自己的命名空间，**命名空间由该类加载器及其所有父类加载器加载的类组成**。
            2. 在同一个命名空间中，不会出现类的全限定名相同的两个类(即class对象在Java Heap中只会有一个实例)。
            3. 在不同的命名空间中，可能出现类的全限定名相同的两个类(即class对象在Java Heap中可能会存在多个实例)。
            [zhengwei.jvm.classloader.TestClassLoader3.testClassLoaderNamespace]
            4. **子类加载器的命名空间包含所有父类加载器的命名空间**。因此由子类加载器加载的类能够访问到父类加载器加载的类，
            但是父类加载器是访问不到子类加载器加载的类的，例如扩展类加载器能够访问到根类加载器加载的类。
            5. 如果两个加载器之间没有直接或间接的关系，那么它们各自加载的类将互不可见。
            [zhengwei.jvm.classloader.TestClassLoader3.testClassLoaderNamespace]
        8. 创建自定义类加载器，只需要继承 `java.lang.ClassLoader` 类，然后重写 `findClass(String name)` 方法即可，
        该方法根据指定的类的二进制名字，返回对应的Class对象的引用。
2. 链接：将类与类之间的关系处理好
    1. 验证，**验证阶段是很重要的，但也不是必须的，它对运行期没有任何影响，可以使用JVM参数 `-Xverifynone` 来关闭大部分的类验证工作，以缩短类加载时间**
        1. 校验.class文件的正确性：魔数因子是否正确；版本号是否符合当前JVM版本；常量池中的类型是否支持
        2. 语义检查；字节码验证和二进制兼容性验证，把加载的类的二进制文件合并到JVM中去；是否有父类；父类是否允许继承；
        是否实现了抽象方法；是否覆盖了父类的final字段或方法；
        3. 字节码验证：主要进行数据流控制和控制流的分析，数据类型是否匹配的分析
        4. 符号引用验证：调用一个不存在的方法和字段，确保解析动作能够正常的执行，如果无法通过符号引用验证则会抛出一个
        `java.lang.IncompatibleClassChangeError` 异常的子类，
        如 `java.lang.IllegalAccessError` 、 `java.lang.NoSuchFieldError` 、 `java.lang.NoSuchMethodError`等
    2. 准备：为类的**静态变量**分配内存空间，并将其**赋初始值**，比如八种基本变量的默认值，如果是引用变量的话其默认值是null，
    在到达初始化之前，类的静态变量只是只是jvm赋予的默认值，而不是真正的用户指定的值，**这些变量都是在方法区中分配内存的**，
    **如果被 `final` 修饰的话，那么在准备阶段其值就已经是指定的值了**
    3. 解析：将类中常量池中寻找类、接口、字段和方法的符号引用替换成直接引用的过程，虚拟机规范中并没有规定解析阶段发送的具体时间，
    只要求在执行 `anewarray,checkcast,getfield,getstatic,instanceof,invokeinterface,invokespecial,invokestatic,
    invokevirtual,multianewarray,new,putfield和putstatic`这13个用于操作符号引用的字节码指令之前，先对它们所引用的符号进行解析。
        1. 类或接口的解析
        2. 字段解析
        3. 类方法解析
        4. 接口方法解析
3. 初始化：为类的静态变量赋予正确的默认值(用户指定的初始值)，就是把链接阶段中的准备阶段的类的静态变量的默认值赋予用户指定的初始值
    1. 类的初始化时机(**特别注意：即使一个类被类加载器加载了，也不一定会去初始化这个类，因为JVM只会在一下几种情况出现的时候才会
    去初始化一个类，其余的时候是不会去初始化这个类的**)
        1. 创建的类的实例
        2. 访问某个类或接口的静态变量(字节码中使用`getstatic`标记)，或者对静态变量进行赋值(字节码中使用`putstatic`标记)，
        或者调用类的静态方法(字节码中使用`invokestatic`)
        3. 反射Class.forName("zhengwei.jvm.Test");调用一个参数的Class.forName("xxxxx");是会默认初始化该类的，源码中是有体现的。
            ```java
            public static Class<?> forName(String className) throws ClassNotFoundException {
                     Class<?> caller = Reflection.getCallerClass();
                     //true代表要初始化这个类
                     return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
            }
            ```
            ```java
            private static native Class<?> forName0(String name, boolean initialize,
                                                         ClassLoader loader,
                                                         Class<?> caller)
            throws ClassNotFoundException;
            ```
        4. 初始化一个类的子类，同时也会初始化这个类的父类，如果父类还有父类，那么会继续初始化父类的父类直到最顶级的父类。
        这条规则不适用于接口。
        5. JVM启动时被表明启动类的类，包含main方法的类。
        6. JDK1.7支持动态语言调用。
        7. 除了上述的几种调用方式，**其余的调用都是被动调用**，都不会导致类的初始化。
    2. 在初始化阶段，JVM会执行类的初始化语句，为类的静态变量赋予初始值(即**程序员自己指定的值**)，在程序中，
    静态变量的初始化方法有两种：
        1. 在静态变量处声明初始值： `public static int a = 1;`
        2. 在静态代码块进行初始化： `public static int a ; static { a = 1; }`
    3. 静态变量的声明语句，以及静态代码块都被看作类的初始化语句，JVM会严格按照初始化语句在类文件的**既定顺序**去执行它们。
    4. 类的初始化步骤
        1. 加入这个类没有被加载和连接，那就先进行加载和连接。
        2. 假如类存在直接父类，并且这个类还没有被初始化，那就初始化父类。
        3. 假如类存在初始化语句，那就依次执行类的初始化语句。
    5. **接口的初始化和类的初始化是有一些区别的**。
        1. 在初始化一个接口的时候并不会初始化它的父接口。
        2. 因此，一个父接口并不会因为它的父接口或者实现类被初始化而被初始化，只有当程序首次使用了该接口的特定接口的静态变量时才会导致该接口的初始化。
    6. **调用ClassLoader的loadClass方法加载一个类时，并不是对类的主动使用，不会导致类的初始化。**
    7. **初始化阶段就是执行类构造器<clinit>()的过程，<clinit>()方法是编译器收集类中所有的类变量赋值操作和static代码块的语句合并产生的，
    并严格按照语句在源代码中出现的顺序依次收集到<clinit>()方法中**
    8. JVM会保证**类构造器<clinit>()在多线程环境中被正确的加锁、同步**，如果有多个线程去初始化同一个类，
    那么只会有一个线程去执行类构造器<clinit>()，其他线程需要阻塞等待，直到活动线程执行<clinint>()完毕，
       **需要特别注意的是：虽然其他线程在同一个类加载器下，一个类型只会被加载一次**
    ```java
    public class TestClassClinitMethod {
        static {
            System.out.println("TestClassClinitMethod init ...");
        }
        /*static {
            System.out.println(Thread.currentThread()+" enter");
            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread()+" exit");
        }*/
        private static class ClassClinit{
            static {
                System.out.println(Thread.currentThread()+" enter");
                try {
                    Thread.sleep(5_000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread()+" exit");
            }
        }
    
        public static void main(String[] args) {
            Runnable runnable= () -> {
                System.out.println(Thread.currentThread()+" start");
                ClassClinit t=new ClassClinit();
                System.out.println(Thread.currentThread()+" over");
            };
            new Thread(runnable,"t1").start();
            new Thread(runnable,"t2").start();
        }
    }
    /* Output: 
            TestClassClinitMethod init ...
            Thread[t1,5,main] start
            Thread[t2,5,main] start
            Thread[t1,5,main] enter
            因为JVM保证<clinit>()方法线程安全，线程休眠期间Thread[t2,5,main]被阻塞，当<clinit>()执行完毕之后，Thread[t2,5,main]将不会再去执行<clinit>()方法
            Thread[t1,5,main] exit
            Thread[t1,5,main] over
            Thread[t2,5,main] over
     *///:~
    ```
4. 使用：
    1. 实例化对象：
        * 为类的新实例分配内存空间，通常在堆上分配内存空间
        * 为实例赋予默认值
        * 为实例赋予指定的默认值
            **注意：Java编译器为它编译的每个类都至少生成一个初始化方法。在Java的.class文件中这个实例化方法被称为<init>，
            针对源代码中的一个构造方法，Java编译器都会产生一个<init>方法**
    2. 实例化一个对象需要经历如下几个过程
        * 父类类构造器<clinit>()->子类类构造器<clinit>()->父类成员变量赋值和实例代码块->父类的构造函数<init>()->子类的成员变量赋值和实例代码块->子类的构造函数<init>()
5. 卸载：把类的相关信息从内存中剔除
    1. 当一个类被加载、链接和初始化之后，它的生命周期就开始了。
    只有当该类不再被引用时，即不可触及时，class对象就结束了它的生命周期，该类的信息将会在方法区卸载，从而结束生命周期。
    2. 一个类何时结束生命周期取决于代表它的class对象何时结束生命周期。
    3. 由Java虚拟机自带的类加载器加载的类，在虚拟机周期中始终不会被卸载。
    Java自带的虚拟机：Bootstrap ClassLoader,ExtClassLoader和AppClassLoader。
    JVM会始终保持对这些类加载器的引用，而这些类加载器也会保持它们所加载类的class对象的引用，因此这些class对象始终是可触及的。
    4. 由用户自定义的类加载器加载的类是可以被卸载的。
**值得注意的是：类在准备和初始化阶段中，在执行为静态变量赋值遵循从上到下的顺序执行具体实例参见[zhengwei.jvm.classloader.TestClassLoader2]**
    * 类和接口在加载的时候有一些不同，JVM在初始化一个类时，要求它的全部父类全部初始化完毕，但是这条规则不适用于接口
        1. 初始化一个类时，并不会初始化它所有实现的接口
        2. 在初始化一个接口时，并不会先去初始化它的父接口  
        因此，一个父接口并不会因为它的子接口或实现类初始化而初始化，只有当程序首次使用了特定接口的静态变量时，才会去初始化该接口。
6. Launcher类，Java程序的入口
    1. 线程上下文类加载的默认值是应用类加载器AppClassLoader，源码中的体现 `sun.misc.Launcher` ,
    Launcher是由Bootstrap ClassLoader加载，ExtClassLoader和AppClassLoader都会在Launcher中进行初始化
    ```java
    private static URLStreamHandlerFactory factory = new Launcher.Factory();
    private static Launcher launcher = new Launcher();
    private static String bootClassPath = System.getProperty("sun.boot.class.path");
    private ClassLoader loader;
    public Launcher() {
            Launcher.ExtClassLoader var1;
            try {
                //初始化ExtClassLoader
                extClassLoader = Launcher.ExtClassLoader.getExtClassLoader();
            } catch (IOException var10) {
                throw new InternalError("Could not create extension class loader", var10);
            }
            try {
                //初始化AppClassLoader，设置AppClassLoader的父类加载器为扩展类加载器ExtClassLoader
                this.loader = Launcher.AppClassLoader.getAppClassLoader(extClassLoader);
            } catch (IOException var9) {
                throw new InternalError("Could not create application class loader", var9);
            }
            //设置线程上下文类加载器为AppClassLoader
            Thread.currentThread().setContextClassLoader(this.loader);
            String var2 = System.getProperty("java.security.manager");
            if (var2 != null) {
                SecurityManager var3 = null;
                if (!"".equals(var2) && !"default".equals(var2)) {
                    try {
                        var3 = (SecurityManager)this.loader.loadClass(var2).newInstance();
                    } catch (IllegalAccessException var5) {
                    } catch (InstantiationException var6) {
                    } catch (ClassNotFoundException var7) {
                    } catch (ClassCastException var8) {
                    }
                } else {
                    var3 = new SecurityManager();
                }
    
                if (var3 == null) {
                    throw new InternalError("Could not create SecurityManager: " + var2);
                }
    
                System.setSecurityManager(var3);
            }
    
   }
    ```
    ExtClassLoader类是Launcher类中的静态内部类
    ```java
    static class ExtClassLoader extends URLClassLoader {
            private static volatile Launcher.ExtClassLoader instance;
    
            public static Launcher.ExtClassLoader getExtClassLoader() throws IOException {
                //单例模式
                if (instance == null) {
                    Class var0 = Launcher.ExtClassLoader.class;
                    synchronized(Launcher.ExtClassLoader.class) {
                        if (instance == null) {
                            instance = createExtClassLoader();
                        }
                    }
                }
    
                return instance;
            }
            //初始化ExtClassLoader
            private static Launcher.ExtClassLoader createExtClassLoader() throws IOException {
                try {
                    return (Launcher.ExtClassLoader)AccessController.doPrivileged(new PrivilegedExceptionAction<Launcher.ExtClassLoader>() {
                        public Launcher.ExtClassLoader run() throws IOException {
                            //获取java.ext.dir系统属性所指定的目录下的文件即jar包
                            File[] var1 = Launcher.ExtClassLoader.getExtDirs();
                            int var2 = var1.length;
    
                            for(int var3 = 0; var3 < var2; ++var3) {
                                MetaIndex.registerDirectory(var1[var3]);
                            }
                            //调用构造方法，加载目录下的jar包
                            return new Launcher.ExtClassLoader(var1);
                        }
                    });
                } catch (PrivilegedActionException var1) {
                    throw (IOException)var1.getException();
                }
            }
    
            void addExtURL(URL var1) {
                super.addURL(var1);
            }
            //构造方法，传入要加载的jar的文件数组
            public ExtClassLoader(File[] var1) throws IOException {
                super(getExtURLs(var1), (ClassLoader)null, Launcher.factory);
                SharedSecrets.getJavaNetAccess().getURLClassPath(this).initLookupCache(this);
            }
    
            private static File[] getExtDirs() {
                String var0 = System.getProperty("java.ext.dirs");
                File[] var1;
                if (var0 != null) {
                    StringTokenizer var2 = new StringTokenizer(var0, File.pathSeparator);
                    int var3 = var2.countTokens();
                    var1 = new File[var3];
    
                    for(int var4 = 0; var4 < var3; ++var4) {
                        var1[var4] = new File(var2.nextToken());
                    }
                } else {
                    var1 = new File[0];
                }
    
                return var1;
            }
            //获取
            private static URL[] getExtURLs(File[] var0) throws IOException {
                Vector var1 = new Vector();
    
                for(int var2 = 0; var2 < var0.length; ++var2) {
                    String[] var3 = var0[var2].list();
                    if (var3 != null) {
                        for(int var4 = 0; var4 < var3.length; ++var4) {
                            if (!var3[var4].equals("meta-index")) {
                                File var5 = new File(var0[var2], var3[var4]);
                                var1.add(Launcher.getFileURL(var5));
                            }
                        }
                    }
                }
    
                URL[] var6 = new URL[var1.size()];
                var1.copyInto(var6);
                return var6;
            }
    
            public String findLibrary(String var1) {
                var1 = System.mapLibraryName(var1);
                URL[] var2 = super.getURLs();
                File var3 = null;
    
                for(int var4 = 0; var4 < var2.length; ++var4) {
                    URI var5;
                    try {
                        var5 = var2[var4].toURI();
                    } catch (URISyntaxException var9) {
                        continue;
                    }
    
                    File var6 = Paths.get(var5).toFile().getParentFile();
                    if (var6 != null && !var6.equals(var3)) {
                        String var7 = VM.getSavedProperty("os.arch");
                        File var8;
                        if (var7 != null) {
                            var8 = new File(new File(var6, var7), var1);
                            if (var8.exists()) {
                                return var8.getAbsolutePath();
                            }
                        }
    
                        var8 = new File(var6, var1);
                        if (var8.exists()) {
                            return var8.getAbsolutePath();
                        }
                    }
    
                    var3 = var6;
                }
    
                return null;
            }
    
            private static AccessControlContext getContext(File[] var0) throws IOException {
                PathPermissions var1 = new PathPermissions(var0);
                ProtectionDomain var2 = new ProtectionDomain(new CodeSource(var1.getCodeBase(), (Certificate[])null), var1);
                AccessControlContext var3 = new AccessControlContext(new ProtectionDomain[]{var2});
                return var3;
            }
    
            static {
                ClassLoader.registerAsParallelCapable();
                instance = null;
            }
        }
    ```
    AppClassLoader类是Launcher类中的静态内部类
    ```java
    static class AppClassLoader extends URLClassLoader {
            final URLClassPath ucp = SharedSecrets.getJavaNetAccess().getURLClassPath(this);
    
            public static ClassLoader getAppClassLoader(final ClassLoader var0) throws IOException {
                final String var1 = System.getProperty("java.class.path");
                //获得指定目录下的文件
                final File[] var2 = var1 == null ? new File[0] : Launcher.getClassPath(var1);
                return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction<Launcher.AppClassLoader>() {
                    public Launcher.AppClassLoader run() {
                        //要加载的jar包的文件路径
                        URL[] var1x = var1 == null ? new URL[0] : Launcher.pathToURLs(var2);
                        //构造方法，传入系统属性java.class.path指定的目录下的jar包，和父类加载器(即ExtClassLoader)
                        return new Launcher.AppClassLoader(var1x, var0);
                    }
                });
            }
            //构造方法，传入要加载的jar包的路径和父加载器ExtClassLoader
            AppClassLoader(URL[] var1, ClassLoader var2) {
                super(var1, var2, Launcher.factory);
                this.ucp.initLookupCache(this);
            }
            //加载类方法
            public Class<?> loadClass(String var1, boolean var2) throws ClassNotFoundException {
                int var3 = var1.lastIndexOf(46);
                if (var3 != -1) {
                    SecurityManager var4 = System.getSecurityManager();
                    if (var4 != null) {
                        var4.checkPackageAccess(var1.substring(0, var3));
                    }
                }
    
                if (this.ucp.knownToNotExist(var1)) {
                    Class var5 = this.findLoadedClass(var1);
                    if (var5 != null) {
                        if (var2) {
                            this.resolveClass(var5);
                        }
    
                        return var5;
                    } else {
                        throw new ClassNotFoundException(var1);
                    }
                } else {
                    return super.loadClass(var1, var2);
                }
            }
    
            protected PermissionCollection getPermissions(CodeSource var1) {
                PermissionCollection var2 = super.getPermissions(var1);
                var2.add(new RuntimePermission("exitVM"));
                return var2;
            }
    
            private void appendToClassPathForInstrumentation(String var1) {
                assert Thread.holdsLock(this);
    
                super.addURL(Launcher.getFileURL(new File(var1)));
            }
    
            private static AccessControlContext getContext(File[] var0) throws MalformedURLException {
                PathPermissions var1 = new PathPermissions(var0);
                ProtectionDomain var2 = new ProtectionDomain(new CodeSource(var1.getCodeBase(), (Certificate[])null), var1);
                AccessControlContext var3 = new AccessControlContext(new ProtectionDomain[]{var2});
                return var3;
            }
    
            static {
                ClassLoader.registerAsParallelCapable();
            }
        }
    ```
## 三、zhengwei.jvm.bytecode->学习Java字节码时敲的一些实例代码
1. 可以使用 `javap -verbose -p` 命令来分析一个class文件，将会分析文件中的魔数、版本号、常量池、类信息、类构造方法和成员变量。
2. 魔数：所有的.class文件前四个字节为魔数，魔数的固定值是 `0xCAFEBABE`
3. 魔数之后的四个字节是版本信息，前两个字节是minor version，后两个字节是major version，可以使用 `java -version` 来验证这一点。
4. 常量池(constant pool)：紧接着版本号之后的就是常量池入口，一个Java类中定义的很多信息都由常量池来维护和描述，
可以将常量池看作是class文件的资源仓库，比如Java中的定义的方法和变量信息都存储在常量池中。<br/>
   常量池中主要存储两类常量：字面量和符号引用。**字面量就是文本字符串，Java中被申明成final的常量值；
   而符号引用是如类和接口的全限定名，字段的名称和描述符，方法的名称和描述符**。
5. 常量池的总体结构：Java类所对应的常量池主要由常量池数量和常量池表组成。常量池的数量紧跟在版本号之后，占据两个字节；
常量池表紧跟在常量池数量之后，常量数组表与一般的数组不同，<br/>
   常量数组中都是不同的元素类型、结构不同的，长度自然也会不同，但是每一种元素的第一个数据都是u1类型的，该字节是个标志位，占据一个字节。
   JVM会根据这个标志位来获取元素的具体元素<br/>
   值得注意的是：**常量池中元素的个数=常量池数量-1(其中0暂时不使用)**，
   目的是满足某些常量池索引值的数据在特定情况下需要表达"不引用任何一个常量池"的含义；
   根本原因在于，索引0也是一个常量(保留常量)，只不过它不位于常量池中，这个常量-> l就对应null值，**常量池的索引从1而非0开始**。
6. 在JVM规范中，每个变量/字段都有描述信息，描述信息主要是描述字段的数据类型、方法的参数列表(包括数量、类型与顺序)与返回值。
根据描述规则，基本数据类型和代表无返回值的void类型都用一个大写字母表示，对象类型使用大写的L加上对象的全限定名表示。<br/>
   >`B -> byte`<br/>
   `C -> char`<br/>
   `F -> float`<br/>
   `I -> int`<br/>
   `J -> long`<br/>
   `S -> short`<br/>
   `Z -> boolean`<br/>
   `V -> void`<br/>
   `L -> 对象，例如: Ljava/lang/Object;`
7. 对于数组类型来说，每一个维度都使用一个前置的 `[` 来表示，如 `int[]` 表示成 `[I;`， `String[][]` 表示成 `[[java/lang/String;`
8. 描述方法时，按照先参数列表后返回值类型的顺序来描述，参数列表被严格定义在 `()` 中，
如方法 `String getRealNameByIdAndNickName(int id, String nickNamw)` 表示成 `(I, Ljava/lang/String;)Ljava/lang/String;`
9. Java字节码对于 `this` 关键的处理:  
    1. 对于类的实例方法(非static方法)，其在编译后所生成的字节码中，方法的参数列表中的参数个数总会比源代码中的参数列表的个数多一个this
    2. this位于参数列表的第一个索引位置 `0` ，这样我们就可以在实例方法中使用this来去访问当前对象的属性和方法了
    3. 对于static方法，是使用不了this关键字的，因为static方法是属于class对象的
    4. 这个添加参数的操作在编译期完成，由javac编译器在编译的时候将对this的访问转化为对一个普通实例的访问
    5. 在运行期间，由JVM自动向实例方法传入this
10. Java字节码对于异常的处理
    1. 统一采用异常表的方法去处理异常
    2. 在JDK1.4.2之前，并不是使用异常表的方式去处理异常的，而是采用特定的指令方式
    3. 当异常处理存在finally语句块时，现代化的JVM采用的是将finally语句块拼接到每一个catch语句块的后面，
    换句话说就是有多少个catch就会有多少个finally语句块跟在后面
    4. 被catch住的异常和被抛出的异常的地位是不同的，catch住的异常会被存储在Code中的异常表中，在字节码层面上看，
    有几个catch就会有多少个finally；被抛出的异常在与Code同级的一个异常表属性中
11. 字节码中对于调用方法的助记符
    1. invokeinterface：调用接口中的方法(在Java8中，接口中允许有方法的实现，需要使用default关键字去标识)，实际上是在运行期间确定的，
    决定到底调用实现了该接口的哪个具体的方法
    2. invokestatic：调用静态方法
    3. invokespecial：调用自己的私有方法、构造方法(<init>)以及父类方法
    4. invokevritual：调用虚方法，需要在运行期确定
    5. invokedynamic：
12. 静态解析的四种情形：
    1. 静态方法
    2. 父类方法
    3. 构造方法
    4. 私有方法(不能被重写)<br/>
    以上四种方法被称为非虚方法，它们是在类被加载阶段就可以将符号引用转换成直接引用了
13. 方法的静态派发和动态派发
    1. `Test test=new ChildTest()` 其中 `Test` 是 `test` 的静态类型，而 `ChildTest` 是test的实际类型，实际指向的类型
    2. 静态类型是不会发生改变的，而实际类型是可以发生变换的(多态)，实际类型需要等到运行期才能够确定
    3. 方法的重载和方法的重写的最根本区别就是**方法的接收者不同**，方法的重载(overload)对于JVM来说是静态行为，在编译期确定，
    方法的重写对于JVM来说是动态行为，在运行期确定
    ```java
    public class DeepenStaticAndDynamicDispatch {
        public static void main(String[] args) {
            Animal animal=new Animal();
            Animal dog=new Dog();
            Dog dog2=new Dog();
            /*
            动态分派
            以下代码调用的对象不同(实际类型的不同)，JVM会去寻找实际类型中的重写方法。
             */
            animal.test("hello");
            dog.test("hello");
            /*
            静态分派
            以下代码是调用的Animal中的重载方法
            重载方法是静态过程，在编译期间就会确定调用哪个方法
            所以JVM只会根据参数的静态类型去寻找要执行的方法，而不是根据参数的实际类型去寻找要执行的方法
            即animal和dog的静态类型都是Animal，所以都会去执行public void test(Animal animal)方法
            如果想要执行public void test(Dog dog)方法，需要声明静态类型为Dog的对象。
             */
            animal.test(dog);
            animal.test(dog2);
            animal.test(animal);
        }
    }
    class Animal{
        public void test(String str){
            System.out.println("Animal str->"+str);
        }
        public void test(Date date){
            System.out.println("Animal date->"+date);
        }
        public void test(Dog dog){
            System.out.println("Animal type->"+dog);
        }
        public void test(Animal animal){
            System.out.println("Animal 666->"+animal);
        }
    }
    class Dog extends Animal{
        @Override
        public void test(String str) {
            System.out.println("Dog str->"+str);
        }
    
        @Override
        public void test(Date date) {
            System.out.println("Dog date->"+date);
        }
    }
    ```
## 四、JVM内存划分
1. 程序计数器(**线程私有**)：
    1. 是一块较小的内存空间，可以看作当前程序执行字节码的行号指示器
    2. 每个线程的程序计数器是独立的
    3. 该区域是唯一没有指定OOM的错误区域，如果线程执行的是Java方法的话，那么程序计数器中记录的是正在执行的虚拟机字节码指令地址，
    如果是native方法，那么该区域为空
2. Java虚拟机栈(**线程私有**)：
    1. 和程序计数器一样，Java虚拟机栈是线程私有的，生命周期和线程相同
    2. 每个方法在执行时都会在虚拟机栈中创建一个**栈帧(stack frame)用于存储局部变量表、操作数栈、动态链接、方法出口信息**等
    3. 每个方法从调用到结束，都对应一个栈帧的入栈和出栈的过程
    4. **局部变量表所需的内存空间在编译期间完成分配，当进入一个方法时，这个方法在栈中分配多少内存时完全确定的，
    在方法运行期间不会改变局部变量表的大小**
    5. 该区域会抛出StackOverflowError和OutOfMemoryError错误
    6. 栈中存储的**变量引用**都是**局部的**，即**定义在方法体内部的变量或引用**，**局部变量和引用都在栈中(包含被声明为final的变量)**
    7. 八种基本数据类型(int,byte,short,boolean,float,double,char,long)的**局部变量(定义在方法体中的基本类型的局部变量)在栈中存储它们对应的值**
    8. 栈中还存储**对象的引用**(**定义在方法体内部的引用类型变量**)，对象的引用并不是对象本身，而是**对象在堆中的地址**，
    换句话说，**局部对象的引用所指对象在堆中的地址存储在栈中**，当然，如果对象引用没有指向具体的实例，那么对象引用为`null`
3. 本地方法栈(**线程私有**)
    1. 作用和Java虚拟机栈类似，区别在于Java虚拟机服务于正在运行的Java程序(即字节码)，而本地方法栈则服务于虚拟机执行native方法
    2. 本地方法栈会抛出StackOverflowError和OutOfMemoryError
4. Java堆(**线程共享**)
    1. Java堆是被所有线程共享的一块区域，也是Java虚拟机中内存最大的一块区域，在虚拟机启动时创建，它对内存的要求是逻辑上连续，
    物理上不一定连续
    2. 此内存区域的唯一目的就是存放对象实例，几乎所有的对象都在这里分配内存，也有例外：栈上分配，标量替换技术
    3. Java堆有被称为GC堆，GC在此区域回收的效率最高，Java堆可以大致分为新生代和老年代，
    再细致一点可以分为**Eden空间，From Survivor空间和To Survivor空间**。不论怎么划分都与存放的内容无关，
    都是存放的对象的实例，进一步的划分内存只是为了更好的进行垃圾收集
    4. **实例变量(非static修饰的成员变量)和对象关联在一起，所以实例变量存放在堆中**
    5. **Java数组也是在堆中开辟内存**
    6. **特别注意：对于 `java.lang.Class` 对象与普通对象一样，存在与Java Heap中**即一个Java对象包含两个部分，
    一部分是存储在方法区的类型的元数据信息和存储在Java Heap中的实际数据
    7. Java堆的划分
        1. 新生代与老年代
        2. Eden(8):From Survivor(1):To Survivor(1)=8:1:1
5. 方法区 aka ~~Permanent Generation(永久代)~~ 元空间 (**线程共享**)
    1. 用于存储已被虚拟机**加载的类信息、常量、静态变量、即时编译器编译后的代码**等数据，**方法区包含静态常量池和运行时常量池**。
    注意：如果一个静态变量是引用类型的话，那么所引用的对象是存放在Java Heap中的，该对象的引用会存在方法区
        1. 这个类型的全限定名
        2. 这个类的直接父类的全限定名(**`java.lang.Object` 除外**)，其他类型若没有声明直接父类，默认父类是Object
        3. 这个类的访问修饰符(`public,private,abstract...`)
        4. 这个类的直接接口的有序序列
        5. 类型的常量池
            1. jvm为每个已加载的类型都维护一个常量池
            2. 常量池就是这个类型用到常量的一个有序集合，包括实际的常量(字面量)和对类型、域和方法的符号引用
            3. 池中的每一项数据都通过索引访问。
            4. 因为常量池中存储了一个类型所用到的所有的符号引用，所以它在Java程序的动态链接中起到了核心作用
        6. 域(Field)信息
            1. JVM必须在方法区中保存类型的所有域的相关信息以及域的声明顺序
            2. 域的相关信息包含：
                * 域名
                * 域类型
                * 域访问修饰符(public,private,protected,static,final,volatile,transient的某个子集)
        7. 方法(Method)信息
            1. JVM必须在方法区中保存所有方法的一下信息，同样包括声明顺序
                * 方法名
                * 方法的返回类型(如void)
                * 方法参数的数量和类型(有序的)
                * 方法的访问修饰符(public,private,protected,static,final,synchronized,native,abstract的子集)，
                除了native和abstract外，其他方法还保存方法的字节码(btyecodes)操作数栈和方法栈帧的局部变量的大小
                * 异常表
        8. **除了常量外的所有static变量(类变量)，因为静态变量和类关联在一起，随着类的加载而存储在方法区中(而非存储在堆中)**
        9. **static final修饰的成员变量存储与方法区中**
        10. 八种基本类型(int,byte,float,long,float,boolean,char,double)的**静态变量会在方法区开辟空间，并将对应的值存在方法区中**，
        对于引用类型的静态变量如果未用 `new` 关键字去引用类型的静态变量分配对象(如 `static Object obj;`)，那么对象的引用obj会存储在方法区中，
        并为其指定默认值 `null` ，若引用类型使用了 `new` 关键字为静态变量分配了实例对象(如：`static Object obj=new Object();`)，
        那么对象的引用obj存在方法区中，并且该对象在堆中的地址也会一并存在方法区中(**注意此时静态变量只是存储的实例对象在堆中的地址，
        实例对象本身还是存在堆中**)
        11. 程序运行时会**加载类编译成的字节码**，这个过程中**静态变量**和**静态方法**及**普通方法**对应的字节码被加载到方法区。
        12. 但是_**方法区中不存实例变量的**_，**这是因为类加载先于类实例的产生，而实例变量和变量关联在一起，
        没有对象就不存在实例变量，类加载时只有class对象，所以方法区中没有实例变量。**
        13. **静态变量**和**静态方法**在方法区存储方式是有区别的。
    2. 方法区再JDK1.8之前是堆上的逻辑的一部分，但是它有一个别名"非堆"，目的是与堆区分开来；在JDK1.8(包含1.8)之后，方法区被从堆中一处，
    两者不再使用同一块内存区域，方法区被分离出来之后被称为**元数据区(Meta-Space)**，**元数据区使用直接内存**
    3. 方法区也被称为**永久代**，是因为GC在此区域的回收效率极低，是因为方法区都是存储的跟class对象相关的信息，
    而方法区中大部分都是由Bootstrap ClassLoader、ExtClassLoader和AppClassLoader加载的，由JVM自带的类加载器加载的类，JVM是不会去卸载的
    4. 该区域的内存回收目标主要针对常量池的回收和类型的卸载
    5. 当方法区无法满足内粗分配需求时，将会抛出OOM错误
6. 运行时常量池(Runtime Constant Pool)
    1. 运行时常量池是方法区的一部分，主要存放class文件中记录的常量池(Constant Pool)，用于存储编译期生成的各种**字面量和符号引用**，
    这部分内容在类被加载时进入方法区的运行时常量池中存放
    2. 当常量池无法再申请到内存时将会抛出OOM错误
7. 直接内存(堆外内存)
    1. 当申请的内存大于实际内存时，将会抛出OOM错误
    2. 它将通过使用native函数库来直接分配堆外内存，需要手动释放内存(Unsafe)
    3. 这部分内存**不受GC管理**，效率比较高，但容易出错。
    4. JVM通过堆上的DirectByteBuffer来操作部分内存
    ```java
    /**
      * 实例类来说明类中的各个元素所存放的内存区域
      */
    public class  PersonDemo
    {
        public static void main(String[] args)
        {   //局部变量p和形参args都在main方法的栈帧中
            //new Person()对象在堆中分配空间
            Person p = new Person("zhengwei",18);
            //sum在栈中，new int[10]在堆中分配空间
            int[] sum = new int[10];
        }
    }
    class Person
    {   //实例变量name和age在堆(Heap)中分配空间
        private String name;
        private int age;
        //类变量(引用类型)name1和"cn"都在方法区(Method Area)
        private static String name1 = "cn";
        //类变量(引用类型)name2在方法区(Method Area)
        //new String("cn")对象在堆(Heap)中分配空间
        private static String name2 = new String("cn");
        //num在堆中，new int[10]也在堆中
        private int[] num = new int[10];
        Person(String name,int age)
        {   
            //this及形参name、age在构造方法被调用时
            //会在构造方法的栈帧中开辟空间
            this.name = name;
            this.age = age;
        }
        //setName()方法在方法区中
        public void setName(String name)
        {
            this.name = name;
        }
        //speak()方法在方法区中
        public void speak(){
            System.out.println(this.name+"..."+this.age);
        }
        //showCountry()方法在方法区中
        public static void  showName(){
            System.out.println("name="+this.name);
        }
    }
    ```
## 五、JIT(Just In Time)
1. 什么是JIT？
    * 为了提高热点代码的执行效率，在运行时，JVM会把这些代码编译成跟本地平台相关的机器码，并进行各种层次的优化，
    完成这个任务的编译器就是即时编译器(Just-In_Time)，简称JIT编译器
2. 什么时热点代码？
    * 当虚拟机发现某个方法或代码块的运行特别频繁时，就会把这段代码认定为**热点代码**
    * 热点代码的分类
        1. 被多次调用的方法：一个方法被调用的多了，方法体内的代码执行的次数自然就变多了，称为热点代码也就理所应当了
        2. 被多次调用的循环体：一个方法只被调用一侧或者少量的几次，但是方法体内部存在循环次数较多的循环体，
        这样循环体的代码也被重复执行了很多次，因此这些代码也可以认为是**热点代码**
    * 热点代码是如何检测的
        1. 基于采样的热点检测：这种方式时，虚拟机会定期的检查各个线程的栈顶，如果发现某个(某些)方法经常出现在栈顶，
        那么就认为这个方法时热点代码
            * 优点：实现简单高效，容易获取方法调用关系(将调用堆栈展开即可)
            * 缺点：不精确，容易受到因线程阻塞或别的外界因素而扰乱热点探测
        2. 基于计数器的热点探测：采用这种方式时，虚拟机会为每个方法(甚至代码块)建立计数器，统计方法的执行次数，
        如果调用的次数达到一定的阈值之后就认为该代码时热点代码
            * 优点：统计结果严谨
            * 缺点：实现麻烦，需要为每个方法建立计数器，不能直接获取方法之间的调用关系
        3. 计数器种类(这两种共同协作)
            * 方法调用计数器：这个计数器用于记录方法被调用的次数
            * 回边计数器：统计一个方法中循环体代码执行的次数
3. 什么是编译和解释
    1. 编译器：把源程序的每条语句都编译成机器码，并保存成二进制文件，这样运行时k可以直接一机器语言来运行此程序，速度快
    2. 解释器：只在执行时才一条一条的将源程序语句解释成机器码来给计算机执行，速度比编译后的代码慢
    3. 字节码并不是机器语言，想要让机器执行，需要JVM把字节码翻译成机器码，这个过程叫做编译，是更深层次的编译(实际上就是解释，引入JIT之后也就存在编译了)
    4. Java需要把字节码逐条翻译成机器码并且执行，这是传统的JVM的解释器功能，正是逐条翻译的效率太低，引入JIT即时编译技术，以提高效率
    5. 必须指出的是，不管是解释执行还是编译执行，最终执行的代码都是可直接在真实机器上运行的**机器码**即**本地代码**
4. 为何Hotspot虚拟机要使用解释器和编译器共存的架构
    1. 解释器和编译器各有优势
        1. 解释器：当程序需要迅速启动和执行的时候，解释器可以首先发挥作用，省去编译的时间，立即执行
        2. 编译器：当程序运行后，随着时间的推移，编译器逐渐发挥作用，把越来越多的代码编译成本地代码，可以有更高的执行效率
        3. 两者的协作：在程序运行环境中内存资源限制较大时，可以使用解释器执行节约内存，反之可以使用编译执行来提高效率。
        当通过编译优化后，如果发现没有起到优化作用，可以通逆优化退回到解释状态继续执行
    2. 即时编译器与Java虚拟机的关系
        即时编译器并不是虚拟机的必需部分，Java虚拟机规范中并没有规定JVM内必须要有即时编译器的存在，更没有限定和指导即时编译器如何去实现
        但是，即时编译器性能的好坏、代码优化程度的高低却是衡量一个商用虚拟机的重要标准，它是虚拟机中最核心且最能体现出虚拟机技术水平的的部分
    3. 即时编译器的分类，在Hotspot虚拟机中默认采用一个解释器和其中一个编译器直接配合完成工作
        1. Client Compiler-C1编译器：更快的编译速度
        2. Server Compiler-C2编译器：更好的编译质量
    4. 分层编译：由于编译器编译本地代码需要占用程序运行时间，要编译出优化程度更高的代码可能需要很多时间，而且想要编译出高效的代码，
    解释器还可能需要替编译器收集性能监控信息，这对解释器的运行速度是有影响的
        1. 第0层：程序解释执行，解释器不开启性能监控，可触发第1层编译
        2. 第1层：也称C1编译，将字节码编译成本地代码，进行简单可靠的优化，如有必要加入性能监控逻辑
        3. 第2层：也称C2编译，也是将字节码编译成本地代码，但是会启用一些编译耗时较长的优化，甚至会根据性能监控信息进行一些不可靠的激进的优化
    5. 编译优化技术
        1. 语言无关的经典优化技术之一：公共子表达式消除
            * 如果一个表达式 E 已经计算过了，并且从先前的计算到现在 E 中所有变量的值都没有发生变化，
            那么 E 的这次出现就成为了公共子表达式。对于这种表达式，没必要花时间再对它进行计算，
            只需要直接使用前面计算过的表达式结果代替 E 就可以了。
            * 例子 `int d = (c * b) * 12 + a + (a+ b * c) -> int d = E * 12 + a + (a + E)`
        2. 语言相关的经典优化技术之一：数组范围检查消除
            * 在 Java 语言中访问数组元素的时候系统将会自动进行上下界的范围检查，超出边界会抛出异常。对于虚拟机的执行子系统来说，
            每次数组元素的读写都带有一次隐含的条件判定操作，对于拥有大量数组访问的程序代码，这无疑是一种性能负担。
            Java 在编译期根据数据流分析可以判定范围进而消除上下界检查，节省多次的条件判断操作。
        3. 最重要的优化技术之一：方法内联
            * 简单的理解为把目标方法的代码“复制”到发起调用的方法中，消除一些无用的代码。只是实际的 JVM 中的内联过程很复杂，在此不分析。
        4. 最前沿的优化技术之一：逃逸分析
            * 逃逸分析的基本行为就是分析对象动态作用域：当一个对象在方法中被定义后，它可能被外部方法所引用，
            例如作为调用参数传递到其他方法中，称为方法逃逸。甚至可能被外部线程访问到，譬如赋值给类变量或可以在其他线程中访问的实例变量，
            称为线程逃逸。
                * 全局变量赋值逃逸
                * 方法返回值逃逸
                * 实例引用发生逃逸
                * 线程逃逸:赋值给类变量或可以在其他线程中访问的实例变量.
                ```java
                public class EscapeAnalysis {
                	/**
                	 * -Xmx4G -Xms4G -XX:-DoEscapeAnalysis -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError
                	 * 1000000个user对象全部在堆中分配
                	 *
                	 * -Xmx4G -Xms4G -XX:+DoEscapeAnalysis -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError
                	 * 84536个user对象在堆上分配，其余对象将在栈上分配，创建1000000个对象的速度也比在对上创建的速度要快
                	 */
                	public static void main(String[] args) {
                		long a1 = System.currentTimeMillis();
                		for (int i = 0; i < 1000000; i++) {
                			alloc();
                		}
                		// 查看执行时间
                		long a2 = System.currentTimeMillis();
                		System.out.println("cost " + (a2 - a1) + " ms");
                		// 为了方便查看堆内存中对象个数，线程sleep
                		try {
                			Thread.sleep(100000);
                		} catch (InterruptedException e1) {
                			e1.printStackTrace();
                		}
                	}
                	private static void alloc() {
                		User user = new User();
                	}
                
                	private static class User {
                
                	}
                }
                ```
            * 栈上分配：将不会逃逸的局部对象分配到栈上，那对象就会随着方法的结束而自动销毁，减少垃圾收集系统的压力。
                * 通过 `-XX:-/+DoEscapeAnalysis` 关闭/开启逃逸分析
                * 我们通过JVM内存分配可以知道JAVA中的对象都是在堆上进行分配，当对象没有被引用的时候，需要依靠GC进行回收内存，
                如果对象数量较多的时候，会给GC带来较大压力，也间接影响了应用的性能。为了减少临时对象在堆内分配的数量，
                JVM通过逃逸分析确定该对象不会被外部访问。那就通过标量替换将该对象分解在栈上分配内存，
                这样该对象所占用的内存空间就可以随栈帧出栈而销毁，就减轻了垃圾回收的压力。
                ```java
                public class EscapeAnalysis {
                	/**
                	 * -Xmx4G -Xms4G -XX:-DoEscapeAnalysis -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError
                	 * 1000000个user对象全部在堆中分配
                	 *
                	 * -Xmx4G -Xms4G -XX:+DoEscapeAnalysis -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError
                	 * 84536个user对象在堆上分配，其余对象将在栈上分配，创建1000000个对象的速度也比在对上创建的速度要快
                	 */
                	public static void main(String[] args) {
                		long a1 = System.currentTimeMillis();
                		for (int i = 0; i < 1000000; i++) {
                			alloc();
                		}
                		// 查看执行时间
                		long a2 = System.currentTimeMillis();
                		System.out.println("cost " + (a2 - a1) + " ms");
                		// 为了方便查看堆内存中对象个数，线程sleep
                		try {
                			Thread.sleep(100000);
                		} catch (InterruptedException e1) {
                			e1.printStackTrace();
                		}
                	}
                	private static void alloc() {
                		User user = new User();
                	}
                
                	private static class User {
                
                	}
                }
                ```
            * 同步消除：如果该变量不会发生线程逃逸，也就是无法被其他线程访问，那么对这个变量的读写就不存在竞争，
            可以将同步措施消除掉（同步是需要付出代价的），如果你定义的类的方法上有同步锁，但在运行时，却只有一个线程在访问，
            此时逃逸分析后的机器码，会去掉同步锁运行，这就是没有出现线程逃逸的情况。
            那该对象的读写就不会存在资源的竞争，不存在资源的竞争，则可以消除对该对象的同步锁。
                * 通过 `-XX:+EliminateLocks` 可以开启同步消除,进行测试执行的效率
                ```java
                 public String createString(String ... values){
                     StringBuffer stringBuffer = new StringBuffer(); 
                     for (String string : values) {
                         stringBuffer.append(string+" ");
                    }
                     return stringBuffer.toString();
                 }   
                public static void main(String[] args) {
                    long start = System.currentTimeMillis();
                    EscapeAnalysis escapeAnalysis = new EscapeAnalysis();
                    for (int i = 0; i < 1000000; i++) {
                        escapeAnalysis.createString("Escape", "Hello");
                    }
                    long bufferCost = System.currentTimeMillis() - start;
                    System.out.println("craeteString: " + bufferCost + " ms");
                }
                ```
            * 标量替换：标量是指无法在分解的数据类型，比如原始数据类型以及reference类型。而聚合量就是可继续分解的，比如 Java 中的对象。
            标量替换如果一个对象不会被外部访问，并且对象可以被拆散的话，真正执行时可能不创建这个对象，
            而是直接创建它的若干个被这个方法使用到的成员变量来代替。
            这种方式不仅可以让对象的成员变量在栈上分配和读写，还可以为后后续进一步的优化手段创建条件。
                * 通过 `-XX:+EliminateAllocations` 可以开启标量替换， 
                `-XX:+PrintEliminateAllocations` 查看标量替换情况（Server VM 非Product版本支持）
## 六、JVM常用命令
### jps(java process server)
查看正在运行的JVM的进程
#### usage
jps [-q] [-mlvV] [<hostid>]  
* jps -q:只显示进程号
* jps -l:显示进程号和main运行的全限定名
* jps -m:显示传入main方法的参数
* jps -v:显示JVM的启动参数
* jps -V:显示进程号和main方法的缩写，所用与jps相同
### jmap
查看JVM的内存情况
#### usage
jmap [option] <pid>  
* jmap:查看Java调用的组件所占用的内存情况
* jmap -histo[:live] pid:查看JVM中类加载到内存的个数和占用的内存
* jmap -dump pid:查看JVM中的heap的使用情况
* jmap -clstats pid:查看JVM的类加载器情况
### jstack
查看JVM中的栈信息，可以找到程序中的死锁信息，若程序中有死锁的话，那么将会有提示。
#### usage
jstack -F [-m] [-l] <pid>
* jstack -l pid:列出所有线程情况
### jhat
装载hrpof文件并分析dump文件，然后在本地启动一个服务器，供本地浏览器访问
### jvisualvm
图形化JVM分析工具，里面有heap、stack、memory、deadlock……等信息。
### jmc(java mission control)
图形化JVM分析工具，可以实时监控JVM的运行情况。
## 七、GC
### GC算法
#### 引用技术算法(reference counting)
* 给对象添加一个引用计数器，当一个地方引用他时计数器加一，当引用失效后，计数器减一，任何时刻计数器为0的对象就是不可能再被使用的对象。
* 引用计数器无法解决对象循环引用的问题。
#### 根搜索算法(GC Root Tracing)
* Java和C#都是采用此算法进行垃圾回收
* 基本思想就是：通过一些列的GC Roots的点作为起始点，进行向下搜索，当一个对象到GC Roots没有任何引用链时，则证明此对象是不可用的。
##### GC Roots
* 在JVM stack(栈中的本地变量)中的引用
* 方法区的静态引用
* JNI(即一般说的native方法)中的引用
##### 枚举根节点
当执行系统停顿下来时，并不需要一个不漏的检查完所有执行上下文和全局的引用位置，虚拟机应当有办法直接得知哪些地方存放着对象的引用。
在HotSpot虚拟机中使用OopMap数据结构来达到这个目的
#### 安全点(Safe Point)
* 在OopMap的协助下，HotSpot虚拟机可以快速且准确的完成GC Roots的枚举，但一个很现实的问题随之而来：可能导致引用变化，
或者说OopMap内容变化的指令非常多，如果每一条指令都生成对应的OopMap，那将会需要大量的额外空间，这样GC的空间成本将会变高
* 实际上，HotSpot虚拟机并没有为每条指令都生成OopMap，而是只在 "特定的位置" 记录这些信息，这些位置被称为**安全点(safe point)**，
即程序执行时并非能在所有地方停下来进行GC，只有到达安全点时才能暂停。
* safe point的选定既不能太少以至于让GC等待太长时间，也不能过于频繁以至于加大运行时的负载。
所以，安全点的选定基本上是以**是否让程序长时间运行的特征**为标准选定的——因为每条指令执行时间很短，
程序不太可能因为指令流太长这个原因而长时间运行，长时间运行的最显著特征就是指令序列复用，例如**方法调用、循环跳转、异常**等，
所以具有这些功能的指令才会产生safe point
* 对于safe point，另一个需要考虑的问题就是如何在GC发生时让所有线程(这里不包括JNI调用的线程)都**跑**到最近的安全点上再停顿下来，
JVM有两种方案：抢占式中断(Preemptive Suspension)和主动式终端(Voluntary Suspension)
    * 抢占式中断：它不需要线程的执行代码主动去配合，在GC发生时，首先把所有线程中断，如果有的线程中断的地方不是安全点，就恢复线程，
    让它运行到安全点
    * 主动式中断：当GC需要中断线程时，不直接对线程操作，仅仅简单的设置一个标志位，各个线程执行时，主动轮询该标志位，
    发现标志位为真则自己主动挂起。轮询标志位的地方和安全点重合，创建对象需要内存的地方也需要轮询，因为GC一般在创建对象时发生
    * 现在JVM都采用主动式中断来响应GC事件。
#### 安全区域(safe region)
* 在使用safe point似乎已经完美的解决了如何进入GC的问题，但实际上情况并不一定。safe point机制保证了程序执行时，
在不太长的时间就会遇到可进入GC的safe point。但如果程序**不执行**呢？所谓的不执行就是程序没有分配CPU时间，典
型的例子就是处于sleep状态和blocked状态的线程，这时候线程无法响应JVM的中断请求，JVM也显然不太可能等待线程重新分配CPU时间。
对于这种情况，就需要安全区域(safe region)来解决
* 在线程执行到安全区域的代码时，首先标识自己进入了安全区域，那样当JVM在这段时间内需要进行GC时，就不用管标识自己进入安全区域的线程了。
在线程要离开安全区域时，他要检查JVM是否已经完成了根节点枚举(或者是整个GC过程)，如果完成了，线程继续执行，
否则它将等待直到收到可以安全离开safe point的信号为止。
#### 对于方法区的回收
* Java虚拟机规范没有要求JVM在方法区实现GC，方法区的GC的性价比一般比较低
* 在heap中，尤其是新生代，常规引用进行一次GC一般可以回收70%~95%的空间，而方法区的GC效率远低于这个比例
* 当前商业的JVM都有实现方法区的GC
* 主要回收两个部分：废弃常量和无用类
* 类回收需要满足如下三个条件：
    1. 该类所有实例均已被回收，也就是在JVM中不存在该class的实例
    2. 加载该类的ClassLoader也被回收了
    3. 该类对应的java.lang.Class对象没有在任何地方被引用，如不能在任何地方通过反射的方式进行方法引用
* 在大量使用反射、动态代理、CGLib等字节码框架，动态生成JSP以及OSGi这类平凡自定义ClassLoader的场景需要JVM具备类卸载的支持以保证方法区不会溢出
#### JVM常见GC算法
##### 标记清除算法(mark-sweep)
* 算法分为 "标记" 和 "清除" 两个阶段，首先标记出所有需要回收的对象，然后回收所有需要回收的对象
* 缺点：
    1. 效率问题，标记和清理两个过程效率都不高，需要扫描所有对象，堆越大，GC越慢
    2. 空间问题，标记清理之后会产生大量不连续的内存碎片，GC的次数越多，碎片越严重，
    内存碎片太多可能会导致后续使用中无法找到足够的连续的内存空间而提前触发一次GC动作。
##### 标记整理算法(mark-compact)
* 标记过程仍然一样，但后续步骤不是进行直接清理，而是令所有存活的对象向一端移动，然后直接清理掉这端边界以外的内存
* 不会产生内存碎片
* 比mark-sweep更耗时，更多的耗时在于进行整理(compact)内存
##### 复制算法(copying)
* 将可用内存划分为两块，每次只使用其中的一块内存，当半区内存使用完毕时，仅将还存活的对象复制到另一块内存上，
然后将原来半区的内存中的对象全部清除
* 这样使得每次内存回收都是对整个半区进行回收，内存分配时也不需要考虑内存碎片化等复杂问题，只要移动堆顶指针，按顺序分配内存就可以了，
实现简单，运行高效。
* 这种算法的代价就是将内存缩小为原来的一半，代价高昂。
* 现在的商业虚拟机都是使用复制算法来进行垃圾回收新生代
* 将内存划分成一块较大的Eden区和两块较小的survivor区，每次使用Eden和其中一块survivor，当回收进行时，
将Eden区和其中一块survivor区中的存活对象一次性全部拷贝到另外一块survivor，然后清理掉Eden和用过的survivor区
* Oracle Hotspot虚拟机默认的Eden和survivor的大小比例为8:1，也就是每次只有10%内存空间浪费了。
* 复制算法在对象存活率高的时候效率有所下降
* 如果不想浪费50%的空间，就需要有额外的空间进行分配担保用于应付半区的内存中所有对象都100%存活的极端情况，所以在老年代不能直接选用复制算法。
##### 分代算法(generation)
* 当前商业的虚拟机都是采用的分代收集，根据对象不同的存活周期将内存划分为几块
* 一般把Java堆分为新生代和老年代，这样可以根据各个年代的特点采用最适合的手机算法
    1. 新生代每次GC都有大批对象死去，只有少量存活，那就选用复制算法。
    2. 老年代中的对象存活时间大都比较长，采用标记整理算法或这标记清理算法比较合适。
##### 新生代(young generation)
* 对象都在新生代进行创建，新生代使用复制算法进行GC(理论上，新生代的生命周期非常短，所以适合复制算法)
* 新生代分为3个区(各个区的内存比例是可以通过参数调整的)
    1. Eden区：占新生代80%的内存空间，对象在此生成，
    2. 两个survivor区：占新生代20%空间，每个survivor各自占10%，两个survivor完全对称，轮流替换。
##### 老年代(old generation)
* 存放经过一次或多次GC还存活的对象
* 一般采用mark-sweep或mark-compact算法进行GC
* 有多种垃圾收集器可以选择。每种垃圾收集器可以看作是一个GC算法的实现。可以根据具体应用的需求选用合适的垃圾收集器(吞吐量或响应时间)
##### ~~永久代~~
~~在Java8之前存在，在Java8之后被元空间取代~~
~~* 并不属于堆，被称为非堆，但是GC会涉及这个区域~~
~~* 存放Class的结构信息，包括常量池、字段描述、方法描述。与垃圾收集要回收的Java对象关系不大~~
### 内存分配
1. 堆上分配
大多数情况在Eden区分配，偶尔会直接在old区分配，具体实现取决于GC
2. 栈上分配
原子类型局部变量
### 内存回收
* GC要做的就是将dead的对象进行回收，释放内存
* Oracle的HotSpot虚拟机认为没有被引用的对象就是dead的对象
* HotSpot虚拟机将对象的引用分为四种： 
    1. strong： `Object o = new Object()` 就是强引用
    2. soft：继承于Reference类型，在GC时，内存不够时会将弱引用对象回收掉；长时间不用的对象也会被回收掉
    3. weak：继承于Reference类型，一定会被GC，当对象被mark为dead时，会在ReferenceQueue中进行通知
    4. phantom：继承于Reference类型，本来就没有引用，当从jvm heap中释放时会通知
#### 内存空间担保
在发生minor gc之前，虚拟机将会检查来年代最大可用的连续空间是否大于新生代所有对象空间总和，如果这个前提成立，那么minor gc将确保是安全的。
当大量对象在minor gc之后仍是存活的，就需要老年代进行空间分配担保，把survivor无法容纳的对象直接晋升到老年代。
如果老年代判断剩余空间不足(根据以往每回收晋升到老年代对象容量的平均值作为经验值)，则进行一次full gc。
### GC的时机
在分代模型的基础上，GC从时机上分为两种：Scavenge GC和Full GC
#### Scavenge GC(Minor GC)
* 触发时机：新对象生成时，Eden区满了
* 理论上Eden区大多数对象都会在Scavenge GC进行回收，复制算法的执行效率会很高，Scavenge GC的时间会比较短
#### Full GC(Major GC)
* 堆整个JVM进行整理，包括young、old和perm
* 主要触发时机：
    1. old满了
    2. perm满了
    3. System.gc()
* 效率低下，尽量避免
### 垃圾回收器(Garbage Collector)
* 分代模型：GC的宏观愿景
* 垃圾回收器：GC的具体实现
* HotSpot提供多种垃圾回收器，我们需要根据具体应用来选择合适的垃圾回收器
* 没有万能的收集器，每种垃圾收集器都有自己的适用场景
#### 并行与并发
* 并行(parallel)：是指多个垃圾收集线程同时进行工作，但是用户线程处于等待状态
* 并发(concurrent)：是指垃圾收集线程与用户线程同时进行工作
    * 注意：并发并不代表解决了GC的停顿问题(stop the world)，在关键的步骤还是要停顿的。
    比如在收集器标记垃圾时。但在清理垃圾的时候用户线程可以和收集器线程同时工作
#### Serial收集器(young)
* 单线程收集器，收集时会暂停所有工作线程(Stop The World，STW)，使用复制算法，当虚拟机运行在Client模式下时，新生代默认采用Serial收集器
* 在新生代和老年代都可以使用
* 是新生代中收集器，使用复制算法
* 因为是单线程，没有多线程切换的额外开销，简单实用
* HotSpot Client缺省的新生代收集器
#### ParNew收集器(young)
* ParNew收集器就是Serial收集器的多线程版本，即并行收集器(暂停业务线程，多线程进行垃圾回收)，除了使用多线程收集外，
其余行为(算法、STW、对象分配规则和回收策略)均与Serial收集器一致。
* 对应的这种收集器是虚拟机运行在Server模式下的默认新生代收集器，**在单CPU情况下，ParNew收集器并不会比Serial收集器有更好的效果**
* 只有在多CPU情况下，ParNew收集器的效率比Serial收集器的效率高。
* 可以通过 `-XX:ParallelGCThreads` 来控制GC的线程数量。需要集合具体的CPU的核心数来制定
* HotSpot Server缺省的新生代收集器是ParNew收集器
#### Parallel Scavenge收集器(young)
Parallel Scavenge收集器也是一个多线程收集器，也是使用复制算法，即并行收集器(暂停业务线程，多线程进行垃圾回收)，
但它的对象分配规则与回收策略都与ParNew收集器有所不同，它是以吞吐量最大化(即GC时间占总运行时间最小)为目标的收集器实现。
它允许较长时间的STW换取总吞吐量最大话。
#### Serial Old收集器(old)
* 和Serial一样是单线程收集器，是老年代收集器，使用标记整理算法
#### Parallel Old收集器(old)
* 老年代版本吞吐量优先的收集器，使用多线程和和标记整理算法，JVM1.6提供，在此之前，如果新生代使用了Parallel Scavenge收集器的话，
老年代除Serial Old外别无选择，因为Parallel Scavenge无法与CMS收集器一起工作
* Parallel Scavenge + Parallel Old = 高吞吐量，但GC停顿时间可能不理想
#### CMS(Concurrent Mark Sweep)收集器(old)
* CMS是一种以最短停顿时间为目标的收集器，使用CMS并不能达到GC效率最高(总体GC时间最少)，
但它能尽可能降低GC时服务停顿时间，CMS使用**标记－清除算法**
* 使用 `-XX:UseConcMarkSweepGC` 开启
* 只有在多CPU下使用才有意义
* 只针对老年代，一般结合ParNew收集器使用
* Concurrent，GC线程和用户程序并发工作(尽量并发)
##### CMS清理步骤
1. 初始标记(initial mark)
    * 需要暂停业务线程(Stop The World)，速度很快
    * 标记那些被GC Roots引用和新生代存活对象所引用的所有老年代对象
2. 并发标记(concurrent mark)
    * 沿着初始标记标记处的GC Roots能够关联到的对象进行GC Roots Tracing的过程
    * 不暂停业务线程，和业务线程一起执行
    * CMS会遍历老年代，然后标记所有存活对象，它会根据上个阶段找到的Object Roots遍历查找。
    * 并不是老年代所有的存活对象都会被标记，因为在标记阶段用户程序可能会改变一些引用(所以需要重新标记这个步骤)
3. 并发预清理(concurrent pre-clean)
    * 这是一个并发过程，不需要暂停业务线程。
    * 在和业务线程并发运行时，一些引用的对象可能会发生改变，
    在这种情况发生时，JVM会将这个对象的区域(Card)标记为dirty，也就是card marking
    * 在pre-clean阶段，那些能够从dirty对象到达的对象也会被标记，这个标记做完之后，dirty card标记就会被清除
4. 并发可失败的预清理(concurrent abortable pre-clean)
    * 这是一个并发过程，不会暂停业务线程
    * 这阶段是为了尽量承担STW中最终标记阶段的工作。
    * 这个阶段持续时间依赖于很多因素，由于这个阶段是在重复做很多相同的工作，直接满足一些条件
5. 重新标记(remark)
    * 需要暂停业务线程(Stop The World)
    * 为了修正在并发标记期间用户程序继续运行而导致标记产生变动的那一部分对象的标记记录
    * 此阶段的停顿时间一般会比初始标记阶段稍长一点，但远比并发标记的时间短
6. 并发清除(concurrent sweep)
    * 不暂停业务线程，和业务线程一起执行
    * 清除不使用的对象，回收内存空间
7. 并发重置(concurrent reset)   
    * 并发执行，重置CMS内部数据结构，为下次GC做准备
##### 优点
* 并发收集，停顿低
* 将标记阶段细分为更多的步骤，尽可能地降低STW的时间
##### 缺点
* CMS以牺牲CPU资源为代价来减少用户线程的停顿。当CPU个数少于4个时，有可能对吞吐量影响很大
* CMS在并发清理过程中，用户线程还在跑，这时需要预留一部分空间给用户线程
* CMS使用mark-sweep算法来进行垃圾回收，会带来碎片化问题。碎片过多会造成Full GC的发生
* 对CPU资源敏感
* 无法处理浮动垃圾(floating garbage)，可能出现 "concurrent mode failure" 失败而导致另外一次full gc的产生。
如果在应用中，老年代增长速度不快，可以适当调高 `-XX:CMSInitiatingOccupancyFraction` 的值来提高触发GC百分比，
以便降低内存回收次数从而获得更好的性能。要是CMS运行期间预留的内存无法满足程序需要时，
虚拟机将启动预备方案：临时启动Serial Old收集器进行老年代垃圾收集，这样会加长停顿时间
* 因为采用的是标记-清理算法，那么在收集结束后将出现大量的内存碎片，内存碎片越多，会给大对象分配内存带来很大麻烦，
往往出现在老年代还有很大内存空间剩余，但无法找到足够大的连续的内存空间来分配当前对象，不得不提前进行一个full gc。
CMS收集器提供 `-XX:UseCMSCompactAtFullCollaction` 开关参数(默认开启)，用于在CMS收集器顶不住要进行full gc时开启内存整理工作，
内存整理无法并发，内存碎片问题没有了，停顿是将会变长。
#### G1(Garbage First Collector)(young & old)
##### 吞吐量
吞吐量关注的是，在一个指定的时间内，最大化一个应用的工作量
1. 如何判断一个系统的吞吐量？
    * 在单位时间内，同一个事务(任务，请求)完成的次数(tps)
    * 单位时间内，进行查询的次数(qps)
2. 对于关注吞吐量的系统，卡顿是可以接收的，因为系统关注的是长时间的大量任务的执行能力，单次快速响应并不值得考虑
##### 响应能力
1. 响应能力指一个系统或程序对请求是否能够及时响应，比如：
    * 桌面UI响应鼠标点击事件
    * 一个网站能够多快的响应页面请求
    * 数据库能够多快的返回查询结果
2. 对于响应能力敏感的场景，长时间的停顿是无法接收的
##### 简介
* G1是一个面向服务端(HotSpot Server Mode)的垃圾收集器，适用于多核处理器、大内存容量的服务端系统
* 满足短时间GC停顿的同时达到一个较高的吞吐量
* Java7以上版本使用
##### 设计目标
* 与应用线程同时工作，几乎不需要STW(与CMS类似)
* 整理空间碎片，不产生内存碎片，G1采用的是复制算法(CMS采用mark-sweep算法，CMS只能在STW时去整理内存碎片)
* GC停顿更加可控，自己可以指定GC停顿事件，G1进行收集垃圾时会参考指定的停顿时间
* 不牺牲系统的吞吐量
* GC不要求额外的内存空间(CMS需要额外的内存空间来存储**浮动垃圾**)
#####  内存堆布局
* heap被划分一个个相等的不连续的内存区域(region)，每个region都有一个分代角色：eden,survivor,old
* 对于每个角色的数量并没有强制的限定，也就是说每种分代内存的打小可以动态变化
* G1的最大的特点就是高效回收，优先去执行那些大量对象可回收的区域(region)
* G1使用gc停顿可预测模型，来满足用户设定的gc停顿时间，根据用户设定的目标时间，G1会自动选择哪些region要清除，一次清理多少个region
* G1从多个region中复制存活对象，然后集中放到一个region中，同时整理、清理内存(copying算法)
##### 重要概念
* 分区(region)：G1采用 **将heap分成大小相等的分区(region)** 策略来解决并行、串行和CMS收集器的碎片化、暂停时间不可控等问题
    * 每个分区都可能是新生代或老年代，但在同一时刻，一个分区只能属于某一个特定的代。新生代、幸存代和老年代的概念还存在，
    **成为逻辑上的概念**，这样方便复用之前分代框架的逻辑
    * 每个代在物理上不连续，则带来了额外的好处——有的分区垃圾很多，有的分区垃圾很少，**G1会优先收集垃圾对象很多的区域(region)**，
    这样可以花费较少的时间来回收较多的垃圾，这也是G1名字的由来，即优先收集垃圾最多的分区
* 依然在新生代满了的时候，对整个新生代进行垃圾回收——整个新生代中的对象要么被晋升要么被回收，
至于新生代也采取分区的机制的原因是：跟老年代策略统一，方便调整代的大小
* G1还是一种带压缩的收集器，在回收老年代的分区时，是将一个分区拷贝到另一个可用分区，这个拷贝的过程实现了局部压缩
* CSet(收集集合，collect set)：一组可被回收的分区集合。在CSet中存活的数据会在GC过程中被移动到另一个可用分区，
CSet中的分区可以来自eden区，survivor区或old区
* RSet(已记忆集合，remember set)：记录了其他region中的对象引用本region中对象的关系，属于points-into结构(谁引用了我的对象)。
RSet的价值在于使得垃圾收集器不需要扫描整个堆找到谁引用了当前分区中的对象，只需要扫描RSet即可。**每个分区拥有一个RSet**
    * G1 GC是在points-out的card table之上再加了一层结构来构成points-into RSet：
    每个region都会记录下到底哪些别的region有指向自己的指针，而这些指针分别在哪些card范围内。
    * 这个RSet其实一个hash table，key是别的region的起始地址，value是一个集合，里面的元素是card table的index。
    e.g.如果regionA的RSet有一项的key是regionB，value里面有index为1234的card，他的意思就是regionB的一个card里有指向regionA。
    所以对于regionA来说，该RSet记录的是points-into的关系；而card table任然记录了points-out的关系。
* snapshot-at-the-beginning(SATB)：是G1在并发标记阶段使用的增量式标记算法。并发标记是并发多线程的，但并发线程在同一时刻只扫描一个区域
* Humongous区域：如果一个对象占据的空间超过一个region的50%以上，G1认为这是一个巨型区域，**默认会将其直接分配到老年代**，
但如果它是一个短期存在的一个巨型对象的话，就会对垃圾收集器产生负面影响。为了解决这一现象，G1划分humongous区域专门用来存储大对象。
如果一个humongous存不下一个大对象，那么G1将会寻找连续的H区来存储。**为了能够找到足够大的连续的H区，有时将不得不启动一次full gc**
##### G1相比于CMS的优势
* 因为G1将内存分成大小相等的区域(region)并采用复制算法，所以不产生内存碎片；
而CMS采用的是标记清理算法，必然会产生内存碎片，只有在STW时才会整理内存
* Eden、Survivor和Old的大小不在固定，是可以动态变化的，提高了内存的使用率；而CMS采用固定大小的分代大小
* G1可以通过设置预期停顿时间(pause time)来控制垃圾收集时间，G1会参考预期停顿时间来收集部分region，而不是去回收整个堆；
其他的垃圾收集器则会收集整个内存区域
* G1可以在新生代和老年代使用，而CMS只可以在老年去使用
##### G1适用的场景
* 服务器多核CPU、JVM较大内存的应用
* 应用在运行时产生大量内存碎片，需要经常压缩空间
* 想要更加可控的、可预期的GC停顿周期；防止高并发下的应用雪崩现象
##### G1的垃圾收集模式
G1提供两种收集模式，young gc和mixed gc，两种都需要STW(Stop The World)
* young gc：选定**所有新生代**region，通过控制新生代的region的个数，即回收的新生代的内存来控制young gc的时间开销
    1. 根扫描：静态和本地对象被扫描
    2. 更新RSet：处理dirty card队列更新RSet
    3. 处理RSet：检测从年轻代指向老年代的对象
    4. 对象拷贝：拷贝存活对象到survivor/old区域
    5. 处理引用队列：软引用、弱引用和虚引用
* mixed gc：选定**所有新生代**region，外加根据global concurrent marking统计得出收集收益高的若干来年代region，
在用户指定的开销范围内尽可能的选择收集收益较高的region。
    * mixed gc不是full gc，**它只回收部分老年代region**，如果mixed gc实在无法跟上程序分配内存的速度，
    导致老年代被填满而无法继续进行mixed gc，就会使用serial old gc来进行full gc收集整个heap，本质上，G1不提供full gc
    1. 全局并发标记(global concurrent marking)
    2. 拷贝存活对象(evacuation)
##### global concurrent marking
全局并发标记，执行过程类似于CMS，但不同的是，在G1 GC中，它**主要为mixed gc提供标记服务**，并不是一次gc的必要环节，
主要有一下四个步骤：  
    1. 初始标记(initial mark,STW)：从GC Roots开始标记
    2. 并发标记(concurrent mark)：这个阶段从GC Roots开始堆heap中的对象进行标记，标记线程与业务线程并发执行，
    并且收集各个region中存活的对象
    3. 重新标记(remark,STW)：标记那些在并发标记阶段引用发生变化的对象
    4. 清理(cleanup)：采用复制算法，将存活对象复制到另一个region中并清空region，加入free list
* 第一阶段initial mark是共用了young gc的暂停，这是因为他们可以复用root scan操作，
所以可以说是global concurrent marking伴随着young gc而发生
* 第四阶段只是回收了没有存活的对象的region，因此不需要STW
##### G1运行的主要模式
* YGC：在Eden区充满时触发，在回收之后，所有之前属于Eden的region将全部变成空白，即不属于任何一个分区(eden,survivor,old)，需要STW
    1. 触发时机：Eden区满了
    2. Eden区的数据被移动到Survivor区，如果Survivor区的空间不够，Eden区的部分数据将直接晋升到老年代
    3. Survivor区数据移动到新的Survivor区也有部分数据晋升到老年代的可能，**但最终Eden区是空的**
    4. G1 YGC期间STW，YGC结束之后，业务线程继续工作
* 并发阶段
* 混合模式(mixed)：
    * G1HeapWastePercent：在global concurrent marking标记结束之后，我们可以知道old generation region中有多少空间需要被回收，
    在每次young gc之后和再次发生mixed gc之前，会检查垃圾占比是否到达此参数，只有到达之后才会发生mixed gc
    * G1MixedGCLiveThresholdPercent：old generation region中的存活对象占比，只有在此参数之下，才会被选入到CSet中
    * G1MixedGCCountTarget：一次global concurrent marking之后，最多执行mixed gc的次数
    * G1OldSetRegionThresholdPercent：一次mixed gc中能被选入到CSet的最多old generation region的数量
* full gc(一般G1出现问题时发生)
##### G1运行参数介绍
* -XX:G1HeapRegion=x：设置region大小，并非最终值
* -XX:MaxGCPauseMillis：设置G1收集过程中的停顿时间，默认值200ms，这是个近似值
* -XX:G1NewSizePercent：新生代最小值，默认5%
* -XX:G1MaxNewSizePercent：新生代最大值，默认60%
* -XX:ParallelGCThreads：STW期间，并行的收集器线程数量
* -XX:ConcGCThreads=x：并发标记阶段，并行的线程数
* -XX:InitialHeapOccupancyPercent：设置触发标记周期的Java堆占用率，默认45%。这里的Java堆占比指的是non_young_capacity_bytes，
包括old+humongous
##### 三色标记算法
从GC Roots出发遍历heap。针对可达对象先标记white为gray，然后标记gary为black，遍历完所有可达对象之后，可达对象都变为black，
所有不可达对象全部为white即可以回收
* 黑色：根对象，或者该对象与它的子对象都扫描过了(对象被标记，且它的所有field(引用其他对象)都被标记完毕)
* 灰色：对象本身被扫描，但还没扫描完该对象中的子对象(它的field还没有被标记完)，正在标记的对象
* 白色：还没有被扫描的对象或不可达对象，垃圾对象
##### SATB(snapshot-at-the-beginning)
1. 在开始标记的时候生成一个快照，标记存活对象
2. 在并发标记的时候将所有被改变的对象入队(在write barrier里把所有旧的引用所指向的对象都变成非白)，
write barrier就是对引用赋值做了额外的处理，通过write barrier就可以了解到哪些对象发生了改变
3. 可能存在浮动垃圾，将在下次进行收集
4. 如何找到GC过程中分配的对象？每个region都记录着两个top-at-mark-start(TAMS)指针，分别为preTAMS和nextTAMS指针，
在TAMS以上的对象是新分配的，因而被视为隐式mark
5. 通过这种方式我们就找到了GC过程中新分配的对象，并认为这些对象都是存活的对象
6. 对于三色标记算法在global concurrent marking时可能产生漏标情况，漏标情况会出现在一下的情况中：
    * 并发标记时，应用线程给一个黑色对象的引用类型字段赋值白色引用：post-write-barrier来记录新增的引用关系，
    然后根据这些引用关系为根重新扫描一遍
    * 并发标记时，引用线程删除所有灰色对象到白色对象的引用：pre-write-barrier来记录所有被删除的引用关系，最后以旧引用关系重新扫描一遍
##### G1的最佳实践
* 不断的调优暂停时间：
    * 通过-XX:MaxGCPauseMillis=x可以设置启动应用程序暂停的时间，G1会根据这个值来调整CSet的大小
    * 一般情况下，这个值可以设置到100ms到200ms，需要注意的是暂停时间并不是越小越好，越小会导致CSet变小，导致每次收集的垃圾变少了，
    滞留在JVM中的垃圾变多，导致JVM没有足够的内存去容纳新的对象，最终退化成full gc(serial gc)
* 不要设置新生代和老年代的大小：
    * G1在运行时自动调整新生代和老年代的大小。通过改变代的大小调整对象的晋升速度以及晋升年龄，从而达到我们为收集器设置的暂停时间
    * 我们自己设置新生代和老年代的大小就意味着放弃了G1的自动调优，我们只需要设置堆的大小，剩下的交给G1自己去分配就行
* 关注Evacuation Failure
    * Evacuation Failure类似于CMS的里面的晋升失败，堆空间的垃圾太多导致无法完成region之间的拷贝，
    于是不得不退化成full gc来坐一起全局范围的垃圾收集
#### ZGC(The Z Garbage Collector)
是Java11中推出的一款低延迟的垃圾收集器，目前只支持x64位系统。
##### 设计目标
* 停顿时间不超过10ms
* 停顿时间不会随着堆的大小或活跃对象的大小而增加
* 支持8MB-4TB级别的堆空间(未来支持16TB)
##### 主要特征
与G1的内存布局一样，ZGC也是采用的region的内存布局，但是暂时没有分代，使用了读屏障、染色指针和内存多重映射等技术来实现的**可并发**的
**标记-整理算法**的，以低延迟为目标的GC。
##### 内存布局
ZGC采用region的堆内存布局，但ZGC不同的是ZGC中的region是可动态创建和销毁的，以及region的动态大小，ZGC共有三种大小的region：
1. small region：容量固定为2mb，用于放置小于256kb的对象
2. medium region：容量固定为32mb，用于存放大于或等于256kb或小于4mb的对象
3. large region：容量不固定，可以动态变动，但必须是2mb的整数倍，用于放置大于4mb的对象。每个large region只会存放一个大对象，者也预示
着，虽然名字叫large region，但她的容量完全有可能小于medium region，最小容量可以低至4mb。large region在ZGC的实现是不可重分配的，因为
复制一个大对象的代价非常大
##### 并发整理算法的实现
###### 染色指针(colored pointer)
从前，我们要在对象上存储一些额外的、只供收集器或虚拟机本身使用的数据，通常会在对象头中增加额外的存储字段，比如对象的哈希码，分代年龄、
锁标记等就是这样存储的。这种记录方式在**有对象访问的场景**下是很流畅的进行的，不会有额外的负担。  
但如果对象存在被移动过的可能性，即不能保证对象访问能够成功呢？又或者有一些根本就不会去访问的对象，但又希望得知该对象的某些信息的应用场景呢？  
HotSpot虚拟机的几种收集器有不同的标记实现方案，有的把标记直接记录在 对象头上(如Serial收集器)，有的收集器把标记记录记录在对象相互独立的
数据结构上(G1、Shenandoah使用了一种相当于堆大小1/64的被称为BitMap的结构来记录信息)，而ZGC的染色指针是最直接、最纯粹的，它直接将标记
信息记录在引用对象的指针上，这时，与其说可达性分析是遍历对象图来标记对象的，不如说是遍历 "引用图" 来标记 "引用" 了。  
染色指针是一种直接将少量额外信息存储在指针上的技术，可是为什么指针本身能够存储额外信息？在64位系统中，理论上可以访问到的内存高达16EB(2^64)
字节。实际上，基于需求(用不到那么多内存)，性能(地址越宽，在做地址转换时需要的页表级数就会越多)和成本(消耗更多晶体管)的考虑，在AMD64架
构中只支持到52位(4PB)的地址总线和48位(256TB)的虚拟地址空间，所以目前64位的硬件实际能够支持的最大内存只有256TB。
此外，操作系统一侧也还会施加自己的约束，64位的Linux则分别支持47位(128TB)的进程虚拟地址空间和46位(64TB)的物理地址空间，
64位的Windows系统甚至只支持44位（16TB）的物理地址空间。  
尽管Linux下64位指针的高18位不能用来寻址，但剩余的46位指针所能支持的64TB内存在今天仍然能够充分满足大型服务器的需要。鉴于此，
ZGC的染色指针技术继续盯上了这剩下的46位指针宽度，将其高4位提取出来存储四个标志信息。通过这些标志位，
虚拟机可以直接从指针中看到其引用对象的三色标记状态、是否进入了重分配集(即被移动过)、是否只能通过finalize()方法才能被访问到。
当然，由于这些标志位进一步压缩了原本就只有46位的地址空间，也直接导致ZGC能够管理的内存不可以超过4TB(2的42次幂, 64 - 18 - 4 = 42)  
```text
000000000000000000|0              0          0        0       |000000000000000000000000000000000000000000
unused            |finalizable    remapped   marked1  marked0 |object address

Linux下64位指针的高18位不能用来寻址，所有不能使用；
Finalizable：表示是否只能通过finalize()方法才能被访问到，其他途径不行；
Remapped：表示是否进入了重分配集(即被移动过)；
Marked1、Marked0：表示对象的三色标记状态；
最后42用来存对象地址，最大支持4T；如果未来ZGC能利用Linux不能用来寻址的18位来记录对象状态时，
那Finalizable、Remapped、Marked1、Marked0这4位就可以空余出来，那么ZGC的对象地址将会增大到2^46
```
####### 染色指针三大优势
1. 染色指针可以使得**一旦某个Region的存活对象被移走之后，这个Region立即就能够被释放和重用掉**，
而不必等待整个堆中所有指向该Region的引用都被修正后才能清理。使得理论上只要还有一个空闲Region，ZGC就能完成收集，
至于为什么染色指针能够导致这样的结果，将在后续解释其“自愈”特性的时候进行解释。
2. 染色指针可以大幅减少在垃圾收集过程中内存**屏障的使用数量**，设置内存屏障，尤其是写屏障的目的通常是为了记录对象引用的变动情况，如果
将这些信息直接维护在指针中，显然就可以省去一些专门的记录操作。实际上，ZGC到目前为止并未使用任何写屏障，只使用了读屏障(一部分是染色指针
的功劳，一部分是ZGC现在还不支持分代收集，天然就没有跨代引用的问题)，能够省去一部分内存屏障，显然对程序运行是大有裨益的，这也就导致了
ZGC的吞吐量相对较低的原因
>内存屏障(Memory Barrier)的目的是为了指令不因编译优化、CPU执行优化等原因而导致乱序执行，
>它也是可以细分为仅确保读操作顺序正确性和仅确保写操作顺序正确性的内存屏障的。说白了就是确保代码执行不乱序

**对于读屏障：**
读屏障是JVM想应哟代码插入一小段代码的技术。当应用线程去堆中读取对象的时候，就会执行这段代码，需要注意的是：**仅从堆中读取对象时**，
才会去触发读屏障。读屏障示例如下：
```java
Object o = obj.FieldA   // 从堆中读取引用，需要加入屏障
<Load barrier>
Object p = o  // 无需加入屏障，因为不是从堆中读取引用
o.dosomething() // 无需加入屏障，因为不是从堆中读取引用
int i =  obj.FieldB  //无需加入屏障，因为不是对象引用
```
在对象标记和转移过程中，用于确定对象的引用地址是否满足条件，并作出响应的动作。

3. 染色指针可以作为**一种可扩展的存储结构**用来记录更多与**对象标记、重定位过程相关的数据**，以便日后进一步提高性能。
现在Linux下的64位指针还有前18位并未使用，它们虽然不能用来寻址，却可以通过其他手段用于信息记录。如果开发了这18位，
既可以腾出已用的4个标志位，将ZGC可支持的最大堆内存从4TB拓展到64TB，也可以利用其余位置再存储更多的标志，
譬如存储一些追踪信息来让垃圾收集器在移动对象时能将低频次使用的对象移动到不常访问的内存区域。
##### 内存多重映射
Java虚拟机作为一个普普通通的进程，这样随意重新定义内存中某些指针的其中几位，操作系统是否支持？处理器是否支持？
>无论中间过程如何，程序代码最终都要转换为机器指令流交付给处理器去执行，处理器可不会管指令流中的指针哪部分存的是标志位，
>哪部分才是真正的寻址地址，只会把整个指针都视作一个内存地址来对待。

Linux/x86-64平台上的ZGC使用了多重映射(Multi-Mapping)将多个不同的虚拟内存地址映射到同一个物理内存地址上，这是一种多对一映射，
意味着ZGC在虚拟内存中看到的地址空间要比实际的堆内存容量来得更大。把染色指针中的标志位看作是地址的分段符，
那只要将这些不同的地址段都映射到同一个物理内存空间，经过多重映射转换后，就可以使用染色指针正常进行寻址了。

ZGC将虚拟的64位虚拟地址按如下规则划分：
1. [0~4TB)：对应JAVA堆
2. [4TB~8TB)：对应M0区域
3. [8TB~12TB)：对应M1区域
4. [12TB~16TB)：预留，未使用
5. [16TB~20TB)：对应Remapped区域

##### 内存分配
当应用创建对象时，首先在堆空间申请一个虚拟地址，但该虚拟地址并不会映射到真正的物理地址上。ZGC同时为该对象在M0,M1和Remapped区域都申请
一份虚拟地址，且这三个虚拟地址对应同一个物理地址，但这三个地址同一时间有且只有一个地址有效。ZGC之所以这么设置，是使用的时间换空间的思想，
去降低GC的停顿时间。**空间换时间**中的空间是虚拟空间，而不是真正的物理空间。 

##### ZGC是如何工作的
ZGC的运作过程大致可划分为以下四个大的阶段。全部四个阶段都是可以**并发**执行的，仅是两个阶段中间会存在短暂的停顿小阶段，这些小阶段，
譬如初始化GC Root直接关联对象的Mark Start，与之前G1和Shenandoah的Initial Mark阶段并没有什么差异。
* **初始停顿标记(Pause Mark Start)**：扫描GC Roots来标记初始存活对象
* **并发标记(Concurrent Mark)**：并发标记是遍历对象图做可达性分析的阶段，前后也要经过初始标记、最终标记的短暂停顿，
而且这些停顿阶段所做的事情在目标上也是相类似的。ZGC的标记是在指针上而不是在对象上进行的，
标记阶段会更新染色指针中的Marked 0、Marked 1 标志位。

* **并发预备重分配(Concurrent Prepare for Relocate)**：这个阶段需要根据**特定的查询条件**统计得出本次手机过程需要清理哪些region，
将这些region组成**重分配集(relocate set)**，ZGC划分region的目的并不是想G1那样做收益优先的增量回收，ZGC每次会扫描**所有**的region，
用**范围更大的扫描成本**来换取省去G1中**记忆的维护成本(RSet,CSet)**。因此ZGC的重分配集只是决定了relocate region的存活对象会被重新
分配到其他region中，relocate set里面的region会被释放，而不能说回收行为只是针对这个集合里面的region进行的，因此标记过程是全堆的。
此外，jdk12中ZGC开始支持类卸载以及弱引用的处理，也是在这个阶段完成的。

* **并发重分配(Concurrent Relocate)**：重分配是ZGC执行过程中核心阶段，这个过程需要把relocate set中的存活对象复制到新的region上，
并为relocate set中的每个region维护一样**转发表(forward table)**，记录从就对象到新对象的转向关系。得益于染色指针的支持，ZGC能仅从
引用上就明确得知一个对象是否处理relocate set中，如果用户线程此时访问了位于relocation set的对象，这次访问将会被**预置的内存屏障**所
捕获，然后立即根据region上的forward table记录将访问转发到新复制的对象上，并且修正该引用的值，使其直接指向新对象的地址，ZGC将这种行为
称为**自愈**(self healing)。这样做的好处就是只有第一次访问对象会陷入转发，就是只慢一次，对比Shenandoah的Brooks转发指针，那是每次
对象访问都必须付出的固定开销，说白了就是每次都慢，因此ZGC对用户程序的运行时负载要比Shenandoah来得更低一些。还有另外一个直接的好处就是
由于染色指针的存在，一旦relocation set中的某个region的存活的对象都复制完毕了，**这个region就可以立即释放用于新对象的分配(但是
转发表还是得留下不能释放)**，哪怕堆中还有很多指向这个对象的未更新的指针也没有关系，这些旧指针一旦被使用，他们都是可以自愈的

* **并发重映射(Concurrent Remap)**：重映射所作的就是**修正整个堆中指向重分配集中旧对象的所有引用**，这一点从目标角度看是与
Shenandoah并发引用更新阶段一样的，但是ZGC的并发重映射并不是一个必须要 "迫切" 去完成的任务，因为前面说过，即使是旧的引用，也会通过ZGC
的自愈功能修正到正确的新的对象地址上，最多只是第一次使用时多一次转发和修正操作。重新映射清理这些旧引用的**主要目的**是为了不变慢(还有
清理结束后可以释放转发表的附加收益)，所以并不是很迫切。因此ZGC很巧妙的将**并发重映射**要做的工作**合并到下一次垃圾收集循环中的并发标记**
里去完成了，反正它们都是要遍历所有的对象，这样就省了一次对象图的开销。一旦所有指针都被修正之后，原来的转发表就可以释放了。
>ZGC的设计理念与Azul System公司的PGC和C4收集器一脉相承，是迄今垃圾收集器研究的最前沿成果，
>它与Shenandoah一样做到了几乎整个收集过程都全程可并发，短暂停顿也只与GC Roots大小相关而与堆内存大小无关，
>因而同样实现了任何堆上停顿都小于十毫秒的目标。

##### ZGC并发处理过程
* 初始化：ZGC初始化之后，整个内存空间的地址视图被设置为了Remapped。程序正常运行，在内存中分配对象，满足条件后启动GC，此后进入标记阶段
* 并发标记阶段：第一次进入标记阶段时视图是M0，如果对象被GC线程标记或者被应用线程访问过，那么就将对象的地址视图从Remapped修改为M0。所以，
在并发标记阶段结束之后，对象地址视图要么是Remapped的，要么是M0。如果，对象的视图时M0，说明对象是活跃的；如果对象的地址是Remapped的话，
说明对象是不活跃的。
* 并发转移阶段：标记结束后进入转移阶段，此时地址视图再次被设置成Remapped。如果对象被GC转移线程标记或者应用线程访问过，那么就将对象的
地址视图从M0调整为Remapped

其实，在标记阶段存在两个地址视图M1和M0，上面的过程只用了一个地址视图，之所以设计成两个，是为了区分前一个和后一次的标记。即第二次进入
标记时，地址视图由M1，而非调整为M0。

着色指针和读屏障技术不仅应用在并发转移阶段，还应用在并发标记阶段：将对象设置为已标记，传统的垃圾回收器需要进行一次内存访问，
并将对象存活信息放在对象头中；而在ZGC中，只需要设置指针地址的第42~45位即可，并且因为是寄存器访问，所以速度比访问内存更快。

##### ZGC触发时机
相比于CMS和G1的触发时机，ZGC的GC触发机制有很大不同。ZGC的核心特点就是并发，GC过程中一直会有新的对象产生，如何保证在GC完成前，保证堆
空间不被占满，是ZGC调优的一大目标，因为在ZGC中，当垃圾来不及回收将堆占满时，会导致正在运行的线程停顿，持续时间可能长达秒级之久。
有如下场景会触发GC：
* **阻塞内存分配请求触发**：当垃圾来不及回收，垃圾将堆占满时，会导致部分线程阻塞。我们应当避免出现这种触发方式。
日志中关键字是**Allocation Stall**。
* **基于分配速率的自适应算法**：**最主要的GC触发方式**，其算法原理可简单描述为**ZGC根据近期的对象分配速率以及GC时间，
计算出当内存占用达到什么阈值时触发下一次GC**。自适应算法的详细理论可参考彭成寒《新一代垃圾回收器ZGC设计与实现》一书中的内容。
通过`ZAllocationSpikeTolerance`参数控制阈值大小，该参数默认2，**数值越大，越早的触发GC**。我们通过调整此参数解决了一些问题。
日志中关键字是**Allocation Rate**。
* **基于固定时间间隔**：通过`ZCollectionInterval`控制，适合应对突增流量场景。流量平稳变化时，自适应算法可能在堆使用率达到95%以上才
触发GC。流量突增时，自适应算法触发的时机可能会过晚，导致部分线程阻塞。调整此参数解决流量突增场景的问题，比如定时活动、秒杀等场景。
日志中关键字是**Timer**。
* **主动触发规则**：类似于固定间隔规则，但时间间隔不固定，是ZGC自行算出来的时机，如若已基于固定时间间隔的触发机制，
可以通过`-ZProactive`参数将该功能关闭，以免GC频繁，影响服务可用性。日志中关键字是**Proactive**。
* **预热规则**：服务刚启动时出现，一般不需要关注。日志中关键字是**Warmup**。
* **外部触发**：代码中显式调用`System.gc()`触发。日志中关键字是 `System.gc()`。
* **元数据分配触发**：元数据区不足时导致，一般不需要关注。日志中关键字是**Metadata GC Threshold**。

##### ZGC参数
* **XX:+UnlockExperimentalVMOptions -XX:+UseZGC**：启用ZGC的配置。
* **-XX:ReservedCodeCacheSize=256m -XX:InitialCodeCacheSize=256m**：设置CodeCache的大小， JIT编译的代码都放在CodeCache中，
一般服务64m或128m就已经足够。
* **-XX:ConcGCThreads=2**：并发回收垃圾的线程。默认是总核数的12.5%，8核CPU默认是1。调大后GC变快，但会占用程序运行时的CPU资源，
吞吐会受到影响。
* **-XX:ParallelGCThreads**：STW阶段使用线程数，默认是总核数的60%。
* **-XX:ZCollectionInterval**：ZGC发生的最小时间间隔，单位秒。
* **-XX:ZAllocationSpikeTolerance**：ZGC触发自适应算法的修正系数，默认2，数值越大，越早的触发ZGC。
* **-XX:+UnlockDiagnosticVMOptions -XX:-ZProactive**：是否启用主动回收，默认开启，这里的配置表示关闭。
* **-Xlog**：设置GC日志中的内容、格式、位置以及每个日志的大小。

##### 相比G1
相比G1、Shenandoah等先进的垃圾收集器，ZGC在实现细节上做了一些不同的权衡选择，譬如G1需要通过写屏障来维护记忆集，才能处理跨代指针，
得以实现Region的增量回收。记忆集要占用大量的内存空间，写屏障也对正常程序运行造成额外负担，这些都是权衡选择的代价。
ZGC就**完全没有使用记忆集**，它甚至连分代都没有，连像CMS中那样只记录新生代和老年代间引用的卡表也不需要，因而完全没有用到写屏障，
所以给用户线程带来的运行负担也要小得多。可是，必定要有优有劣才会称作权衡，ZGC的这种选择也限制了它**能承受的对象分配速率不会太高**。
可以想象以下场景来理解 ZGC的这个劣势：ZGC准备要对一个很大的堆做一次完整的并发收集，
假设其全过程要持续十分钟以上(切勿混淆并发时间与停顿时间，ZGC立的Flag是停顿时间不超过十毫秒)，在这段时间里面，
由于应用的对象分配速率很高，将创造大量的新对象，这些新对象很难进入当次收集的标记范围，
通常就只能全部当作存活对象来看待——尽管其中绝大部分对象都是朝生夕灭的，这就产生了大量的浮动垃圾。如果这种高速分配持续维持的话，
每一次完整的并发收集周期都会很长，回收到的内存空间持续小于期间并发产生的浮动垃圾所占的空间，堆中剩余可腾挪的空间就越来越小了。
**目前唯一的办法就是尽可能地增加堆容量大小，获得更多喘息的时间**。但是若要从根本上提升ZGC能够应对的对象分配速率，
还是需要引入分代收集，让新生对象都在一个专门的区域中创建，然后专门针对这 个区域进行更频繁、更快的收集。
Azul的C4收集器实现了分代收集后，能够应对的对象分配速率就比不分代的PGC收集器提升了十倍之多。
##### NUMA(Non-Uniform Memory Access，非统一内存访问架构)
NUMA(Non-Uniform Memory Access，非统一内存访问架构)是一种为多处理器或者多核处理器的计算机所设计的内存架构。由于摩尔定律逐渐失效，
现代处理器因频率发展受限转而向多核方向发展，以前原本在北桥芯片中的内存控制器也被集成到了处理器内核中，
这样每个处理器核心所在的裸晶(DIE)都有属于自己内存管理器所管理的内存，如果要访问被其他处理器核心管理的内存，
就必须通过InterConnect通道来完成，这要比访问处理器的本地内存慢得多。
##### ZGC存在的问题
ZGC最大的问题就是浮动垃圾
ZGC的停顿时间是在10ms以下，但是ZGC的执行时间还是远远大于这个时间的。假如ZGC全过程需要执行10分钟，在这个期间由于对象分配速率很高，
将创建大量的新对象，这些对象很难进入当次GC，所以只能在下次GC的时候进行回收，这些只能等到下次GC才能回收的对象就是浮动垃圾。
>ZGC没有分代概念，每次都需要进行全堆扫描，导致一些“朝生夕死”的对象没能及时的被回收。

目前唯一的办法是增大堆的容量，使得程序得到更多的喘息时间，但是这个也是一个治标不治本的方案。如果需要从根本上解决这个问题，
还是需要引入分代收集，让新生对象都在一个专门的区域中创建，然后专门针对这个区域进行更频繁、更快的收集。  
吞吐量不够高
##### GC之痛
很多低延迟高可用Java服务的系统可用性经常饱受GC停顿的困扰。GC停顿指的是垃圾回收期间STW(Stop The World)，当STW时，所有应用线程全部停止
活动，等待GC停顿结束。
##### CMS与G1停顿时间瓶颈
CMS、G1和ZGC的Young GC都是使用的标记-复制算法，但具体实现的不同导致了巨大的性能差异。  
标记-复制算法应用在CMS的新生代(ParNew默认时CMS的新生代收集器)和G1垃圾收集器中。标记-复制节点分为如下三个阶段：
1. 初始标记：从GC Roots集合开始，标记活跃对象
2. 转移阶段：把活跃对象复制到新的内存地址上
3. 重定位阶段：因为转移阶段导致对象的对象地址发送了变化，在重定位阶段，所有指向对象就地址的指针都要调整到对象新的地址上。

### Java内存泄露的经典原因
1. 对象定义在错误的范围(wrong scope)
2. 异常(Exception)处理不当
未正确的关闭相关资源时，会导致JVM中资源膨胀
3. 集合数据管理不当
    * 当使用Array-Base的数据结构时(如ArrayList或者HashMap时)，尽量避免resize
        * 比如在声明ArrayList时，应尽量估算size，在创建时就确定size的值
        * 减少resize可以避免没有必要的array copying，gc碎片等问题
    * 如果一个List只需要顺序访问，不需要随机访问(random access)，就用LinkList代替ArrayList
        * LinkedList本质是链表，不需要resize。
## 八、JVM参数
### 常用参数
* -verbose:gc：打印出详细的gc日志
* -Xms20m：指定堆初始大小
* -Xmx20m：指定堆最大的大小
* -Xss100k：指定虚拟机栈大小
* -Xmn10m：指定堆中的新生代大小
* -XX:PrintGCDetails：打印出堆详细信息
* -XX:SurvivorRatio=8：指定Eden区和Survivor区的大小比例
* -XX:+PrintCommandLineFlags：打印出JVM的默认启动参数
* -XX:+TraceClassLoading：跟踪class加载的情况
* -XX:MaxMatespaceSize=10m：指定元空间大小，并不会进行扩展
* -XX:HeapDumpOnOutOfMemory：指定在发生堆溢出时，转储堆信息
* -XX:PretenureSizeThreshold=111：单位为字节(byte)，新生对象直接晋升到老年代的阈值，
**需配合 `-XX:+UseSerialGC` 一起使用，否则不起作用**
* -XX:+UseCompressedOops：针对32位程序运行在64位JVM上时，对于特定的指针进行压缩，以免占用过多内存
* -XX:+UseCompressedClassPointers：使用指针压缩
* -XX:MaxTenuringThreshold=5：在JVM可以自动调节对象晋升(Promote)到老年代阈值的GC中，设置晋升到老年代的最大对象年龄，
该参数的默认值是15，CMS中默认值位6，G1中默认15(在JVM中，该数值是由4bit来表示的，所以最大值位1111即15)
    经历多次Minor GC后，存活的对象会在From Survivor和To Survivor之间来回存放，而这一前提就是两个空间有足够多的空间来存放这些数据，
    在GC算法中，
    会计算每个对象的年龄，如果空间中某个年龄的对象已经占据了Survivor空间的50%(这个比例可以自定义)，那么这时JVM会自动调整阈值，
    不在使用默认的晋升阈值去将新生代中的对象晋升到老年代，
    因为默认的晋升阈值会会导致Survivor空间不足，所以需要调整阈值，让这些存活的老对象尽快晋升到老年代。
* -XX:TargetSurvivorRatio=60：此参数为一个百分比，表示在Survivor空间中存活的对象达到60%时，
那么JVM就重新计算MaxTenuringThreshold(晋升到老年代的对象年龄阈值)
* -XX:+PrintTenuringDistribution：打印出对象在Survivor区的对象年龄的情况
* -XX:+PrintGCDetailsStamps：打印GC的时间戳

### GC选择参数
* -XX:+UseParallelGC：使用并行垃圾收集器，新生代使用Parallel Scavenge GC，老年代使用Parallel Old GC
* -XX:+UseSerialGC：使用串行垃圾收集器，新生代使用Serial GC，老年代使用Serial Old GC
* -XX:+UseConcMarkSweepGC：指定老年代使用CMS垃圾收集器，需要再指定一个新生代垃圾收集器
* -XX:+UseParNewGC：指定新生代使用ParNew GC，并行垃圾收集器
* -XX:+UseG1GC：指定使用G1垃圾收集器
### 注意点
1. 对于 `System.gc()` 的理解
System.gc()是告诉JVM接下来进行一次Full GC，但是具体什么时候去执行，最终由JVM自己决定，它的主要作用就是在没有对象创建的时候，
去进行一次GC，因为平时触发GC是在对象创建时导致内存空间不足才会触发
