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
    //如果获取锁失败并且加入到CLH队列失败的话，则打断当前线程
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
//将竞争资源的Thread的Node加到队列的队尾
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        //如果队尾为null，即队列还没有初始化的时候
        if (t == null) { // Must initialize
            //用CAS算法将一个临时的 空Node节点设置为head节点
            if (compareAndSetHead(new Node()))
                //同时tail节点也指向head节点
                //至此FIFO双向队列被初始化为：t -> tail -> head -> Node
                tail = head;
        } else {//如果队列已经被初始化过了，或者tail不为null
            //将当前竞争资源的Thread的Node的前驱节点指向t(tail)
            //将node节点放到队列的最后
            node.prev = t;
            //将node设置为tail节点
            if (compareAndSetTail(t, node)) {
                //将之前的tail节点的后驱节点指向node
                t.next = node;
                //返回之前的tail节点
                return t;
            }
        }
    }
}
```
上面的代码执行完毕后，竞争资源失败的线程将会加到队列的尾部
```text
 ------      ----      
| Node | -> | T2 |
 ------      ----      
   |           |
   |           |
--------     ------
| head |    | tail |
--------     ------
head和tail类似于一个标识，标识了这个节点的位置属性。
```
在addWaiter方法后会返回关于当前线程的Node信息，接下来就进入了 `acquireQueued(Node, int)` 方法了。
```java
//尝试加锁
//当前线程尝试获取锁，获取不到挂起线程
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        //自旋，这里有个死循环来确保没竞争到资源的线程被挂起
        for (;;) {
            //获取当前竞争资源的Thread的Node的前一个节点
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
            //如果尝试获取资源失败了，那么应该被挂起
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
//如果尝试加锁失败，那么挂起线程
//将节点为cancelled状态的节点移除，将节点waitStatus为0的(int默认是0)节点置为SIGNAL(-1)
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    //node节点的前一个节点的等待状态
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * This node has already set status asking a release
         * to signal it, so it can safely park.
         */
        return true;
    if (ws > 0) {//如果节点的状态是CANCELLED的话，那么移除节点
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
        //把所有cancelled节点移除后，将当前竞争资源的线程的前一个节点的waitStatus置为SIGNAL状态
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
//挂起线程并且检查线程的打断状态
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```
### `NonfairSync` 释放锁
释放锁的主要方法是 `release(int)` 
```java
public final boolean release(int arg) {
    //如果释放锁成功了
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```
最终调用的是AQS的 `tryRelease` 方法。
```java
protected final boolean tryRelease(int releases) {
    //如果一个线程的获取多次锁
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;、
    //如果所有锁都已释放完毕
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```
唤醒线程
```java
//唤醒head节点的下一个节点
//这个node是队列的head节点
private void unparkSuccessor(Node node) {
    /*
     * If status is negative (i.e., possibly needing signal) try
     * to clear in anticipation of signalling.  It is OK if this
     * fails or if status is changed by waiting thread.
     */
    int ws = node.waitStatus;
    if (ws < 0)
        //将head节点的waitStatus状态置为0
        compareAndSetWaitStatus(node, ws, 0);

    /*
     * Thread to unpark is held in successor, which is normally
     * just the next node.  But if cancelled or apparently null,
     * traverse backwards from tail to find the actual
     * non-cancelled successor.
     */
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    //唤醒head的下一个节点s
    if (s != null)
        LockSupport.unpark(s.thread);
}
```
被唤醒的线程会在之前被阻塞的地方继续执行，即还是继续执行acquireQueued的自旋for循环中。
### 对于ReentrantLock的公平锁和非公平锁
### 非公平锁
对于非公平锁的运行流程，假设现在有t1,t2,t3三个线程：假设t1已经获取到了锁，在t2和t3都去获取锁的时候，都会先去尝试修改state的值(即先去
获取锁，而不是先去加入到等待队列)，如果先获取锁失败了，那么就执行 `acquire` 方法，实际执行的是 `nonfairTryAcquire` 方法，加锁失败则
加入到队列中，加锁成功则继续线程接下来的逻辑。  
当t1释放锁，唤醒了t2，t2通过`tryAcquire`方法借助CAS去修改state的值，在此时，t3也过来也通过`tryAcquire`方法修改state的值。如果t3修
改state成功了，那么t3就获取到了锁资源，t2仍然在队列中等待，这就是非公平锁的体现。  
非公平锁在最坏的情况下会出现线程饥饿的情况，可能有点线程永远得不到唤醒。非公平锁的效率比公平锁的效率要高。
### 公平锁
对于公平锁，它单独重新实现了 `tryAcqurie` 方法，无论是在队列中的数据还是新来的线程，具体代码如下，和非公平锁的加锁逻辑有些许不同
```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        //这里相比较于非公平锁多了一个hasQueuedPredecessors的判断
        if (!hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```
hasQueuedPredecessors方法是用来判断AQS等待队列中是否存在元素，如果有其他等待的线程，那么当前线程会被加入到等待队列的尾部，做到真正的
先来后到，有序加锁。 `hasQueuedPredecessors` 方法的代码如下：
```java
//返回false代表队列中没有节点或者仅有一个节点是当前线程创建的节点。返回true则代表队列中存在等待节点，当前线程需要入队等待。
public final boolean hasQueuedPredecessors() {
    // The correctness of this depends on head being initialized
    // before tail and on head.next being accurate if the current
    // thread is first in queue.
    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    return h != t &&
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```
先判断head是否等于tail，如果队列中只有一个Node节点，那么head会等于tail，接着判断head的后置节点，这里肯定会是null，如果此Node节点对
应的线程和当前的线程是同一个线程，那么则会返回false，代表没有等待节点或者等待节点就是当前线程创建的Node节点。此时当前线程会尝试获取锁。

如果head和tail不相等，说明队列中有等待线程创建的节点，此时直接返回true，如果只有一个节点，而此节点的线程和当前线程不一致，也会返回true
### 非公平锁和公平锁的区别
非公平锁和公平锁的区别：非公平锁性能高于公平锁性能。非公平锁可以减少CPU唤醒线程的开销，整体的吞吐效率会高点，CPU也不必取唤醒所有线程，
会减少唤起线程的数量

非公平锁性能虽然优于公平锁，但是会存在导致线程饥饿的情况。在最坏的情况下，可能存在某个线程一直获取不到锁。不过相比性能而言，饥饿问题可以
暂时忽略，这可能就是ReentrantLock默认创建非公平锁的原因之一了。
## Condition
Condition是在java 1.5中才出现的，它用来替代传统的Object的`wait()`、`notify()`实现线程间的协作，相比使用Object的`wait()`、`notify()`，
使用Condition中的`await()`、`signal()`这种方式实现线程间协作更加安全和高效。因此通常来说比较推荐使用Condition

其中AbstractQueueSynchronizer中实现了Condition中的方法，主要对外提供await(Object.wait())和signal(Object.notify())调用。
### await
```java
public final void await() throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    //将当前线程加入到Condition队列中
    //Condition队列和AQS的阻塞队列是两个独立的队列
    Node node = addConditionWaiter();
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    while (!isOnSyncQueue(node)) {
        //挂起该节点
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null) // clean up if cancelled
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```
```java
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // If lastWaiter is cancelled, clean out.
    if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    //将当前线程封装成一个Node节点，并且该Node的模式为CONDITION
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    lastWaiter = node;
    return node;
}
```