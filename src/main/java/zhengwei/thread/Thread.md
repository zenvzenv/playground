# Thread专栏
## 基础
### 线程的几大状态
1. 新建状态(New)：线程被创建后，就进入了新建状态，例如： `Thread thread = new Thread();`
2. 就绪状态(Runnable)：也被称为“可执行状态”。线程创建之后，其他线程调用了该线程的 `start()` 方法从而来启动该线程。例如：`thread.start()`。处于就绪状态的线程，随时可能被CPU调度运行
3. 运行状态(Running)：线程获得CPU执行权。**线程只能从就绪状态进入运行状态，其余状态不能进入运行状态**
4. 阻塞状态(Blocked)：阻塞状态是因为线程因为某种原因放弃了CPU的执行权，暂时停止运行。直到线程进入就绪状态(获得对象的锁)，才有机会进入运行状态。阻塞的情况分为三种
    1. 等待阻塞：通过调用线程的 `wait()` 方法，让现场等待某工作的完成并且**释放该线程持有的锁**，JVM会把该线程移入到Wait Set中
    2. 同步阻塞：线程在获取synchronized锁失败之后会进入同步阻塞状态，JVM会把该线程移入到Entry　Set中
    3. 其他阻塞：通过调用线程的 `sleep()` 方法或 `join()` 方法或发出IO请求时，线程会进入阻塞状态。当sleep超出指定时间时、join等待线程终止或结束、或IO处理完毕时，线程重新转入就绪状态
5. 死亡装填(Dead)：线程执行完毕或者异常退出的时候，该线程结束生命周期
### 1. wait()，notify()和notifyAll()等方法介绍
1. `wait(),notify()，notifyAll()` 方法都是定义在 `java.lang.Object` 类中
2. `wait()` 让当前线程处于“等待(阻塞)状态”，直到其他线程调用了**加锁对象**的 `notify()` 或 `notifyAll()` 方法去唤醒线程，从而进入**就绪状态**，**释放该线程持有的锁**
3. `wait(long timeout) wait(long timeout,int nanos)` 让当前线程处于“等待状态”，超过指定的等待时间之后退出等待状态，进入**就绪状态**
3. `notify()` 唤醒**一个**等待在该对象锁上的线程
4. `notifyAll()` 唤醒**所有**等待在该对象锁上的线程
### 2. 为什么wait(),notufy(),notifyAll()要定义在Object中?
* `wait(),notity()和notifyAll()` 都依赖对象锁(即同步锁)， 每个对象的对象有且仅有一个，都要通过对象多来对锁进行操作。
### 3. yield()介绍
* `yield()` 的作用是让步。它能让当前线程有“运行状态”转为“就绪状态”，从而让其他具有相同优先级的等待线程去有机会**竞争**对象锁获得CPU的执行权；有可能当前线程调用yield()之后又进入了“运行状态”继续运行了。
### 4. yield()和wait()的区别
* `wait()` 是让线程由“运行状态”转到“等待(阻塞)状态”，而 `yield()` 是让当前线程由“运行状态”转为“就绪状态”
* `wait()` 会让线程释放锁，而 `yield()` 不会让线程释放锁
---
## chapter01
1. Java应用程序的main函数就是一个线程，是被JVM启动时调用的，线程名就叫main
2. 创建一个线程必须要创建Thread，重写 `run()` 方法，并调用 `start()` 方法。
3. 在JVM启动时，实际上会有很多的线程，但至少有一个非守护线程。
4. 当你调用一个线程的 `start()` 方法时，此时至少有两个线程，一个是调用你线程的那个线程，还有一个是被执行 `start()` 方法的线程。
5. 线程分为new,runnable,running,block,terminate，需要注意的是：block是不能直接到running状态的，block需要先转到runnable状态，等分配到cpu资源时才会切换到running状态。
## chapter02
1. 模仿Thread的策略模式，实现了简单的税务计算功能
2. 用多线程模拟银行的叫号功能
## chapter03
1. 通过 `Thread t = new Thread()` 创建线程对象，默认有一个线程名，默认从0开始计数，Thread-0,Thread-1,Thread-2...以此类推
2. 如果在构造Thread的时候没有传递Runnable或者没有复写Thread的run方法，那么该Thread将什么都不会执行，如果传递了Runnable接口的实例，或者复写了run方法，则会执行该方法的逻辑单元(逻辑代码)
3. 通过 `Thread t = new Thread(Runnable)` 创建线程对象，会有默认的线程名，当线程启动的时候，JVM会去执行Runnable中复写run方法中的的代码
4. 如果构造线程未传入ThreadGroup，那么Thread会获取父线程的ThreadGroup作为自己的ThreadGroup，父线程就是启动这个线程的线程，此时子线程和父线程在同一个ThreadGroup中，可以通过ThreadGroup的一些API做些操作
5. 构造Thread的时候传入stackSize代表着该线程占用的stack的大小，如果没有指定，默认是0，0代表会忽略该参数，该参数会有JNI函数去调用。需要注意的是：该参数在有些平台有用有些平台没有用
## chapter04
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
## chapter05
1. synchronized关键字
    1. 修饰在方法上的话那么锁定的是 `this` 对象，在字节码上是体现不出monitorenter和monitorexit的
    2. 修饰在方法内部的话，我们可以指定任意对象作为synchronized锁定对象，在字节码上是能够体现出monitorenter和monitorexit的
    3. 一个monitorenter可以对应多个monitorexit，因为程序不一定是正常退出，也有可能发生异常而退出
    4. 在synchronized代码块内部，其实是单线程执行的，而在synchronized外部是多线程并发执行的，所以要控制synchronized代码的范围，以确保效率不会太低
    5. 如果synchronized修饰在实例方法上的话，那么synchronized锁定的是this对象；如果修饰在方法内部的话，我们需要指定一个锁对象，这时锁定的就是我们自己指定的对象；如果修饰在静态方法上的话，那么锁定的是这个类的class对象
    6. synchronized是可重入锁
    7. synchronized关键字保证变量的可见性与原子性
    8. **在JMM中，synchronized规定，在线程加锁时，先清空工作内存->在主内存拷贝最新变量的副本到工作内存——>执行完代码->将更新后的共享变量刷新会主存->释放互斥锁**
2. sleep和wait的区别
    1. sleep是Thread提供的方法，wait是Object提供的方法
    2. sleep不会释放锁，wait会释放锁，并且把线程加入到Wait Set中
    3. 使用sleep方法不需要在synchronized代码块中，但是wait需要
    4. 使用sleep时，不需要被唤醒，但是wait是需要被唤醒的
