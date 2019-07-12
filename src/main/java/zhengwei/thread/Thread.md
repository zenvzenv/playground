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
* chapter04
    1. 守护线程可以理解为协作线程，协作它的父线程完成某项特定的作业，如果父线程结束了，那么守护线程也就结束了
    2. 子线程会默认继承父线程的daemon
    3. `getName()` 获取线程的名字，`getId()` 获取线程id，`getPriority()` 获取线程优先级，`join()` 当前线程等待子线程执行完毕之后，再执行父线程
    4. 对于线程中断，Java只是把中断标志位设置为true，我们需要根据判断中断标志位来决定后续的操作
    5. 优雅的关闭线程，利用一个标志位来判定是否要结束线程
        1. 可以设置标志位，比如一个flag，循环体中来判断flag，如果是true则继续下次循环，如果为false则结束
            ```java
            @Override
            private volatile boolean start = true;
            public void run() {
                while (start) {
                    //do something
                }
            }
            ```
        2. 可以通过判断线程中的中断标志位，通过 `java.lang.Thread.interrupted` 方法来获取中断标志位
            ```java
            @Override
            public void run() {
                //判断中断标志位，如果是中断状态的话就退出
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(1_000L);
                    } catch (InterruptedException e) {
                        break;//return
                    }
                    //-------后续操作--------
                }
            }
            ```
    6. 强制关闭线程，利用守护线程的特性来结束线程
        1. 将要执行真正业务逻辑的线程realTask包装到一个父线程中executorThread。把realTask设置成executorThread的守护线程，那么executorThread结束的话，那么作为executorThread的守护线程的realTask也会结束
            ```java
            public void execute(Runnable task) {
	            //包装真正执行业务逻辑的线程的线程
                executorThread = new Thread(() -> {
                    //真正要执行业务逻辑的线程
                    Thread realTask = new Thread(task);
                    realTask.setDaemon(true);
                    realTask.start();
                    try {
                        //让executorThread等待realTask执行完毕
                        realTask.join();
                        finished = true;
                    } catch (InterruptedException e) {
                        //捕获到打断信号
                        System.out.println("executorThread被打断，执行线程结束生命周期");
        				e.printStackTrace();
                    }
                });
                executorThread.start();
            }
            ```
* chapter05
    1. synchronized关键字
        1. 修饰在方法上的话那么锁定的是 `this` 对象，在字节码上是体现不出monitorenter和monitorexit的
        2. 修饰在方法内部的话，我们可以指定任意对象作为synchronized锁定对象，在字节码上是能够体现出monitorenter和monitorexit的
        3. 一个monitorenter可以对应多个monitorexit，因为程序不一定是正常退出，也有可能发生异常而退出
        4. 在synchronized代码块内部，其实是单线程执行的，而在synchronized外部是多线程并发执行的，所以要控制synchronized代码的范围，以确保效率不会太低