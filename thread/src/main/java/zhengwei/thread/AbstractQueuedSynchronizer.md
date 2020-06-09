# AbstractQueueSynchronizer(抽象队列同步器)
Java中的同步工具都是基于AQS来完成的。
## 实现原理
AQS内部维护了一个共享变量 `private volatile int state; (The synchronization state.)` 代表共享资源，和一个 `FIFO` 线程等待队列，
(多线程发生竞争阻塞时进入此队列)，这里volatile确保内存可见性，当 `state = 1` 时代表此时已有线程加锁，其他线程来加锁则会失败，加锁失
败的线程会被放入一个 `FIFO` 的一个队列，然后被 `UNSAFE.park()` 挂起，等待其他获取锁得线程释放锁或被其他线程唤醒。

另外 `state` 得所有操作都是通过 `CAS` 来确保操作得原子性和线程安全
AQS中提供了很多关于锁的实现方法：
1. getState():获取锁的标志位
2. setState():设置锁的标志位
3，tryAcquire(int):独占方式获取锁。尝试获取资源，成功返回 `true` ，失败返回 `false`
4. tryRelease(int):独占方式释放锁。尝试释放资源，成功返回 `true` ，失败返回 `false`
主要以 `ReentrantLock` 作为例子进行讲解。
## ReentrantLock
ReentrantLock有两种实现模式，一种是公平锁 `FairSync` ，一种是非公平锁 `NonfairSync` ，以非公平锁为例
### `NonfairSync`加锁lock成功
```java
final void lock() {
    // 能否将state设置成1
    // 能够设置成1代表此时锁没有线程占用
    if (compareAndSetState(0, 1))
        // 设置当前独占锁的线程为当前线程
        setExclusiveOwnerThread(Thread.currentThread());
    else
        // 如果此时的state不为0，代表已经有线程占用了锁
        // 需要进行额外的后续操作
        // 此操作也是可重入锁的体现，参数 int 就是冲入的次数
        acquire(1);
}
protected final boolean compareAndSetState(int expect, int update) {
    // See below for intrinsics setup to support this
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
//设置拥有锁的线程
protected final void setExclusiveOwnerThread(Thread thread) {
    exclusiveOwnerThread = thread;
}
```
如果线程通过CAS抢占资源成功，则将state置为1，并且将该线程设置为拥有资源的线程。
### `NonfairSync`加锁lock失败
抢占的失败的情况是在lock的时候，state变成了1，其他别的线程再来抢占资源时去修改state的值时必然会失败，此时，抢占失败的线程就会被加入到
一个FIFO的等待队列，通过UNSAFE的park()挂起。
```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
//尝试上锁
protected final boolean tryAcquire(int acquires) {
    return nonfairTryAcquire(acquires);
}
/**
 * Performs non-fair tryLock.  tryAcquire is implemented in
 * subclasses, but both need nonfair try for trylock method.
 */
final boolean nonfairTryAcquire(int acquires) {
    //获取当前的线程
    final Thread current = Thread.currentThread();
    //获取目前的state的值
    int c = getState();
    //如果state为0的话(等于二次判断是否有线程占有锁)
    if (c == 0) {
        //上锁
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    //可重入的体现
    //如果当前锁已经被抢，但加锁的线程是之前持有锁的线程的话
    //直接将state加上int值
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```
`nonfairTryAcquire()` 方法中首先会获取state的值，如果不为0则说明当前对象的锁已经被其他线程所占有，接着判断占有锁的线程是否为当前线程，
如果是则累加state值，这就是可重入锁的具体实现，累加state值，释放锁的时候也要依次递减state值。
如果state为0，则执行CAS操作，尝试更新state值为1，如果更新成功则代表当前线程加锁成功。

>如果T1加锁成功，那么state的值为1，T2来加锁时，就会去执行 `acquire(int)` 方法，最终调用 `nonfairTryAcquire(int)` 方法，因为当前
>持有锁的是T1，那么T2执行这个方法会返回false，然后将T2通过addWaiter(Node.EXCLUSIVE)方法加入到FIFO队列中；如果再次加锁的是T1的
>话，那么在原来的state的基础上加上acquires的值，这也是可重入的表现。
### 对于加锁失败进入到FIFO队列
```java
//是将node添加到FIFO的队尾
private Node addWaiter(Node mode) {
    //创建一个队列的节点Node，此Node包含当前竞争资源失败的线程
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    //如果此时的队尾不为null
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    //如果此时队尾为null。则调用enq方法，将竞争失败的线程加到队列的尾部
    enq(node);
    return node;
}
//将Node加到队列的队尾
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```
上面的代码执行完毕后，竞争资源失败的线程将会加到队列的尾部
```text
--------     ------      ----       ------
| head | -> | Node | -> | T2 | ->  | tail |
--------     ------      ----       ------
其实head也是一个普通的Thread封装的Node节点，head节点的Thread将会被唤醒
```
在addWaiter方法后会返回关于当前线程的Node信息，接下来就进入了 `acquireQueued(Node, int)` 方法了。
```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            //获取当前Node的前一个节点
            final Node p = node.predecessor();
            //如果p当前Node的前一个节点是head，那么尝试获取锁
            if (p == head && tryAcquire(arg)) {
                //加锁成功后将当前节点设置成head
                setHead(node);
                //方便GC进行回收资源
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            //如果加锁失败或者前一个节点不是head
            //
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * This node has already set status asking a release
         * to signal it, so it can safely park.
         */
        return true;
    if (ws > 0) {
        /*
         * Predecessor was cancelled. Skip over predecessors and
         * indicate retry.
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        /*
         * waitStatus must be 0 or PROPAGATE.  Indicate that we
         * need a signal, but don't park yet.  Caller will need to
         * retry to make sure it cannot acquire before parking.
         */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```