3. 死锁：可以通过 `jstack <pid>` 查看是否有死锁，代码中synchronized的重叠使用，很可能会发生死锁
4. 生产者和消费者实例，需要注意的是多个生产者和多个消费者的时候对于notify和notifyAll的把控，具体见代码
5. 生产者和消费者经典案例：
    1. 主要利用 `wait()` 和 `notify()` 和 `notifyAll()` 方法来完成
    2. `wait()` `notify()` `notufyAll()` 都是Object类提供的方法，而不是Thread提供的方法
    3. 需要注意的是wait方法最好和while循环进行配合使用，在每次被唤醒的时候都会去再次进行条件判断，确保程序的正确性
    4. 在Java总每个对象都存在两个“虚”集合，Wait Set和Entry Set
        1. Wait Set：如果线程A执行了对象锁的wait方法之后，JVM会把该线程放入该锁对象的Wait Set中，该线程处于blocked状态。
                     当锁对象调用notify方法时，JVM会唤醒Wait Set中的一个线程，把该线程从Wait Set移入到Entry Set中，等待去竞争锁，获得锁后状态从blocked变为runnable
                     如果调用了notifyAll方法，JVM会唤醒Wait Set中所有的线程，把Wait Set中所有的线程移入到Entry Set中，等待区竞争锁，获得锁后状态从blocked变为runnable
        2. Entry Set：如果线程A已经持有了锁对象的锁，并没有释放锁，当别的线程想要获得这个对象的锁时，那么这个线程就会进入所对象的Entry Set中，这个线程处于blocked状态。
                      对于Entry Set中的线程，JVM会选择Entry Set中的一个线程去获取对象锁，获得锁之后，线程的状态从blocked状态转变成runnable状态
    5. 如果一个线程想要去获取锁必须具备如下几个条件
        1. 对象锁已经释放(synchronized代码块执行完毕或者调用了wait方法主动放弃CPU执行权)
        2. 该线程处于runnable状态
## chapter06
1. 自定义锁的实例，主要用到的是wait和notifyAll方法
## chapter07
1. 引用程序钩子函数，在JVM退出之前调用，可以做一些程序的资源释放工作和通知操作，但是kill -9是不会执行钩子函数的
2. 自定义简易版线程池：实现了提交任务，关闭线程池，拒绝策略，动态伸缩线程池中线程数量的大小，详情见代码
3. 如果我们想捕获Thread在运行期间抛出的非检查异常的话可以使用`java.lang.Thread.setUncaughtExceptionHandler(UncaughtExceptionHandler eh)`方法
## chapter08
1. JMM(Java内存模型)
    1. 什么是JMM(Java内存模型)
        * Java程序是运行在JVM之上的，JMM就是一种符合内存模型规范的，屏蔽了各种硬件和操作系统的访问差异的，保证了Java程序在个平台下对内存的访问都能保证效果一致的机制与规范
        * Java内存模型规定了所有的变量都存储在主存中，每条线程都有自己的工作内存，线程的工作内存中保存了该线程中用到的变量的主内存的副本的拷贝，线程对变量的所有操作都必须在工作内存中进行，而不是直接读取主内存。
        * 不同线程之间也无法直接访问其他线程的工作内存，线程间的变量的传递与同步均需要通过自己的工作内存与主内存之间进行数据同步进行
        * JMM是一种规范，目的在于解决多线程通过共享内存进行通信时，存在的本地内存数据不一致、编译器会对代码指令重排序、处理器对代码进行重排序等带来的问题
        * JMM中规定的主内存与工作内存，可以简单的类比到计算机内存模型中的主存与缓存的概念。特别需要注意的是：JMM中的主内存与工作内存与JVM内存结构中的Java Heap、stack、method area等并不是同一层次的内存划分，无法直接类比，
          如果一定要勉强对应的话，从变量、主内存和工作内存的定义来看，在主内存上主要对应于Java Heap中的对象的实例的部分,工作内存则是虚拟机栈的部分区域
    1. 缓存不一致的问题
        * 计算机在运行程序的时候，都是把程序翻译成CPU指令发送给CPU去执行的，CPU执行命令的时候势必会有数据的读写操作，程序的一些临时变量都是存放在主存中的，即物理内存上，
          但是CPU的运行速度比主存的速度快很多，如果任何时候对数据的操作都要通过主存来完成的话，那么效率会很低，因此CPU中设立了高速缓存，即程序在运行的过程中，把临时变量复制一份到高速缓存中，
          那么程序在运行，就不需要从主存中去读取数据了，直接从高速缓存中读取即可，计算完成后再刷新会主存即可，当然，这样子的运行流程再单线程下运行时没有问题的，但是在多线程环境下就会出现问题。
          由于是多线程，每个线程可能处于不同的CPU核心上，也就对应不同的高速缓存，每个线程只从自己的高速缓存中读取数据，待整个计算完毕之后各自把结果刷新会主存中，着就会出现缓存数据不一致问题。
        * 为了解决缓存不一致问题，有一下两种解决方案
            1. 通过在总线上加锁即通过向CPU发送LOCK#的指令：因为CPU和其他部件进行通信时都是通过总线进行通信的，如果对总线加LOCK#锁的话，就会阻塞其他CPU对其他部件的访问(如内存)，
               从而是的只有一个CPU能够对该变量所在的内存进行读写，这就解决缓存不一致的问题，但是这种方法会导致效率降低，使得各个CPU之间变成串行化执行。
            2. 通过缓存一致性协议(MESI)来控制：保证了每个缓存中使用的共享变量的副本是一致的。其核心思想就是：当CPU写数据时，如果操作的变量是共享变量时，即在其他CPU中也存在该变量的副本，
               会发送通知，通知其他CPU将该变量的缓存行置为无效状态，因此其他CPU需要读取这个变量时，发现自己的缓存已经失效了，就会去主存中重新读取改变量的值
    2. JMM原子操作
        * read(读取)：作用于主内存变量，从主内存读取数据
        * load(载入)：作用于主内存变量，从主内存读取到的数据写入工作内存
        * use(使用)：作用于工作内存变量，从工作内存读取数据来进行计算
        * assign(赋值)：作用于工作内存变量，将计算好的值重新写回工作内存
        * store(存储)：作用于工作内存变量，将工作内存中一个变量的值传递到主内存中，以便后续write操作
        * write(写入)：作用于主内存变量，把store操作从工作内存的值存入主内存变量中
        * lock(锁定)：作用于主内存变量，将主内存变量加锁，表示为线程独占状态
        * unlock(解锁)：作用于主内存变量，把一个处于锁定状态的变量释放出来，释放后的变量才能被其他线程锁定
