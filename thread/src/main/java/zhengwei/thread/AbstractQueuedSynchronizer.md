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
## 重要成员变量和方法
### 成员变量和方法

|  方法和属性值     | 含义                    |
|-------------|-----------------------|
| waitStatus  | 当前节点在队列中的状态           |
| thread      | 表示处于该节点的线程            |
| prev        | 前驱指针                  |
| predecessor | 返回前驱节点，没有的话抛出npe      |
| nextWaiter  | 指向下一个处于CONDITION状态的节点 |
| next        | 后继指针                  |
waitStatus枚举如下：

|  枚举       | 含义                           |
|-----------|------------------------------|
| 0         | 当一个Node被初始化的时候的默认值           |
| CANCELLED | 为1，表示线程获取锁的请求已经取消了           |
| CONDITION | 为-2，表示节点在等待队列中，节点线程等待唤醒     |
| PROPAGATE | 为-3，当前线程处在SHARED情况下，该字段才会使用 |
| SIGNAL    | 为-1，表示线程已经准备好了，就等资源释放了      |
### 线程锁模式
| 模式        | 含义              |
|-----------|-----------------|
| SHARED    | 表示线程以共享的模式等待锁   |
| EXCLUSIVE | 表示线程正在以独占的方式等待锁 |
### 同步状态state
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
    //标记是否成功拿到资源
    boolean failed = true;
    try {
        //标记等待过程是否被中断过
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
        //如果获取资源失败的，那么就将此节点的状态标记为cancel
        if (failed)
            cancelAcquire(node);
    }
}
//检测是否需要挂起线程，当且仅当当前的Node的前一个节点的waitStatus为-1时返回true，
//之后此Node中的Thread会通过parkAndCheckInterrupt被挂起
//当返回false时，那么则会继续去竞争资源直到返回true
//将节点为cancelled状态的节点移除，将节点waitStatus为0的(int默认是0)节点置为SIGNAL(-1)
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    //pred为node的前一个节点
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
//将节点的状态标记为取消
private void cancelAcquire(Node node) {
    // Ignore if node doesn't exist
    if (node == null)
        return;
    //将此node的thread置空，那么此node为虚node
    node.thread = null;
    // Skip cancelled predecessors
    Node pred = node.prev;
    //过滤cancel节点
    while (pred.waitStatus > 0)
        node.prev = pred = pred.prev;

    // predNext is the apparent node to unsplice. CASes below will
    // fail if not, in which case, we lost race vs another cancel
    // or signal, so no further action is necessary.
    Node predNext = pred.next;

    // Can use unconditional write instead of CAS here.
    // After this atomic step, other Nodes can skip past us.
    // Before, we are free of interference from other threads.
    node.waitStatus = Node.CANCELLED;

    // If we are the tail, remove ourselves.
    //如果此node为尾节点，那么将从后往前的第一个非取消状态的节点设置为tail
    //更新失败的话进入else，如果更新成功将tail的后继节点设置为null
    if (node == tail && compareAndSetTail(node, pred)) {
        compareAndSetNext(pred, predNext, null);
    } else {
        // If successor needs signal, try to set pred's next-link
        // so it will get one. Otherwise wake it up to propagate.
        int ws;
        //如果当前线程不是head的后继节点：1.判断当前节点的waitStatus是否为SIGNAL；2.如果不是，则尝试将前驱节点设置为SIGNAL看是否成功
        //如果1和2中有一个为true，再判断当前节点的线程是否为null
        //如果上述条件都满足，把当前节点的前驱节点的后驱指针指向当前节点的后继节点
        if (pred != head &&
            ((ws = pred.waitStatus) == Node.SIGNAL ||
             (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
            pred.thread != null) {
            Node next = node.next;
            if (next != null && next.waitStatus <= 0)
                compareAndSetNext(pred, predNext, next);
        //如果当前节点是head的后继节点，或者上述条件不满足，那就唤醒当前节点的后继节点
        } else {
            unparkSuccessor(node);
        }

        node.next = node; // help GC
    }
}
```
>通过上面的流程，我们对于CANCELLED节点状态的产生和变化已经有了大致的了解，但是为什么所有的变化都是对Next指针进行了操作，而没有对Prev指针进行操作呢？什么情况下会对Prev指针进行操作？  
>执行cancelAcquire的时候，当前节点的前置节点可能已经从队列中出去了（已经执行过Try代码块中的shouldParkAfterFailedAcquire方法了），
>如果此时修改Prev指针，有可能会导致Prev指向另一个已经移除队列的Node，因此这块变化Prev指针不安全。 
>shouldParkAfterFailedAcquire方法中，会执行下面的代码，其实就是在处理Prev指针。
>shouldParkAfterFailedAcquire是获取锁失败的情况下才会执行，进入该方法后，说明共享资源已被获取，当前节点之前的节点都不会出现变化，
>因此这个时候变更Prev指针比较安全。
>```java
>do {
> 	node.prev = pred = pred.prev;
> } while (pred.waitStatus > 0);
>```
### `NonfairSync` 释放锁
释放锁的主要方法是 `release(int)` 
```java
public final boolean release(int arg) {
    //如果释放锁成功了
    if (tryRelease(arg)) {
        Node h = head;
        //h == null Head还没初始化。初始情况下，head == null，第一个节点入队，Head会被初始化一个虚拟节点。所以说，这里如果还没来得及入队，就会出现head == null 的情况。
        //h != null && waitStatus == 0(在唤醒后继节点时会将head的waitStatus的状态置为0) 表明后继节点对应的线程仍在运行中，不需要唤醒。
        //h != null && waitStatus < 0 表明后继节点可能被阻塞了(是否要阻塞Thread的前提就是前驱节点的waiatStatus是否是<0)，需要唤醒。
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
        //将当前持有锁的线程置为null
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
    //node为头节点
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
    //如果下个节点是null或者下个节点被canceled，就找到队列最开始的非cancel节点
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        //从队尾开始找，到队首，找到队列中第一个waitStatus<0的节点
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
>为什么要从后往前找CANCEL节点呢？  
>在之前的 `addWaiter(Node)` 方法中
>```java
>    private Node addWaiter(Node mode) {
>        Node node = new Node(Thread.currentThread(), mode);
>        // Try the fast path of enq; backup to full enq on failure
>        Node pred = tail;
>        if (pred != null) {
>            node.prev = pred;
>            if (compareAndSetTail(pred, node)) {
>                pred.next = node;
>                return node;
>            }
>        }
>        enq(node);
>        return node;
>    }
>```
>1.节点的入队操作并不是原子操作，也就是说node.prev = pred; compareAndSetTail(pred, node)这两个地方可以看作是tail入队的原子操作，
>但此时pred.next = node还没有完成，如果这时候执行了unparkSuccessor方法，就没有办法从前往后找，因为unparkSuccessor方法会破坏
>队列的头节点，从前往后找的话会有问题；  
>2.还有一点原因是，在执行cancelAcquire方法去取消节点时，先断开的时next指针，prev并没有断开，
>因此只有从后往前遍历才能遍历完所有的Node  
>综上所述：如果在极端情况下的入队操作是非原子性的和CANCEL节点的产生过程中断开next指针的操作，可能导致无法遍历所有的节点。所以，
>唤醒对应的线程之后，对应的线程就会向下执行，继续执行acquireQueued
### 中断恢复
唤醒后，会执行 `Thread.interrupted()` ，这个函数返回线程的终端状态，并清除
```java
private final boolean parkAndCheckInterrupt() {
	LockSupport.park(this);
    //唤醒后继续向下执行
	return Thread.interrupted();
}
```
再回到acquireQueued代码，当parkAndCheckInterrupt返回true或者false的时候，interrupted的值不同，但都会执行下次循环。
如果这个时候获取锁成功，就会把当前interrupted返回。
```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```
如果acquireQueued为True，就会执行selfInterrupt方法。
```java
static void selfInterrupt() {
	Thread.currentThread().interrupt();
}
```
该方法其实是为了中断线程。但为什么获取了锁以后还要中断线程呢？这部分属于Java提供的协作式中断知识内容，感兴趣同学可以查阅一下。这里简单介绍一下：
1. 当中断线程被唤醒时，并不知道被唤醒的原因，可能是当前线程在等待中被中断，也可能是释放了锁以后被唤醒。
因此我们通过`Thread.interrupted()`方法检查中断标记（该方法返回了当前线程的中断状态，并将当前线程的中断标识设置为false），
并记录下来，如果发现该线程被中断过，就再中断一次。
2. 线程在等待资源的过程中被唤醒，唤醒后还是会不断地去尝试获取锁，直到抢到锁为止。也就是说，在整个流程中，并不响应中断，只是记录中断记录。
最后抢到锁返回了，那么如果被中断过的话，就需要补充一次中断。
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
## 几个问题
### 1、线程何时会加入到等待队列中
当执行 `acquire(1)` 时，会通过 `tryAcquire` 获取锁。在这种情况下，如果获取锁失败，就会调用 `addWaiter` 加入到等待队列中去。
### 2、等待队列中线程的出队时机
对于从addWaiter方法返回的Node节点，然后传入acquireQueued中，
### 3、等待队列中线程应该被挂起的时机
在等待队列中，当前Node的前驱Node的waitStatus为SIGNAL(-1)时，那么当前的Node中的Thread应该被挂起