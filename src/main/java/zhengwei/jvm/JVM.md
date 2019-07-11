# JVM专栏
* TestGC-->学习JVM的时候测试GC的一些代码
* zhengwei.jvm.classloader-->学习JVM中类加载过程时测试时写的实例代码，有代码的帮助，理解起来会更加的透彻
    > 类的加载过程是:加载->链接->初始化->使用->卸载<br/>
    > 其中链接分为:验证->准备->解析<br/>
    __需要特别注意的是准备和初始化是两个过程__<br/>
    1. 加载：加载是把.class文件加载进JVM之中
        1. 加载类的方式有从本地直接加载；通过网络下载.class文件；从jar中加载；从专有数据库中加载和将Java动态编译成.class文件
        2. 类加载器
            1. 类加载器是用来把类加载进JVM中的，从JDK1.2开始，类加载采用**双亲委托机制**，这种机制保证了JVM平台的安全性。在此委托机制中，除了JVM自带的根类加载器外，其余的类加载器都有且只有一个父类加载器。
               Java中所有的核心类库都将会由JVM自带的Bootstrap ClassLoader、ExtClassLoader和AppClassLoader进行加载，用户自定义的类加载器是没有机会去加载的，防止包含恶意核心类乎代码被加载。
            2. JVM自带的类加载器
                1. 根类加载器(**Bootstrap ClassLoader**)，无父类，最顶级的类，会去系统属性 `sun.boot.class.path` 所指定的路径下加载类库，由C++实现，不是ClassLoader的子类
                2. 扩展类加载器(**ExtClassLoader**),父加载器是**根类加载器**，负责加载Java平台中扩展功能的一些jar包，它从系统属性 `java.ext.dirs` 所指定的目录下加载类库，它是ClassLoader的子类
                3. 系统类加载器(**AppClassLoader**)，父加载器是**扩展类加载器**，负责加载classpath中所指定的jar包，从系统属性 `java.class.path` 所指定的目录下加载类，它是ClassLoader的子类
            3. 自定义类加载器
                1. 需要继承 `java.lang.ClassLoader` , `java.lang.ClassLoader` 是一个抽象类，但是没有抽象方法，不能够直接实例化，需要继承它，然后实例化，需要重写findClass方法。
                2. 用户自定义的类加载器的父类加载器是应用类加载器AppClassLoader
                3. 还有一种特殊的类加载器，它的存在就是为了打破双亲委托机制的局限性，为了使用SPI机制而存在的，那就是线程上下文类加载器 `Thread.currentThread().getContextClassLoader()`
                    1. 特别注意的是：自从JDK1.6开始我们使用诸如jdbc、xml...等接口的实现时，即具体实现由厂商来实现的功能时，其实不需要再去显示的去调用 `Class.forName("xxx.class")`
                    2. 因为有 `java.util.ServiceLoader` 类的存在
                        1. 一个重要的属性 `private static final String PREFIX = "META-INF/services/";` ，ServiceLoader会加载去classpath下jar包中的META-INF/services/文件中所表明要加载的类的二进制名字，
                           但是ServiceLoader这个类是由BootstrapClassLoader去加载的，但是BootstrapClassLoader加载不了我们指定的厂商实现的jar包，那么这时候就要用到线程上下文类加载器( `Thread.currentThread().getContextClassLoader()` )
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
                       `java.sql.DriverManager` loadInitialDrivers会去调用ServiceLoader.load方法去加载jdbc相关的驱动类
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
            6. 值得注意的是：各个类加载器之间的关系并**不是继承关系**，而是**包含关系**，形成一种**树形结构**。除了根类加载器，其余的类加载器都有且只有一个父类加载器。
            7. 类加载器的**命名空间**：
                1. 每个类加载器都有自己的命名空间，**命名空间由该类加载器及其所有父类加载器加载的类组成**。
                2. 在同一个命名空间中，不会出现类的全限定名相同的两个类。
                3. 在不同的命名空间中，可能出现类的全限定名相同的两个类。[zhengwei.jvm.classloader.TestClassLoader3.testClassLoaderNamespace]
                4. **子类加载器的命名空间包含所有父类加载器的命名空间**。因此由子类加载器加载的类能够访问到父类加载器加载的类，但是父类加载器是访问不到子类加载器加载的类的，例如扩展类加载器能够访问到根类加载器加载的类。
                5. 如果两个加载器之间没有直接或间接的关系，那么它们各自加载的类将互不可见。[zhengwei.jvm.classloader.TestClassLoader3.testClassLoaderNamespace]
            8. 创建自定义类加载器，只需要继承 `java.lang.ClassLoader` 类，然后重写 `findClass(String name)` 方法即可，该方法根据指定的类的二进制名字，返回对应的Class对象的引用。
    2. 链接：将类与类之间的关系处理好
        1. 验证：校验.class文件的正确性；语义检查；字节码验证和二进制兼容性验证，把加载的类的二进制文件合并到JVM中去。
        2. 准备：为类的**静态变量**分配内存空间，并将其**赋初始值**，在到达初始化之前，类的静态变量知识只是jvm赋予的默认值，而不是真正的用户指定的值
        3. 解析：将类中常量池中寻找类、接口、字段和方法的符号引用替换成直接引用的过程
    3. 初始化：为类的静态变量赋予正确的默认值，就是把链接阶段中的准备阶段的类的静态变量的默认值赋予用户指定的初始值
        1. 类的初始化时机
            1. 创建的类的实例
            2. 访问某个类或接口的静态变量(字节码中使用`getstatic`标记)，或者对静态变量进行赋值(字节码中使用`putstatic`标记)，或者调用类的静态方法(字节码中使用`invokestatic`)
            3. 反射Class.forName("zhengwei.jvm.Test");调用一个参数的Class.forName("xxxxx");是会默认初始化该类的，源码中是有体现的。
                ```java
                public static Class<?> forName(String className) throws ClassNotFoundException {
                         Class<?> caller = Reflection.getCallerClass();
                         return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
                }
                ```
                ```java
                private static native Class<?> forName0(String name, boolean initialize,
                                                             ClassLoader loader,
                                                             Class<?> caller)
                throws ClassNotFoundException;
                ```
            4. 初始化一个类的子类，同时也会初始化这个类的父类，如果父类还有父类，那么会继续初始化父类的父类直到最顶级的父类。这条规则不适用于接口。
            5. JVM启动时被表明启动类的类，包含main方法的类。
            6. JDK1.7支持动态语言调用。
            7. 除了上述的几种调用方式，**其余的调用都是被动调用**，都不会导致类的初始化。
        2. 在初始化阶段，JVM会执行类的初始化语句，为类的静态变量赋予初始值(即**程序员自己指定的值**)，在程序中，静态变量的初始化方法有两种：
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
    4. 使用：
        1. 实例化对象：
            * 为类的新实例分配内存空间，通在堆上分配内存空间
            * 为实例赋予默认值
            * 为实例赋予指定的默认值
                **注意：Java编译器为它编译的每个类都至少生成一个初始化方法。在Java的.class文件中这个实例化方法被称为<init>，针对源代码中的一个构造方法，Java编译器都会产生一个<init>方法**
    5. 卸载：把类的相关信息从内存中剔除
        1. 当一个类被加载、链接和初始化之后，它的生命周期就开始了。只有当该类不再被引用时，即不可触及时，class对象就结束了它的生命周期，该类的信息将会在方法区卸载，从而结束生命周期。
        2. 一个类何时结束生命周期取决于代表它的class对象何时结束生命周期。
        3. 由Java虚拟机自带的类加载器加载的类，在虚拟机周期中始终不会被卸载。Java自带的虚拟机：Bootstrap ClassLoader,ExtClassLoader和AppClassLoader。JVM会始终保持对这些类加载器的引用，而这些类加载器也会保持它们所加载类的class对象的引用，因此这些class对象始终是可触及的。
        4. 由用户自定义的类加载器加载的类是可以被卸载的。
    **值得注意的是：类在准备和初始化阶段中，在执行为静态变量赋值遵循从上到下的顺序执行具体实例参见[zhengwei.jvm.classloader.TestClassLoader2]**
        * 类和接口在加载的时候有一些不同，JVM在初始化一个类时，要求它的全部父类全部初始化完毕，但是这条规则不适用于接口
            1. 初始化一个类时，并不会初始化它所有实现的接口
            2. 在初始化一个接口时，并不会先去初始化它的父接口<br/>
            因此，一个父接口并不会因为它的子接口或实现类初始化而初始化，只有当程序首次使用了特定接口的静态变量时，才会去初始化该接口。
    6. Launcher类，Java程序的入口
        1. 线程上下文类加载的默认值是应用类加载器AppClassLoader，源码中的体现 `sun.misc.Launcher` ,Launcher是由Bootstrap ClassLoader加载，ExtClassLoader和AppClassLoader都会在Launcher中进行初始化
        ```java
        private static URLStreamHandlerFactory factory = new Launcher.Factory();
        private static Launcher launcher = new Launcher();
        private static String bootClassPath = System.getProperty("sun.boot.class.path");
        private ClassLoader loader;
        public Launcher() {
                Launcher.ExtClassLoader var1;
                try {
         	        //初始化ExtClassLoader
                    var1 = Launcher.ExtClassLoader.getExtClassLoader();
                } catch (IOException var10) {
                    throw new InternalError("Could not create extension class loader", var10);
                }
                try {
                    //初始化AppClassLoader，设置AppClassLOader的父类加载器为扩展类加载器ExtClassLoader
                    this.loader = Launcher.AppClassLoader.getAppClassLoader(var1);
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
* zhengwei.jvm.bytecode->学习Java字节码时敲的一些实例代码
    1. 可以使用 `javap -verbose -p` 命令来分析一个class文件，将会分析文件中的魔数、版本号、常量池、类信息、类构造方法和成员变量。
    2. 魔数：所有的.class文件前四个字节为魔数，魔数的固定值是 `0xCAFEBABE`
    3. 魔数之后的四个字节是版本信息，前两个字节是minor version，后两个字节是major version，可以使用 `java -version` 来验证这一点。
    4. 常量池(constant pool)：紧接着版本号之后的就是常量池入口，一个Java类中定义的很多信息都由常量池来维护和描述，可以将常量池看作是class文件的资源仓库，比如Java中的定义的方法和变量信息都存储在常量池中。<br/>
       常量池中主要存储两类常量：字面量和符号引用。**字面量就是文本字符串，Java中被申明成final的常量值；而符号引用是如类和接口的全限定名，字段的名称和描述符，方法的名称和描述符**。
    5. 常量池的总体结构：Java类所对应的常量池主要由常量池数量和常量池表组成。常量池的数量紧跟在版本号之后，占据两个字节；常量池表紧跟在常量池数量之后，常量数组表与一般的数组不同，<br/>
       常量数组中都是不同的元素类型、结构不同的，长度自然也会不同，但是每一种元素的第一个数据都是u1类型的，该字节是个标志位，占据一个字节。JVM会根据这个标志位来获取元素的具体元素<br/>
       值得注意的是：**常量池中元素的个数=常量池数量-1(其中0暂时不使用)**，目的是满足某些常量池索引值的数据在特定情况下需要表达“不引用任何一个常量池”的含义；根本原因在于，索引0也是一个常量(保留常量)，只不过它不位于常量池中，这个常量-> l就对应null值，**常量池的索引从1而非0开始**。
    6. 在JVM规范中，每个变量/字段都有描述信息，描述信息主要是描述字段的数据类型、方法的参数列表(包括数量、类型与顺序)与返回值。根据描述规则，基本数据类型和代表无返回值的void类型都用一个大写字母表示，对象类型使用大写的L加上对象的全限定名表示。<br/>
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
    8. 描述方法时，按照先参数列表后返回值类型的顺序来描述，参数列表被严格定义在 `()` 中，如方法 `String getRealNameByIdAndNickName(int id, String nickNamw)` 表示成 `(I, Ljava/lang/String;)Ljava/lang/String;`
    9. Java字节码对于 `this` 关键的处理:  
        1. 对于类的实例方法(非static方法)，其在编译后所生成的字节码中，方法的参数列表中的参数个数总会比源代码中的参数列表的个数多一个this
        2. this位于参数列表的第一个索引位置 `0` ，这样我们就可以在实例方法中使用this来去访问当前对象的属性和方法了
        3. 对于static方法，是使用不了this关键字的，因为static方法是属于class对象的
        4. 这个添加参数的操作在编译期完成，由javac编译器在编译的时候将对this的访问转化为对一个普通实例的访问
        5. 在运行期间，由JVM自动向实例方法传入this
    10. Java字节码对于异常的处理
        1. 统一采用异常表的方法去处理异常
        2. 在JDK1.4.2之前，并不是使用异常表的方式去处理异常的，而是采用特定的指令方式
        3. 当异常处理存在finally语句块时，现代化的JVM采用的是将finally语句块拼接到每一个catch语句块的后面，换句话说就是有多少个catch就会有多少个finally语句块跟在后面
        4. 被catch住的异常和被抛出的异常的地位是不同的，catch住的异常会被存储在Code中的异常表中，在字节码层面上看，有几个catch就会有多少个finally；被抛出的异常在与Code同级的一个异常表属性中
    11. 字节码中对于调用方法的助记符
        1. invokeinterface：调用接口中的方法(在Java8中，接口中允许有方法的实现，需要使用default关键字去标识)，实际上是在运行期间确定的，决定到底调用实现了该接口的哪个具体的方法
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
        3. 方法的重载和方法的重写的最根本区别就是**方法的接收者不同**，方法的重载(overload)对于JVM来说是静态行为，在编译期确定，方法的重写对于JVM来说是动态行为，在运行期确定
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
* JVM内存划分
    1. 程序计数器(**线程私有**)：
        1. 是一块较小的内存空间，可以看作当前程序执行字节码的行号指示器
        2. 每个线程的程序计数器是独立的
        3. 该区域是唯一没有指定OOM的错误区域，如果线程执行的是Java方法的话，那么程序计数器中记录的是正在执行的虚拟机字节码指令地址，如果是native方法，那么该区域为空
    2. Java虚拟机栈(**线程私有**)：
        1. 和程序计数器一样，Java虚拟机栈是线程私有的，生命周期和线程相同
        2. 每个方法在执行时都会在虚拟机栈中创建一个**栈帧(stack frame)用于存储局部变量表、操作数栈、动态链接、方法出口信息**等
        3. 每个方法从调用到结束，都对应一个栈帧的入栈和出栈的过程
        4. **局部变量表所需的内存空间在编译期间完成分配，当进入一个方法时，这个方法在栈中分配多少内存时完全确定的，在方法运行期间不会改变局部变量表的大小**
        5. 该区域会抛出StackOverflowError和OutOfMemoryError错误
        6. 栈中存储的**变量引用**都是**局部的**，即**定义在方法体内部的变量或引用**，**局部变量和引用都在栈中(包含被声明为final的变量)**
        7. 八种基本数据类型(int,byte,short,boolean,float,double,char,long)的**局部变量(定义在方法体中的基本类型的局部变量)在栈中存储它们对于的值**
        8. 栈中还存储**对象的引用**(**定义在方法体内部的引用类型变量**)，对象的引用并不是对象本身，而是**对象在堆中的地址**，换句话说，**局部对象的引用所指对象在堆中的地址存储在栈中**，当然，如果对象引用没有指向具体的实例，那么对象引用为`null`
    3. 本地方法栈(**线程私有**)
        1. 作用和Java虚拟机栈类似，区别在于Java虚拟机服务于正在运行的Java程序(即字节码)，而本地方法栈则服务于虚拟机执行native方法
        2. 本地方法栈会抛出StackOverflowError和OutOfMemoryError
    4. Java堆(**线程共享**)
        1. Java堆是被所有线程共享的一块区域，也是Java虚拟机中内存最大的一块区域，在虚拟机启动时创建，它对内存的要求是逻辑上连续，物理上不一定连续
        2. 此内存区域的唯一目的就是存放对象实例，几乎所有的对象都在这里分配内存，也有例外：栈上分配，标量替换技术
        3. Java堆有被称为GC堆，GC在此区域回收的效率最高，Java堆可以大致分为新生代和老年代，再细致一点可以分为**Eden空间，From Survivor空间和To Survivor空间**。不论怎么划分都与存放的内容无关，都是存放的对象的实例，进一步的划分内存只是为了更好的进行垃圾收集
        4. **实例变量(非static修饰的成员变量)和对象关联在一起，所以实例变量存放在堆中**
        5. **Java数组也是在堆中开辟内存**
    5. 方法区(**线程共享**)
        1. 用于存储已被虚拟机**加载的类信息、常量、静态变量、即时编译器编译后的代码**等数据，**方法区包含静态常量池和运行时常量池**
            1. 这个类型的全限定名
            2. 这个类的直接父类的全限定名(**`java.lang.Object` 除外**)，其他类型若没有声明直接父类，默认父类是Object
            3. 这个类的访问修饰符(public,private,abstract...)
            4. 这个类的直接接口的有序序列
            5. 类型的常量池
            6. 域(Field)信息
            7. 方法(Method)信息
            8. **除了常量外的所有static变量(类变量)，因为静态变量和类关联在一起，随着类的加载而存储在方法区中(而非存储在堆中)**
            9. **static final修饰的成员变量存储与方法区中**
            10. 八种基本类型(int,byte,float,long,float,boolean,char,double)的**静态变量会在方法区开辟空间，并将对应的值存在方法区中**，对于引用类型的静态变量如果未用 `new` 关键字去引用类型的静态变量分配对象(如 `static Object obj;`)，那么对象的引用obj会存储在方法区中，并为其指定默认值 `null` ，
                若引用类型使用了 `new` 关键字为静态变量分配了实例对象(如：`static Object obj=new Object();`)，那么对象的引用obj存在方法区中，并且该对象在堆中的地址也会一并存在方法区中(**注意此时静态变量只是存储的实例对象在堆中的地址，实例对象本身还是存在堆中**)
            11. 程序运行时会**加载类编译成的字节码**，这个过程中**静态变量**和**静态方法**及**普通方法**对应的字节码被加载到方法区。
            12. 但是_**方法区中不存实例变量的**_，**这是因为类加载先于类实例的产生，而实例变量和变量关联在一起，没有对象就不存在实例变量，类加载时只有class对象，所以方法区中没有实例变量。**
            13. **静态变量**和**静态方法**在方法区存储方式是有区别的。
        2. 方法区再JDK1.8之前是堆上的逻辑的一部分，但是它有一个别名"非堆"，目的是与堆区分开来；在JDK1.8(包含1.8)之后，方法区被从堆中一处，两者不再使用同一块内存区域，方法区被分离出来之后被称为**元数据区**，**元数据区使用直接内存**
        3. 方法区也被称为**永久代**，是因为GC在此区域的回收效率极低，是因为方法区都是存储的跟class对象相关的信息，而方法区中大部分都是由Bootstrap ClassLoader、ExtClassLoader和AppClassLoader加载的，由JVM自带的类加载器加载的类，JVM是不会去卸载的
        4. 该区域的内存回收目标主要针对常量池的回收和类型的卸载
        5. 当方法区无法满足内粗分配需求时，将会抛出OOM错误
    6. 运行时常量池(Runtime Constant Pool)
        1. 运行时常量池时方法区的一部分，主要存放class文件中记录的常量池(Constant Pool)，用于存储编译期生成的各种**字面量和符号引用**，这部分内容在类被加载时进入方法区的运行时常量池中存放
        2. 当常量池无法再申请到内存时将会抛出OOM错误
    7. 直接内存(堆外内存)
        1. 当申请的内存>实际内存时，将会抛出OOM错误
        2. 它将通过使用native函数库来直接分配堆外内存，需要手动释放内存(Unsafe)
        3. 这部分内存**不受GC管理**，效率比较高，但容易出错。
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
        