2. volatile关键字
    1. 一旦共享变量被volatile修饰，那么就具备两层语义
        1. 保证不同线程的可见性，一个线程修改了变量，那么对于其他线程是可见的，实现原理时内存一致性协议
        2. 禁止对其重排序，也就保证了有序性，实现原理时内存屏障
        3. **但是并不保证原子性**
        4. **底层实现主要是通过汇编的lock前缀指令**，它会锁定这块内存区域的缓存并写回主内存，此操作被称为缓存锁定，一个处理器的缓存值通过总线回写到内存会导致其他处理器相应的缓存失效
    2. 总结：
        1. 保证重排序的时候不会把后面的指令放到屏障的前面，也不会把前面的代码放到后面
        2. 强制对缓存的操作立即写入主存
        3. 如果是写操作，它会是其他CPU中的缓存失效
    3. 使用场景
        1. 状态量标记
            ```java
            volatile boolean start=true;
            while(start){
               //
            }
            void close(){start = false;}
            ```
        2. 保证内存屏障的一致性
            ```java
            volatile boolean flag = false;
            //----------Thread1------------
            //...
            //...
            Object obj=createObj();    //1
            flag=true;                 //2,不会被重排序到别的地方
            //----------Thread2------------
            while(!flag){
               sleep();
            }
            ```
3. 并发编程中的三个重要概念
    1. 原子性：即一个或多个操作，要么全部执行并且执行过程中不会被其他线程打断，要么就全部不执行
        * **Java对基本数据类型的读取和赋值是保证了原子性的**，要么都成功，要么都失败，不可中断
        >int a=10;  原子性           Java保证基本类型赋值时是原子性的, `bipush 10`<br/>
        >int b=a;   不是原子性       可拆分成： `oload_0 ; istore_1`<br/>
        >a++;       不是原子性       可拆分成： `iinc 0 by 1`<br/>
        >a=a+1      不是原子性       可拆分成： `iload 0 ; iconst_1 ; iadd ; istore_0`
    2. 可见性：即当多个线程访问统一变量时，一个线程修改了变量的值，其他线程能够立即看到修改的值
        * Java通过volatile关键使得变量可见，当被volatile修饰的变量被修改的时候，会被立即刷新回主存中，其他线程会直接去主存中读取该变量
    3. 有序性：即程序运行按照程序定义的先后顺序去执行
        * 有序性相对于CPU对代码的重排序优化而言，有时CPU为了提高程序运行效率，会对输入的代码进行优化，它不保证程序中的各个语句的执行顺序按照代码中定义的顺序去执行，但是保证程序最终的结果和代码顺序执行之后的结果一致
        * happens-before relationship定义
            >happens-before在JMM中是非常重要的原则，它是判断数据是否存在竞争，线程是否安全的重要依据，保证了多线程环境下的可见性
            1. 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前.
            2. 如果两个操作之间存在happens-before关系，并不意味着一定要按照happens-before原则来制定执行顺序，如果重排序之后的结果与按照happens-before关系执行的结果一致的话，那么重排序是合法的。
        * happens-before原则规则
            1. 程序次序规则：一个线程内，按照代码顺序，书写在代码前面的操作先行发生于书写在代码后面的操作
                * 一段代码在单线程中**执行的结果**是有序的，因为JVM和CPU会对代码进行重排序，虽然重排序了，但是并不影响最终的执行结果，所以最终执行结果与顺序执行的结果是一样的。所以这个规则只对单线程有效，对多线程下无法确保正确性
            2. 锁定规则：一个unlock操作后行发生于lock操作
                * 无论在单线程环境还是多线程环境，一个锁处于锁定状态，那么必须先执行unlock操作之后才能进行lock操作
            3. volatile变量规则：对一个被volatile修饰的变量的写操作先行与对这个变量的读操作，即写操作的优先级大于读优先级
                * volatile保证了线程间的可见性。如果一个线程去写一个volatile修饰的变量，另一个线程去读这个变量，那么这个写操作一定是happens-before这个读操作
            4. 传递规则：如果操作A先行发生于操作B，而操作B先行发生于操作C，那么操作A先行发生于操作C，即 A happens-before B,B happens-before C->A happens-before C
            5. 线程启动规则：Thread对象的 `start()` 方法先行发生于此线程的每一个动作，即Thread是线程最先被调用的方法
                * 假定ThreadA在执行过程中，通过调用ThreadB.start()方法来启动ThreadB，那么ThreadA对共享变量的修改在接下来线程B开始执行后确保对ThreadB可见
            6. 线程中断原则：对线程的 `interrupt()` 方法的调用先行发生于被中断的线程中的代码检测中断事件的发生
                * 假定ThreadA在执行过程中，通过调用ThreadB.join()来等待线程B的结束，那么线程B在终止之前对共享变量的修改在ThreadA等待返回后可见
            7. 线程终结原则：线程中所有的操作都发生于线程的终止检测，我们可以通过Thread.join()方法结束，Thread.isAlive()的返回值手段来检查到线程已经终止运行
            8. 对象终结原则：一个对象的初始化完成先行与它的finalize()方法被调用
            9. 将一个元素放入一个线程安全的队列的操作happens-before从队列中取出这个元素
            10. 将一个元素放入一个线程安全的容器的操作happens-before从容器中取出这个元素的操作
            11. 在CountDownLatch上的倒数操作happens-before CountDownLatch#await()操作
            12. 释放Semaphore许可的操作happens-before获得许可的操作
            13. Future表示的任务的所有操作happens-before Future#get()操作
            14. 向Executor提交一个Runnable或Callable的操作happens-before任务开始执行操作
        * **如果两个操作不存在上述规则的任意一条happens-before原则，那么这两个操作就没有顺序的保证，JVM会对这些操作进行重排序。如果操作A happens-before 操作B，那么操作A在内存上所作的操作对操作B是可见的**
        * 用一个简单的例子来阐述happens-before原则
            ```java
            private int i=0;
            public void write(int j){
                  i=j;
            }
            public int read(){
                  return i;
            }
            ```
            我们约定ThreadA执行write()，ThreadB执行read()，并且ThreadA先于ThreadB执行，那么ThreadB获得的结果是什么？我们来分析下
            1. 由于两个方法是被两个线程执行的，所以不满足程序次序原则
            2. 两个方法都没有使用锁，所以不满足锁定原则
            3. 由于变量i没有被volatile修饰，所以不满足volatile变量原则
            4. 传递规则也是不满足的<br/>
            所以我们是不能够通过happens-before原子来推导出ThreadA happens-before ThreadB，虽然在时间上ThreadA先于ThreadB执行的，但是就是无法确定ThreadB获得的结果是什么，所以这段代码是线程不安全的。那么需要对变量i加上volatile修饰或者给两个方法加锁即可满足happens-before原则
