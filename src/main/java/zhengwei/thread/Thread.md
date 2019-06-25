# Thread专栏
* chapter01
    1. Java应用程序的main函数就是一个线程，是被JVM启动时调用的，线程名就叫main
    2. 创建一个线程必须要创建Thread，重写 `run()` 方法，并调用 `start()` 方法。
    3. 在JVM启动时，实际上会有很多的线程，但至少有一个非守护线程。
    4. 当你调用一个线程的 `start()` 方法时，此时至少有两个线程，一个是调用你线程的那个线程，还有一个是被执行 `start()` 方法的线程。
    5. 线程分为new,runnable,running,block,terminate，需要注意的是：block是不能直接到running状态的，block需要先转到runnable状态，等分配到cpu资源时才会切换到running状态。