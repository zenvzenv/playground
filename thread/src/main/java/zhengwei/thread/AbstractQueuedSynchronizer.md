# AbstractQueueSynchronizer(抽象队列同步器)
Java中的同步工具都是基于AQS来完成的。
## 实现原理
AQS内部维护了一个共享变量 `private volatile int state; (The synchronization state.)` 代表共享资源，和一个 `FIFO` 线程等待队列，
(多线程发生竞争阻塞时进入此队列)，这里volatile确保内存可见性，当 `state = 1` 时代表此时已有线程加锁，其他线程来加锁则会失败，加锁失
败的线程会被放入一个 `FIFO` 的一个队列，然后被 `UNSAFE.park()` 挂起，等待其他获取锁得线程释放锁或被其他线程唤醒。

另外 `state` 得所有操作都是通过 `CAS` 来确保操作得原子性和线程安全