## Atomic
>JDK提供的原子工具类，可以保证：1.可见性；2.有序性；3.原子性，那么为什么能够保证这些特性呢？
>由于1.volatile修饰的一个原子工具类的成员变量保证了可见性和有序性；2.CAS算法，也就是CPU级别的同步指令，相当于乐观锁(自旋锁)，它可以检测其他线程对共享数据的变化
1. 对于并发包下的原子包的核心就是利用volatile来修饰value使得对变量的修改对所有线程可见，同时使用Unsafe提供的多种基于底层硬件指令的配合进行compareAndSwap(CAS)操作，达到lock-free的线程安全，提高并发性能。
2. 原子包下的操作大多利用的是自旋锁的方式进行而不是使用的synchronized同步锁。
3. 实际上，在JDK对于synchronized的不断优化下，如果线程之间冲突非常频繁的话，CAS的效率反而不如synchronized，因为自旋虽然没有让线程阻塞，但其实线程是一直在占有资源去执行代码的，长时间的自选会造成资源的浪费
4. 原子包工具类中atomicInteger++ -> incrementAndGet()的过程解析
```java
for(;;){
    //获取到当前内存中的值value，被volatile修饰的变量
    int current = get();
    int next = current+1;
    //期望值是current，内存值是value(已经赋值给了current)，判断current的值是否被修改，如果没有被修改，那么就更新，否则不更新
    if(compareAndSet(current,next)) {
        return next;
    }
}
```
5. 原子工具类可能引发ABA问题  
thread      T1          T2
            A           A->B->A     中间A已经被T2线程多次修改，但最终改回了A
            A->B                    这样在在T1看来的话，是没有做修改的，T1会继续接下来的操作
