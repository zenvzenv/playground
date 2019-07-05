# Thread专栏
* chapter01
    1. Java应用程序的main函数就是一个线程，是被JVM启动时调用的，线程名就叫main
    2. 创建一个线程必须要创建Thread，重写 `run()` 方法，并调用 `start()` 方法。
    3. 在JVM启动时，实际上会有很多的线程，但至少有一个非守护线程。
    4. 当你调用一个线程的 `start()` 方法时，此时至少有两个线程，一个是调用你线程的那个线程，还有一个是被执行 `start()` 方法的线程。
    5. 线程分为new,runnable,running,block,terminate，需要注意的是：block是不能直接到running状态的，block需要先转到runnable状态，等分配到cpu资源时才会切换到running状态。
* chapter02
    1. 模仿Thread的策略模式，实现了简单的税务计算功能
    2. 用多线程模拟银行的叫号功能
* chapter03
    1. 通过 `Thread t = new Thread()` 创建线程对象，默认有一个线程名，默认从0开始计数，Thread-0,Thread-1,Thread-2...以此类推
    2. 如果在构造Thread的时候没有传递Runnable或者没有复写Thread的run方法，那么该Thread将什么都不会执行，如果传递了Runnable接口的实例，或者复写了run方法，则会执行该方法的逻辑单元(逻辑代码)
    3. 通过 `Thread t = new Thread(Runnable)` 创建线程对象，会有默认的线程名，当线程启动的时候，JVM会去执行Runnable中复写run方法中的的代码
    4. 如果构造线程未传入ThreadGroup，那么Thread会获取父线程的ThreadGroup作为自己的ThreadGroup，父线程就是启动这个线程的线程，此时子线程和父线程在同一个ThreadGroup中，可以通过ThreadGroup的一些API做些操作
    5. 构造Thread的时候传入stackSize代表着该线程占用的stack的大小，如果没有指定，默认是0，0代表会忽略该参数，该参数会有JNI函数去调用。需要注意的是：该参数在有些平台有用有些平台没有用