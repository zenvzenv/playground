# ConcurrentHashMap
HashMap是线程不安全的Map集合，ConcurrentHashMap是线程安全的Map集合，支持并发读写。ConcurrentHashMap中不允许有null值
## 1.7与1.8比对
跟jdk1.7版本相比，jdk1.8版本又有了很大的变化，已经抛弃了Segment的概念，虽然源码里面还保留了，也只是为了兼容性的考虑。
## 原理概述
在ConcurrentHashMap中是通过一个Node<K, V>[]数组来保存添加到Map中的键值对的，在数组的同一位置上是通过链表或红黑树的来保存的。这个数组
当且经当在第一个元素被添加之后才会被真正初始化，否贼只是初始化了一个ConcurrentHashMap，只设定了一个 **`sizeCtl`** 变量，这个变量非常
重要，后面的扩容和对象的状态判断都和这个变量有关系。

与HashMap一样，ConcurrentHashMap的默认初始容量是16，当往map中添加元素时，通过key的hash值与table的长度绝对该元素落在哪个桶索引。优
先以链表的形式来存储元素，当同一个桶中的元素达到了8以上个时，如果数组的长度小于64时，则会优先扩容table的长度，如果table的长度达到了64，
那么就会将链表转成红黑树。

通过扩容将元素重新打散，这些元素被扩容到新的数组中，同一个链表中的元素通过hash值的数组长度位来区分，是还是放在原来的位置还是放到扩容的
长度的相同位置去 。在扩容完成之后，如果某个节点的是树，同时现在该节点的个数又小于等于6个了，则会将该树转为链表。

取元素的时候，相对来说比较简单，通过计算hash来确定该元素在数组的哪个位置，然后在通过遍历链表或树来判断key和key的hash，取出value值。