6.为了解决ABA问题，JDK提供了 `java.util.concurrent.atomic.AtomicStampedReference` 类来在加上了Stamp，每次对比更新的时候都会去比较Stamp的值，以确保引用被正确修改。
* volatile、synchronized和JMM系统阐述
    1. synchronized
        1. synchronized保证了同一时刻只有一条线程可以执行某个方法或者某个代码块(主要是对方法或者代码块中的共享变量的操作)
        2. synchronized可以保证一个线程的变化(主要是共享变量的变化)被其他线程看见(保证可见性。完全可以替代volatile的作用)
        3. synchronized的三种应用方式
            1. 修饰实例方法，作用于当前实例加锁即锁定的是this对象，想要进入同步代码块需要先获取this对象的锁
            2. 修饰静态变量，作用于当前class对象加锁，想要进入同步代码块需要先获取当前class对象的锁
            3. 修饰代码块，指定加锁对象，给给定对象加锁，想要进入同步代码块需要先获取给定对象的锁
        4. JVM中的同步(synchronized)是基于**进入管程(entermonitor)**和**退出管程(exitmonitor)**来实现的，无论是显示同步(即synchronized修饰的是代码块)还是隐式同步(即synchronized修饰在方法上)都是如此
            1. 显示同步：synchronized修饰的是代码块，有明显的entermonitor和exitmonitor指令
            2. 隐式同步：synchronized修饰的是方法，JVM使用ACC_SYNCHRONIZED标志来隐式实现同步
        5. 理解Java对象头与monitor(这里先对重量级锁即对象多进行阐述)
            1. 在JVM中对象(不包括数组)的组成一般有对象头，实例变量和填充数据
                1. 实例变量：存放类的属性信息，包括父类的属性，如果是数组还会包含数组的长度，这部分内存按4字节对齐
                2. 填充数据：由于JVM要求对象的起始地址必须为8字节的整数倍。填充数据不是必须的仅仅是为了对其数据
                3. 对象头：是实现synchronized的基础，一般而言synchronized使用的锁对象是存储在Java对象头中的，JVM用两个字节来存储对象头(如果是数组的话会JVM会用三个字节存储对象头，多出来的一个字节用于存储数组长度)<br/>
                    | 虚拟机位数 | 头对象结构        | 说明                                                                      |
                    | ---------- | ---------------------- | --------------------------------------------------------------------------- |
                    | 32/64bit   | Mark Word              | 存储对象的hashcode，分代年龄，所信息或GC标志等信息      |
                    | 32/64bit   | Class Metadata Address | 类型指针指向对象的类元数据，JVM通过这个这个指针确定这个类是属于哪个类的实例 |
                4. Mark Word的具体结构<br/>
                    | 锁状态 | 25bit        | 4bit         | 1bit是否为偏向锁 | 2bit锁标志位 |
                    | -------- | ------------ | ------------ | ---------------- | ------------ |
                    | 无锁状态 | 对象hashcode | 对象分代年龄 | 0                | 01           |
                5. 每个对象都存储着一个monitor与之关联，对象与其monitor之间的关系存在多种实现方式，如monitor可以与对象一起创建和销毁或当线程试图获取对象锁时自动生成，但当一个monitor被某个线程持有后，它便处于锁定状态
                   在JVM(Hotspot)中，monitor是由ObjectMonitor来实现，其数据结构如下：
                   ```c++
                   ObjectMonitor() {
                       _header       = NULL;
                       _count        = 0; //记录个数
                       _waiters      = 0,
                       _recursions   = 0;
                       _object       = NULL;
                       _owner        = NULL;
                       _WaitSet      = NULL; //处于wait状态的线程，会被加入到_WaitSet
                       _WaitSetLock  = 0 ;
                       _Responsible  = NULL ;
                       _succ         = NULL ;
                       _cxq          = NULL ;
                       FreeNext      = NULL ;
                       _EntryList    = NULL ; //处于等待锁block状态的线程，会被加入到该列表
                       _SpinFreq     = 0 ;
                       _SpinClock    = 0 ;
                       OwnerIsThread = 0 ;
                     }
                   ```
                   **ObjectMonitor中有两个队列，_WaitSet和_EntryList，用来保存ObjectWaiter列表(每个等待锁的线程都会被封装成ObjectWaiter对象)，_owner指向持有ObjectMonitor对象的线程，
                   当多个线程同时访问同一段同步代码时，首先会进入_EntryList集合，当线程获取对象monitor之后进入_Owner区域并把monitor中的_owner变量设置为当先线程同时monitor中的计数器_count加一(这也是synchronized可重入的重要原因)，
                   若线程调用 `wait()` 方法，将释放当前持有的monitor，_owner变量恢复成null，_count变量自减一，同时该线程进入_WaitSet集合中等待被唤醒，从_WaitSet中被唤醒的线程首先会被放到_EntryList集合中，
                   在_EntryList集合中的线程会去争抢monitor，强盗monitor的线程进入RUNNABLE状态，等待被CPU调用。若当前线程执行完毕也将释放monitor并复位ObjectMonitor中的变量，以便其他线程进入monitor。**
                6. monitor对象存在与每个Java对象的对象头中(存储的时monitor对象(ObjectMonitor)的指针)，synchronized锁便是通过这种方式获取锁的，也就是说所有的Java对象都可以作为synchronized的锁对象，同时 `wait(),notify(),notifyAll()` 方法处于Object类中的原因
        6. synchronized代码块底层原理
            1. 定义一个synchronized代码块
            ```java
            private void test(){
                synchronized (object){
                    System.out.println("hello synchronized");
                }
            }
            ```
            通过 `javap -verbose -p ` 命令来反编译class文件可以得到字节码信息
            ```text
             0 aload_0
             1 getfield #11 <zhengwei/jvm/bytecode/TestByteCode2.object>
             4 dup
             5 astore_1
             6 monitorenter    //进入同步方法
             7 getstatic #12 <java/lang/System.out>
            10 ldc #13 <hello synchronized>
            12 invokevirtual #14 <java/io/PrintStream.println>
            15 aload_1
            16 monitorexit //退出同步方法
            17 goto 25 (+8)
            20 astore_2
            21 aload_1
            22 monitorexit //退出同步方法
            23 aload_2
            24 athrow
            25 return
            ```
            从字节码中可以看出同步代码块的实现原理时通过monitorenter和monitorexit指令来实现的，其中monitorenter是同步代码块开始的地方，monitorexit是代码块结束的地方，
            当执行monitorenter指令时，当先线程将试图获取objectref(即锁对象)所对应的monitor持有权(即monitor对象对应的ObjectMonitor中)，当objectref的monitor的计数器为0时(ObjectMonitor中的_count为0)，
            那么线程会成功获objectref的monitor即锁，并将_count置为1，取锁成功。如果当前线程以及持有了objectref的monitor的持有权，那么它可以重入这个monitor，重入时会将_count的值自加1。
            徜若其他线程已经拥有的objectref的monitor的持有权，那当前线程被阻塞，直到正在执行的线程执行完毕，即monitorexit执行被执行，执行线程释放monitor并将_count置为0，其他线程才有机会去获取objectref的monitor。
            **值得注意的是，编译器会保证方法无论以何种方式结束(异常结束还是正常)，都会去调用monitorexit指令，使得一条monitorenter指令对应一条monitorexit，确保同步方法被正常的退出，编译器会自动生成一个处理所有异常异常处理器，它的目的就是用来执行monitorexit的**
        7. synchronized方法底层原理
            1. 方法级别的同步是隐式的，无需通过字节码指令来控制，它实现在方法调用和返回操作中。JVM可以在方法区常量池中的方法表结构(method_info Structure)中的ACC_SYNCHRONIZED访问标志区分一个方法是不是同步方法。
               当调用方法时，调用指令会先检查方法的ACC_SYNCHRONIZED访问标志是否被设置了，如果设置了，执行线程会先去持有monitor(管程)，然后再去执行方法，最后在方法完成(无论是正常退出还是异常退出)时释放monitor。
               在方法调用期间，执行线程持有了monitor，其他任何线程都无法再获取同一个monitor。如果一个同步方法执行期间抛出了异常，并且方法内部无法处理异常，那么这个同步方法持有的monitor将在异常抛到同步方法之外时自动释放
            2. 定义一个同步方法
                ```java
                private synchronized static void test2(){
                    System.out.println("do nothing");
                }
                ```
                反编译之后的字节码如下：
                ```text
                 private static synchronized void test2();
                    descriptor: ()V
                    flags: ACC_PRIVATE, ACC_STATIC, ACC_SYNCHRONIZED
                    Code:
                      stack=2, locals=0, args_size=0
                         0: getstatic     #12                 // Field java/lang/System.out:Ljava/io/PrintStream;
                         3: ldc           #15                 // String do nothing
                         5: invokevirtual #14                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
                         8: return
                    LineNumberTable:
                        line 60: 0
                        line 61: 8
                ```
               从字节码中可以看出，synchronized修饰的方法并没有monitorenter和monitorexit指令，取而代之的是ACC_SYNCHRONIZED访问标识，标识该方法是一个同步方法。JVM通过ACC_SYNCHRONIZED标志位来判断该方法是否是同步方法，从而进行相应的调用。
        8. JVM对synchronized的优化
            1. 锁的状态分为四种：无锁状态、偏向锁、轻量级锁和重量级锁，随着锁的竞争，锁可以从偏向所升级到轻量级锁，再升级到重量级锁，**锁的升级是单向的，也就是说锁只能从低到高升级，而不能降级**
            2. 偏向锁
                * 偏向锁是Java6之后新加的锁，它是一种针对加锁操作的优化手段。在大多数情况下，锁不仅不存在多线程竞争的情况，而且总是由同一线程多次获得，因此为了减少同一线程获取锁的代价(会涉及到一些CAS操作，比较耗时)而引入偏向锁。
                * 偏向锁的核心思想，如果一个线程获取了锁，那么锁进入偏向模式，此时Mark Word的结构也变成偏向锁结构，当这个线程再次请求时，无需再做任何同步操作，即可获得锁，这样省去了大量有关锁的申请操作，从而提高程序性能。
                * 对于没有锁竞争的场合，偏向锁由很好的优化效果，毕竟极有可能连续多次是同一个线程申请相同的锁。
                * 但是对于锁进展较为强烈的场合，偏向锁就失效了，因为这样的场合极有可能每次申请的线程都不一样，因此该场合下使用偏向锁会得不偿失，**需要注意的是：偏向锁失效后并不会立即膨胀为重量级锁，而是先升级为轻量级锁**
            3. 轻量级锁
                * 倘若偏向锁失效，JVM并不会将锁立即升级成重量级锁，JVM会尝试使用一中称为轻量级锁的优化手段(Java6之后加入)，此时Mark Word的结构变为轻量级锁结构。
                * 轻量级锁的核心思想：对绝大部分锁，再整个同步周期内不存咋竞争，**注意：这是经验之谈，需要了解的是，轻量级锁锁使用的场景是线程交替执行同步代码块的场景，如果存在同一时间访问同一锁的场合，轻量级锁就会膨胀为重量级所**
            4. 自旋锁
                * 轻量级锁失效后，JVM为了避免线程真实的在操作系统中挂起，还会进行一段称为自旋锁的优化手段。
                * 自旋锁的核心思想：在大多数情况下，线程持有锁的时间不会太长，如果直接挂起操作系统层面的线程可能会得不偿失，毕竟操作系统实现线程之间的切换时需要从用户态转换成核心态，这个状态的转换的需要相对较长的时间，时间成本较高，
                  因此自旋锁假设在不久的将来，当前线程可以获取到锁，因此JVM会让当前想要获取锁的线程做几个空循环(这就是被称为自旋的原因)，一般不会太久，可能时50~100个循环，在经过若干次循环后，如果得到了锁，就进入临界区，如果还获取不到锁，
                  那就会将线程在操作系统层面挂起，这就是自旋锁的优化方法，可以提升效率
                * 如果自旋锁还是获取不到锁的话，最终膨胀成重量级锁
            5. 软消除
                * 软消除是JVM的另一种锁的优化，这种优化更彻底，Java在JIT(Just In Time，即时编译)，通过上下文扫描，去除不可能存在共享资源竞争的锁，通过这种方法消除没有必要的锁，可以节省毫无意义的请求锁的时间。
                ```java
                /**
                 * 消除StringBuffer同步锁
                 */
                public class StringBufferRemoveSync {
                
                    public void add(String str1, String str2) {
                        //StringBuffer是线程安全,由于sb只会在append方法中使用,不可能被其他线程引用
                        //因此sb属于不可能共享的资源,JVM会自动消除内部的锁
                        StringBuffer sb = new StringBuffer();
                        sb.append(str1).append(str2);
                    }
                
                    public static void main(String[] args) {
                        StringBufferRemoveSync rmsync = new StringBufferRemoveSync();
                        for (int i = 0; i < 10000000; i++) {
                            rmsync.add("abc", "123");
                        }
                    }
                
                }
                ```
        9. synchronized的关键点
            1. synchronized的可重入性
                * 从互斥锁的设计上来说，当一个线程试图操作一个由其他线程持有的对象锁上网临界区时，将会处于阻塞状态，但当一个线程再次请求自己持有对象锁的临界区资源时，这种情况属于重入锁，请求将会成功，在Java中synchronized时基于原子性的内部锁机制，
                  是可重入的，因此在一个线程调用synchronized方法的同时在其他方法内部该对象的另一个synchronized方法时，也就是说一个线程得到一个对象锁后再次请求该对象锁时允许的，这就是synchronized可重入性
                  ```java
                    /**
                     * 测试下synchronized的可重入锁
                     * synchronized是可重入锁->线程child1获得了Child child = new Child();这个实例的锁，再去调用这个对象中其他的加锁方法也是被允许的不需要重新申请锁(包括父类的synchronized方法)
                     * 但如果synchronized调用的方法的锁被其他线程持有，那还是要等锁释放之后才能获得锁去执行。
                     *
                     * @author zhengwei AKA Awei
                     * @since 2019/7/18 11:01
                     */
                    public class TestSynchronizedReentrant {
                    	public static void main(String[] args) {
                    		Child child = new Child();
                    		Parent parent = new Parent();
                    //		new Thread(parent::parentDoSomething, "parent1").start();
                    //		new Thread(parent::parentDoSomething, "parent2").start();
                    		new Thread(Parent::parentDoSomething, "parent1").start();
                    		new Thread(child::childDoSomething, "child1").start();
                    	}
                    }
                    
                    class Parent {
                    	synchronized static void parentDoSomething() {
                    		try {
                    			System.out.println("Parent do something ->" + Thread.currentThread().getName() + " start");
                    			Thread.sleep(100_000L);
                    			System.out.println("Parent do something ->" + Thread.currentThread().getName() + " end");
                    		} catch (InterruptedException e) {
                    			e.printStackTrace();
                    		}
                    	}
                    }
                    
                    class Child extends Parent {
                    	synchronized void childDoSomething() {
                    		try {
                    			System.out.println("Child do something ->" + Thread.currentThread().getName() + " start");
                    //			Thread.sleep(5_000L);
                    			childDoAnotherThing();
                    			System.out.println("Child do something ->" + Thread.currentThread().getName() + " end");
                    		} catch (Exception e) {
                    			e.printStackTrace();
                    		}
                    	}
                    
                    	private synchronized void childDoAnotherThing() {
                    		try {
                    			System.out.println("Child do another thing ->" + Thread.currentThread().getName() + " start");
                    //			Thread.sleep(5_000L);
                    			parentDoSomething();
                    			System.out.println("Child do another thing ->" + Thread.currentThread().getName() + " end");
                    		} catch (Exception e) {
                    			e.printStackTrace();
                    		}
                    	}
                    }
                  ```
                  正如代码所演示的，如果一个线程获得某个实例对象的monitor之后进入synchronized代码块中，并在同步代码调用了该实例的另一个同步代码块，再次请求时，江北允许，这就是重入的最直接体现，
                  **需要注意的时，子类也是可以通过重入锁调用父类的同步方法，注意由于synchronized是基于monitor实现的，因此每重入一次，monitor的_count自加1**
            2. 线程中断与synchronized
                * 中断线程，把线程的中断标志位置为true
                * Java提供三个中断方法
                    ```java
                    //中断线程（实例方法）
                    public void Thread.interrupt();
                    
                    //判断线程是否被中断（实例方法）
                    public boolean Thread.isInterrupted();
                    
                    //判断是否被中断并清除当前中断状态（静态方法）
                    public static boolean Thread.interrupted();
                    ```
                * 对于NEW和TERMINATED状态的线程来说，调用interrupt是没有任何意义的，Java会对这两种状态的线程忽略interrupt
                * 对于RUNNABLE状态的线程(没有获取到CPU执行权的线程)，调用interrupt之后，只是设置了该线程的中断标志，并不会让线程实际中断，想要发现线程是否真的中断需要用去程序去判断那么问题来了，我们既然不能真正的中断，那么要我们要中断标志干嘛呢？这里其实是把线程的生杀大权交给我们自己去判断，Java给我们一个标志位来让让我们自己去决策接下来要干嘛，我们可以判断标志位然后去中断我们的程序而不是强制的终止系统
                * 对于BLOCKED状态(竞争CPU执行权失败而被挂起的线程)的线程来说，发起interrupt方法只是会设置中断的标志位，并不会对线程造成影响
                * WAITING和TIMED_WAITING状态(sleep,wait,join...)的线程，在触发interrupt的时候会抛出InterruptedException异常，同时会设置中断标志
                * 具体代码参见zhengwei.thread.chapter04.ThreadInterrupt
            3. 等待唤醒机制与synchronized
                * 所谓的等待唤醒机制重要指的是 `wait(),notify(),notifyAll()` 这三个方法，在使用这三个方法时，必须是在synchronized代码块中或者synchronized方法中，否则会抛出IllegalMonitorStateException异常，这是因为调用这几个方法前必须拿到当前对象的监视器monitor对象，也就是说这三个方法依赖于monitor对象
                * 在前面的分析中我们知道monitor存在与对象头中的Mark Word中(存储monitor对象的指针)，而synchronized关键字可以获取monitor，这就是为什么 `wait(),notify(),notifyAll()` 这三个方法要在synchronized代码块内部执行的原因了
                * 需要特别注意的一点是：wait方法会释放锁，sleep方法不会释放锁，线程wait之后，直到notify或notifyAll将其唤醒方能继续执行。
    2. JMM
        1. 理解Java内存区域与Java内存模型
            1. Java内存区域(这里不做过多阐述，详情参见[zhengwei.jvm.JVM.md](https://github.com/zw030301/playground/blob/master/src/main/java/zhengwei/jvm/JVM.md))
                1. 方法区(Method Area)：线程共享，又名Non-heap，只要用于存储已被JVM加载的类的信息、常量、静态变量，即时编译器编译之后的代码等数据，根据JVM规范当方法区无法满足内存分配需求时，将会派出OutOfMemoryError异常。
                                       值得注意的是，方法区存在一个运行时常量池(Runtime Constant Pool)的区域，它主要存储编译器生成的各种字面量和符号引用，这些内容将在类加载后放入运行时常量池中，以便后续使用
                2. 堆(Heap)：线程共享，在虚拟机启动时创建，主要存放对象的实例，几乎所有的对象都在这里分配内存，GC的主要收集区域就是堆，根据JVM规范规定，如果堆中没有内存来完成实例分配，并且堆无法扩展，将会抛出OutOfMemoryError异常
                3. 程序计数器(Program Counter Register)：线程私有，是JVM中最小的一块内存区域，主要记录当前线程所执行的字节码行号指示器。字节码解释器工作时，通过改变这个计数器的值来选取下一条需要执行的字节码指令，分支、循环、跳转和线程恢复都要依赖程序计数器
                4. 栈(Stack)：线程私有，生命周期与线程相同，代表Java方法执行的内存模型。每个线程都会创建一个栈帧来存储方法的变量表、操作数栈、动态链接方法、返回值和返回地址信息。每个方法调用都对应这一个栈帧的入栈和出栈的过程
                5. 本地方法栈(Native Method Stack)：线程私有，这部分主要与JVM调用到Native方法时需要使用
            2. JMM(Java内存模型)
                **特别注意：JMM和JVM内存是两个不同的层次划分**
                1. JMM本身是一种抽象的概念，并不真实存在，它描述的是一组规则与规范，通过这个规则和规范定义了程序中各个变量(包括实例字段、静态字段和构成数组对象的元素)的访问方式，由于JVM允许程序的实体是线程，每个线程的创建JVM都会为其创建一个工作内存(有些地方称为栈空间)，用于存储线程私有的数据，
                   而JMM规定所有变量都存储在主内存中，主内存是共享区域，所有线程都可以访问，但线程对变量的操作(读取赋值等)必须在工作内存中进行，首相将变量从主内存拷贝到自己的工作内存，然后对变量进行操作，操作完后再将变量写回主内存，不能直接操作主内存中的数据，工作内存存储着从主内存中拷贝来的变量副本，
                   前面说过，工作内存是线程私有的，因此不同的线程之间无法访问对方的工作内存，线程的通信与传值必须通过主存来完成。
                2. 需要注意的是，JMM与Java内存区域的划分是两个不同的概念，更恰当的说JMM是描述的一组规则，通过这组规则控制程序中各个变量在共享区域和私有数据区域的访问方式，JMM围绕原子性、有序性和可见性展开。JMM和Java内存区域唯一相似点：都存在共享数据区域和私有数据区域。
                   **在JMM中主存属于共享数据区域，从某种程度上讲应该包含了堆和方法区，而工作内存数据属于私有数据区域，从某种程度上讲应该包含程序计数器、虚拟机栈和本地方法栈**。许我们在某些地方，我们可能回看到主内存被描述为**堆内存**，工作内存被描述为**线程栈**，实际上是同一个含义
                    1. 主内存
                        * **主要存储的是Java实例**，所有线程创建的实例对象都存放在主内存中，**不管该实例对象是成员变量还是方法中的本地变量(即局部变量)**，当然也包括了共享的类信息、常量、静态变量。由于是共享数据区域，多条线程对同一个变量的访问可能会发生线程安全问题
                    2. 工作内存
                        * 主要存储当前方法的所有本地变量(**工作内存中存储的是主内存中的变量的拷贝**)，每个线程只能访问自己的工作内存，即线程中的本地变量对其他线程是不可见的，就算是两个线程执行的是同一段代码，他们也会各自在自己的工作内存中创建属于当前线程的本地变量，当然包括程序计数器、相关Native方法信息，
                          **注意由于工作内存是线程私有的，线程间无法相互访问工作内存，因此存储在工作内存中的变量不存在线程安全问题**
                3. 在明白主内存和工作内存之后，接下来了解下主内存与工作内存的数据存储类型，以及操作方式。根据虚拟机规范，对于一个实例变量中的成员方法而言，如果方法中包含本地变量是基本数据类型(int,float,double,char,byte,boolean,long,short)，将直接存储在工作内存的栈帧上，
                   但倘若本地变量是引用类型的变量，那么该变量的引用会存储在工作内存的栈帧中，而引用类型的实例变量将存储在主内存中(共享数据区，堆)中。但对于实例对象的成员变量，不管它是基本数据类型或者包装类型或是引用类型，都会被存储到堆区。
                   至于static变量以及类本身相关信息都会被存储到存储在主存上(Hotspot虚拟机是存储在方法区中)。
                   需要注意的是，在主内存中的实例对象是线程共享的，如果两个线程同时调用了同一对象的同一方法，那么两条线程会将操作的数据拷贝进自己的工作内存中，待任务执行完毕之后再将结果刷新回主内存中。
                4. 硬件架构与Java内存模型
                    1. 硬件内存架构
                        * 现代的CPU架构，一个CPU拥有多个CPU核心，每个CPU核心都会有CPU缓存(L1,L2,L3缓存)，从多线程的角度来看，每条线程都会映射到一个CPU核心去运行。在CPU内部还有一组寄存器，寄存器中存放的是CPU直接访问和处理的数据，是一个临时存放数据的地方，一般CPU都会去主存中取数据到寄存器中，然后进行处理，但由于内存的处理速度远远低于CPU的速度，导致CPU在处理指令的时候往往花费很多时间在等待内存做准备工作，于是在CPU和内存之间添加一个CPU缓存区，CPU缓存很小，但是速度比内存快很多。如果CPU总是操作内存的话，那么效率会很低，此时CPU可以把从内存提取到的数据暂时保存起来，如果寄存器要取内存同一位置的的数据，直接从缓存中获取，无需从主存存取。需要注意的是：CPU并不是每次都从缓存中获取数据，如果需要获取的指令的没有缓存在CPU缓存的话，CPU还是会从主存中获取数据，即没有命中，如果缓存中有的话，那么直接从主存中获取，即命中。
                        * 总而言之当一个CPU需要访问主存时，会先读取一部分主存数据到CPU缓存(当然如果CPU缓存中存在需要的数据就会直接从缓存获取)，进而在读取CPU缓存到寄存器，当CPU需要写数据到主存时，同样会先刷新寄存器中的数据到CPU缓存，然后再把数据刷新到主内存中。
                    2. Java线程与硬件处理器
                        * JVM中线程的实现原理是基于一对一的线程模型，所谓一对一线程模型，实际上就是通过语言级别层面程序去间接调用系统内核的线程模型，即我们在使用Java线程时，JVM是转而调用当前操作系统的内核线程来完成任务的。
                          > 内核线程(Kernel-Level Thread,KLT)它是由操作系统内核支持的线程，这种线程是有操作系统来完成线程切换，内核通过操作调度器进而对线程进行调度，并将线程的任务映射到各个处理器上。每个内核线程可以视为内核的一个分身，也就是操作系统可以同时处理多任务的原因。
                        * 由于我们编写的多线程是基于语言层面的，程序一般不直接调用内核线程，取而代之的是一种轻量级进程(Light Weight Thread)，也就是通常意义上的线程，由于每个轻量级线程都会对应到一个内核线程，因此我们可以通过轻量级进程调用内核线程，进而由操作系统内核将任务映射到各个处理器，这种轻量级进程与内核线程间一对一的线程模型就叫一对一线程模型。
                    3. Java内存模型与硬件架构的关系
                        * 多线程的执行最终都会映射到硬件处理器上去运行，但Java内存模型与硬件内存架构并不完全一致。对于硬件来说，只有寄存器、缓存、主存的概念，并没有工作内存(线程私有区域)和主内存之分(堆内存)，也就是说Java内存模型对内存的划分对硬件内存并没有任何影响，因为JMM只是一种抽象的概念，是一组规则，并不实际存在。
                          不管是工作内存的数据还是主内存中的数据，对于计算机而言都会存储在计算机主存中，当然也有可能存储在缓存或者寄存器中，因此总体上来说，Java内存模型和硬件架构是一个相互交叉的关系，是一种抽象概念划分与真实物理硬件的划分(注意对于JVM的内存区域划分也是同样的道理)
                    4. Java内存模型存在的必要性
                        * 由于JVM运行程序的实体是线程，而每个线程的创建时JVM都会为其创建一个工作内存(栈空间)，用于存储用于存储线程私有的数据，线程与主内存中的变量操作必须通过工作内存间接完成，主要过程是每条线程把主内存中的变量拷贝到自己的工作内存空间中，然后对变量进行操作，操作完成后将变量写回主内存中，如果存在两条线程同时对一个主内存中的实例对象的变量进行操作就有可能触发线程安全的问题。
                        * 为了解决线程安全的问题，JMM定义了一组规则，通过这组规则来决定一个线程对共享变量的写入何时对另一个线程可见，这组规则就叫Java内存模型(JMM),JMM围绕着程序执行的原子性、有序性和可见性展开
        2. Java内存模型的承诺
            > 主要围绕原子性、有序性和可见性
            1. 原子性
                1. 原子性指的是一个操作是不可中断的，即使是在多线程环境下，一个操作一旦开始将不会受其他的线程的影响。一组操作要么全部都执行要么全部都不执行。
                2. JVM保证基本数据类型int,byte,short,float,boolean,char,long,double的读写都是原子性的，但是对于32位虚拟机而言，long和double的基本数据类型的读写不是原子性的，因为32位虚拟机每次原子读写都是32位的，但是long和double是64位的的存储单元，这样会导致一个线程A在写时，原子操作完前32位数据后，轮到线程B去读取时，恰好读取到了后32位数据，这样可能会读取到一个即非原值又不是线程修改的变量，它可能时个"半个变量"的数值，即64位数据被两个线程分成了两次读取，但是不必担心，因为读到半个变量的情况比较少见，至少在目前的商业虚拟机中，对64位的数据读写操作作为原子操作来执行的。
            2. 指令重排序
                * 计算机在执行程序时，为了提高性能，编译器和处理器会对代码做一些优化，即指令重排序，
                    1. 编译器优化的指令重排序
                        * 编译器在不改变单线程程序语义的情况下，可以重新安排程序语句的执行顺序
                    2. 指令并行重排序
                        * 现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性(即后一个执行的语句无需依赖前面执行的语句的结果)，处理器可以改变语句对应的机器码指令的执行顺序
                    3. 内存系统的重排序
                        * 由于处理器使用缓存和读写缓冲区，这使得加载(load)和存储(store)操作看上去是乱序执行的，因为三级缓存的存在，导致内存与缓存的数据同步存在时间差。
                    4. 其中编译器优化的重排序数据编译期重排，指令并行的重排和内存系统的重排属于处理器重排，在多线程环境下，这些重排优化可能导致程序出现内存可见性问题。
                    5. 指令重排序只会保证单线程中串行语义执行的一致性，但并不关系线程间的语义一致性
            3. 可见性
                * 可见性是指当一个线程修改了某个共享变量后其他线程是能够立即得到修改后的值。
                * 工作内存与主内存同步延迟现象就造成了可见性问题，另外指令重排以及编译器优化也可能导致可见性问题
            4. 有序性
                * 有序性是指在单线程环境下，我们总是认为代码的执行是按顺序执行的，这样理解并没有什么问题，指令重排序保证单线程环境下的语义一致性，但是对于多线程而言，则可能出现乱序现象，因为程序编译成机器码指令后可能会出现指令重排序现象，重排后的指令与原指令的顺序未必一致。
                * 在Java程序中，倘若在本线程中，所有操作都视为有序行为，如果是多线程环境下，一个线程中观察另一个线程，所有操作都是无序的，前半句指的是单线程内保证串行语义的一致性，后半句指的是治理重排序现象和工作内存与主内存同步延迟现象。
            5. JMM提供的解决方案
                * 在Java内存模型中都提供一套解决方案供Java工程师在开发过程使用，如原子性问题，除了JVM自身提供的对基本数据类型读写操作的原子性外，对于方法级别或者代码块级别的原子性操作，可以使用synchronized关键字或者重入锁(ReentrantLock)保证程序执行的原子性，工作内存与主内存同步延迟现象导致的可见性问题，可以使用synchronized关键字或者volatile关键字解决，它们都可以使一个线程修改后的变量立即对其他线程可见。对于指令重排导致的可见性问题和有序性问题，则可以利用volatile关键字解决，因为volatile的另外一个作用就是禁止重排序优化，关于volatile稍后会进一步分析。除了靠sychronized和volatile关键字来保证原子性、可见性以及有序性外，JMM内部还定义一套happens-before 原则来保证多线程环境下两个操作间的原子性、可见性以及有序性。