## 重要概念
### 重要属性：
```java
//table的最大容量
private static final int MAXIMUM_CAPACITY = 1 << 30;
//table默认容量
private static final int DEFAULT_CAPACITY = 16;
//树化默认阈值
static final int TREEIFY_THRESHOLD = 8;
//从树化退化成链表的阈值
static final int UNTREEIFY_THRESHOLD = 6;
//进行树化时，table的最小容量
static final int MIN_TREEIFY_CAPACITY = 64;
static final int MOVED     = -1; // 表示正在转移
static final int TREEBIN   = -2; // 表示已经转换成树
static final int RESERVED  = -3; // hash for transient reservations
//这个变量时int的最大值，也就是任何树与这个数进行 & 操作之后得到的都是正数
static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash
//默认没初始化的数组，用来保存元素，使用volatile修饰，确保变量的可见性和代码有序性
transient volatile Node<K,V>[] table;
private transient volatile Node<K,V>[] nextTable;//转移的时候用的数组
/**
 * 用来控制表初始化和扩容的，默认值为0，当在初始化的时候指定了大小，这会将这个大小保存在sizeCtl中，大小为数组的0.75
 * 当为负的时候，说明表正在初始化或扩张，
 *     -1表示初始化
 *     -(1+n) n:表示活动的扩张线程
 */
//sizeCtl==0时，代表table没有初始化，且table的初始容量为16
//sizeCtl>0时，如果table数组没有初始化，那么记录的时table的容量；如果数组已经初始化，那么其记录的是扩容阈值(数组的初始容量 * 0.75)
//sizeCtl==-1时，表示table正在初始化
//sizeCtl<0 && sizeCtl!=-1时，表示数组正在扩容，-(1+n)代表有n个线程正在共同完成数组的扩容操作
private transient volatile int sizeCtl;
```
### 重要内部类
Node<K,V>,这是构成每个元素的基本类。key和value不允许为null
```java
static class Node<K,V> implements Map.Entry<K,V> {
    //key的hash值
    final int hash;
    final K key;
    volatile V val;
    //下一个节点
    volatile Node<K,V> next;

    Node(int hash, K key, V val, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.val = val;
        this.next = next;
    }

    public final K getKey()       { return key; }
    public final V getValue()     { return val; }
    public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
    public final String toString(){ return key + "=" + val; }
    public final V setValue(V value) {
        throw new UnsupportedOperationException();
    }
}
```
TreeNode，构造树的节点
```java
static final class TreeNode<K,V> extends Node<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, K key, V val, Node<K,V> next,
                 TreeNode<K,V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }
}
```
TreeBin 用作树的头结点，只存储root和first节点，不存储节点的key、value值。
```java
static final class TreeBin<K,V> extends Node<K,V> {
        TreeNode<K,V> root;
        volatile TreeNode<K,V> first;
        volatile Thread waiter;
        volatile int lockState;
        // values for lockState
        static final int WRITER = 1; // set while holding write lock
        static final int WAITER = 2; // set when waiting for write lock
        static final int READER = 4; // increment value for setting read lock
}
```
ForwardingNode在转移的时候放在头部的节点，是一个空节点
```java
static final class ForwardingNode<K,V> extends Node<K,V> {
        final Node<K,V>[] nextTable;
        ForwardingNode(Node<K,V>[] tab) {
            super(MOVED, null, null, null);
            this.nextTable = tab;
        }
}
```
## 重要方法
在ConcurrentHashMap中使用了unSafe方法，通过直接操作内存的方式来保证并发处理的安全性，使用的是硬件的安全机制。
```java
/*
 * 用来返回节点数组的指定位置的节点的原子操作
 */
@SuppressWarnings("unchecked")
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}

/*
 * cas原子操作，在指定位置设定值
 */
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                    Node<K,V> c, Node<K,V> v) {
    return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
}
/*
 * 原子操作，在指定位置设定值
 */
static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
    U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
}
```
## 初始化
```java
//无参构造器，所有参数均使用默认值
public ConcurrentHashMap() {
}

//指定初始容量的构造器
public ConcurrentHashMap(int initialCapacity) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException();
    int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
               MAXIMUM_CAPACITY :
                //规整初始容量，初始容量为大于指定的初始容量的二次幂数
                //如，指定了32，那么初始容量则为64
                //1.5 * initialCapacity + 1
               tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
    //sizeCtl此时记录了初始容量
    this.sizeCtl = cap;
}

//接受另外一个map
public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
    //当接受另外一个map时，使用默认的容量
    this.sizeCtl = DEFAULT_CAPACITY;
    putAll(m);
}
public ConcurrentHashMap(int initialCapacity, float loadFactor) {
    this(initialCapacity, loadFactor, 1);
}

public ConcurrentHashMap(int initialCapacity,
                         float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
        throw new IllegalArgumentException();
    if (initialCapacity < concurrencyLevel)   // Use at least as many bins
        initialCapacity = concurrencyLevel;   // as estimated threads
    long size = (long)(1.0 + (long)initialCapacity / loadFactor);
    int cap = (size >= (long)MAXIMUM_CAPACITY) ?
        MAXIMUM_CAPACITY : tableSizeFor((int)size);
    this.sizeCtl = cap;
}
```
可以看到，在任何一个构造方法中，都没有对存储Map元素Node的table变量进行初始化。而是在第一次put操作的时候在进行初始化。
下面来看看数组的初始化方法initTable：
```java
//sizeCtl==0时，代表table没有初始化，且table的初始容量为16
//sizeCtl>0时，如果table数组没有初始化，那么记录的时table的容量；如果数组已经初始化，那么其记录的是扩容阈值(数组的初始容量 * 0.75)
//sizeCtl==-1时，表示table正在初始化
//sizeCtl<0 && sizeCtl!=-1时，表示数组正在扩容，-(1+n)代表有n个线程正在共同完成数组的扩容操作
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    //如果第一个对table进行操作，那么table是null，那么就进入while循环进行table的初始化
    while ((tab = table) == null || tab.length == 0) {
        //如果线程发现sizeCtl<0说明table正在初始化，那么就放弃CPU执行权不再往下执行
        if ((sc = sizeCtl) < 0)
            Thread.yield(); // lost initialization race; just spin
        //如果线程修改sizeCtl的值为-1，表示当前table正在初始化
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
                //二次判断
                if ((tab = table) == null || tab.length == 0) {
                    //计算table的初始容量，如果在构造ConcurrentHashMap时指定了容量，那么就以指定的容量作为初始容量
                    //否则使用默认容量16
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    //此处计算扩容阈值，n代表的是table的容量
                    //n - n/4 = 3/4n，即0.75n
                    sc = n - (n >>> 2);
                }
            } finally {
                //初始化完后，sizeCtl记录的是扩容阈值
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```
## put操作
```java
//空壳方法，直接调用putVal
public V put(K key, V value) {
    return putVal(key, value, false);
}

final V putVal(K key, V value, boolean onlyIfAbsent) {
    //ConcurrentHashMap不允许空的key和value
    if (key == null || value == null) throw new NullPointerException();
    //计算key的hash值，经过此方法得到的hash值一定为正数
    int hash = spread(key.hashCode());
    //用来计算这个桶总共有多少个元素，用来控制扩容和转换成树
    int binCount = 0;
    //进入自旋
    for (Node<K,V>[] tab = table;;) {
        //fh代表节点f的hash值，hash大于的节点一定是链表节点
        Node<K,V> f; int n, i, fh;
        //如果table还没有被初始化
        if (tab == null || (n = tab.length) == 0)
            //对table进行初始化
            tab = initTable();
        //(n - 1) & hash计算出该key所在的桶的索引
        //tabAt方法定位到table的索引位置
        //如果该桶中还没有数据，那么就把当前K,V放入桶中即可
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            //使用CAS将当前K,V设置都当前桶中
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        //检测到某个节点的hash是MOVED(-1)，表示table正在进行扩容操作
        else if ((fh = f.hash) == MOVED)
            //当前线程前去协助扩容操作
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            //如果当前桶有元素的话，那就采用synchronized的方式锁住头节点，来确保线程安全
            synchronized (f) {
                //取出桶中的元素进行二次比较
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) {
                        //链表元素个数加一
                        binCount = 1;
                        //遍历到链表尾部
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            //hash相同，并且key也相同，那么就用新的value覆盖旧的value
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                //当使用putIfAbsent的时候，只有在这个key没有设置值得时候才设置
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            //如果当前节点的下一个节点为空，那么就把d
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
}
```
对当前的table进行无条件**自旋**直到put成功，put过程总结如下：
1. 如果table还未初始化，那么就调用initTable进行初始化
2. 如果没有hash冲突就CAS插入元素
3. 如果还在进行扩容，那么就协助扩容
4. 如果存在hash冲突，那么就使用synchronized来确保线程安全，这里有两种情况，一种是链表形式就直接遍历到链表尾部插入，如果是树形结构的话
就按红黑树规则插入
5. 如果某一个链表中的元素个数超过了8，并且table的长度超过了64，就将链表转换成树节点
6. 最后调用addCount去计算map中的元素个数，并检测是否需要扩容