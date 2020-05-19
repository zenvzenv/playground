# Map
## HashMap(JDK1.8)
## 概述
HashMap是基于散列算法实现，key和value均允许null值。在hash时，null的key的hash为0。Hash不保证内部元素的有序性，这意味着在操作键值对
时，元素的顺序会发生变化。HashMap不是线程安全的，在多线程环境下会出现问题(jdk1.7中可能会出现死循环，jdk1.8中会出现元素覆盖的情况)
## 原理
HashMap是基于散列算法的，散列分为散列再探测和拉链式。HashMap采用的拉链式，在发生hash冲突的时候，在冲突的为止上转为链表，在jdk1.8中
引入了红黑树，在链表元素超过8个时将链表转为红黑树。  
在对元素增删改的时候，首先需要定位到元素所在桶的位置，之后再在链表中定位该元素。在jdk1.7的时候HashMap的底层数据结构是 `数组 + 链表`，
在jdk1.8时HashMap底层采用的是 `数组 + 链表 + 红黑树` 的数据结构
## 构造方法
### 构造方法分析
HashMap有4个构造方法，一般是初始化一些重要的变量，比如 `initialCapacity` 和 `loadFactor` ，而底层的数据结构是延迟初始化的，
只有在往HashMap中真正的去插入数据的时候才会去真正的初始化底层的桶数据结构。其构造方法如下：
```java
/* 构造方法1 */
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
    this.loadFactor = loadFactor;
    //初始化阈值，将桶的数量初始化为大于指定值的2次幂
    this.threshold = tableSizeFor(initialCapacity);
}
/* 构造方法2 */
public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}
/* 构造方法3 */
//默认的构造方法，仅使用默认的状态因子，默认0.75
public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}
/* 构造方法4 */
//从另外一个Map中复制元素
public HashMap(Map<? extends K, ? extends V> m) {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    putMapEntries(m, false);
}
```
### 初始容量、负载因子和阈值
| 名称 | 用途 |
| -------- | -------- |
| initialCapacity   | HashMap 初始容量   |
| loadFactor | 负载因子 |
| threshold | 阈值，HashMap所能容纳的最大值，超过这个值则扩容 |
相关代码如下：
```java
/*
 * The default initial capacity - MUST be a power of two.
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
/*
 * The next size value at which to resize (capacity * load factor).
 */
int threshold;
/*
 * The load factor for the hash table.
 */
final float loadFactor;
static final float DEFAULT_LOAD_FACTOR = 0.75f;
```
其实在HashMap的源码中并没有定义initialCapacity这个变量，这个变量表示一个初始容量，只是构造方法中用一次，没必要定义一个变量保存。
在HashMap的构造方法中并没有对底层的数据结构进行初始化，这里HashMap进行了延迟初始化处理，在第一个数据真正插入进来的时候才去初始化数据结构，
后面会专门进行讲解。  
默认情况下，HashMap的初始容量是16，装载因子是0.75f，HashMap没有定义默认的阈值，是因为threshold是被计算出来的，是动态变化的，计算公式
是 `threshold = capacity * loadFactory` ，观察 `构造方法1` 会发现阈值并不是通过这个公式计算出来的,这是不是可以说明 threshold 
变量的注释有误呢？还是仅这里进行了特殊处理，其他地方遵循计算公式呢？而是通过 `tableSizeFor` 方法
计算的，代码如下：
```java
/*
 * Returns a power of two size for the given target capacity.
 * 此方法就是为了获得比初始容量大的2次幂数
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```
对于负载因子，它反应了HashMap中桶数组的使用情况(假设键值对均匀的分布在HashMap中)。通过调节负载因子，可使HashMap在时间和空间复杂度上
有不同的表现。当我们调低负载因子时，HashMap所能容纳的元素变少了，扩容时，将键值存储到新的桶数组中，建的冲突降低，链表的长度下降，此时，
HashMap的增删改查的效率提高，这里就是典型的空间换时间；相反，如果我们调高负载因子(loadFactory可大于1)，HashMap中能够容纳更多的元素，
但是hash碰撞增加，链表会变长，增删改查的效率也会随之降低，这就是时间换空间的情况。至于负载因子怎么调节，因根据实际情况进行调整。一般情
况下，使用默认的负载因子即可。
## 查找
HashMap的查找原理比较简单，大致原理是：先定位到所在的桶，然后再对链表或红黑树进行查找，通过这两部即可找到元素，代码如下：
```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    //定位元素所在的桶的位置
    if ((tab = table) != null && (n = tab.length) > 0 &&
        //在比较的同时对first进行赋值
        (first = tab[(n - 1) & hash]) != null) {
        //总是会去检查第一个元素是不是要查找的元素
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            //检查是否是红黑树节点
            if (first instanceof TreeNode)
                //查找红黑树
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            //检索链表查找元素
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```
### 确定所在的桶
在 `getNode(int, Object)` 方法中，对于定位所在的桶使用的是 `first = tab[(n - 1) & hash` 进行计算的。为什么使用这个公式？因为HashMap
的桶的大小总是2次幂，此时的 `tab[(n - 1) & hash` 其实是对length**取余数**，这里使用位运算，比直接使用 `%` 的效率要高，这里算个小优化。
举个例子：假设hash=200，length=16，运算结果如下：
```text
hash    : 1100 1000
n - 1   : 0000 1111  &
------------------------
          0000 1000  -> 8
即200 % 16 = 8 
```
### hash
hash也是一个比较关键的小方法。其代码如下：
```java
//计算一个元素键的hash值
static final int hash(Object key) {
    int h;
    //HashMap允许null值，null的键的hash为0
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```
注意到hash是将key的hashCode的高16位和低16位进行异或操作，这样做有几个好处：  
1. 在确定元素所在的桶的位置时，用hash取模上桶的长度，由于桶的长度一般较短，在进行&操作的时候，只能取到hash的低位，而没有hash的高位，
这导致了计算结果至于低位有关，与高位无关，这无形中加大了hash碰撞的可能。
2. 用高16位和低16位进行异或之后，加大了低位的随机性，降低了hash碰撞，变相的让高16位也加入到定位桶的计算中。  
此时定位桶的过程如下：
```text
hash        :   1100 1000
hash >>> 4  :   0000 1100    ^
----------------------------------
                1100 0100
n - 1       :   0000 1111    &
----------------------------------
                0000 0100 -> 4
```
因为在Java中，int类型是32位，前16位是高位，低16位为低位，所以在计算时需要右移16位进行计算。
>为什么HashMap要重写hash函数，而不直接使用Object的hashCode()?  
>因为我们有时候会重写hashCode()函数，由于有时候我们的hashCode()方法写的并不是很好，会出现大量的hash碰撞。增加hash方法的复杂性，
>进而影响hash分布，这也是为什么HashMap不直接用hashCode的原因。
## 遍历
对于遍历HashMap，我们通常使用如下方法：
```java
for (Object key : map.keySet()) {
    //do something
}
```
或
```java
for (HashMap.Entry entry : map.entrySet()) {
    //do something
}
```
一般都是对 HashMap 的 key 集合或 Entry 集合进行遍历。上面代码片段中用 foreach 遍历 keySet 方法产生的集合，
在编译时会转换成用迭代器遍历，等价于：
```java
Set keys = map.keySet();
Iterator ite = keys.iterator();
while (ite.hasNext()) {
    Object key = ite.next();
    // do something
}
```
在遍历过程中，多次遍历的结果是一致的，但是key遍历出来的顺序和我们插入的顺序是不一致的，具体的迭代器代码如下：
```java
public Set<K> keySet() {
    Set<K> ks = keySet;
    if (ks == null) {
        ks = new KeySet();
        keySet = ks;
    }
    return ks;
}
//键集合
final class KeySet extends AbstractSet<K> {
    public final int size()                 { return size; }
    public final void clear()               { HashMap.this.clear(); }
    public final Iterator<K> iterator()     { return new KeyIterator(); }
    public final boolean contains(Object o) { return containsKey(o); }
    public final boolean remove(Object key) {
        return removeNode(hash(key), key, null, false, true) != null;
    }
    public final Spliterator<K> spliterator() {
        return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
    }
    public final void forEach(Consumer<? super K> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }
}
//key的迭代器
final class KeyIterator extends HashIterator
    implements Iterator<K> {
    public final K next() { return nextNode().key; }
}
//迭代器
abstract class HashIterator {
    Node<K,V> next;        // next entry to return
    Node<K,V> current;     // current entry
    int expectedModCount;  // for fast-fail
    int index;             // current slot

    HashIterator() {
        expectedModCount = modCount;
        Node<K,V>[] t = table;
        current = next = null;
        index = 0;
        if (t != null && size > 0) { // advance to first entry
            //寻找第一个包含元素的桶
            do {} while (index < t.length && (next = t[index++]) == null);
        }
    }

    public final boolean hasNext() {
        return next != null;
    }

    final Node<K,V> nextNode() {
        Node<K,V>[] t;
        Node<K,V> e = next;
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        if (e == null)
            throw new NoSuchElementException();
        if ((next = (current = e).next) == null && (t = table) != null) {
            //寻找下一个包含元素的桶
            do {} while (index < t.length && (next = t[index++]) == null);
        }
        return e;
    }

    public final void remove() {
        Node<K,V> p = current;
        if (p == null)
            throw new IllegalStateException();
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        current = null;
        K key = p.key;
        removeNode(hash(key), key, null, false, false);
        expectedModCount = modCount;
    }
}
```
当我们在遍历所有的key时，首先要获取KeySet，然后再通过KeySet的迭代器KeyIterator进行遍历。KeyIterator继承自HashIterator类，核心遍历
代码全部封装再HashIterator中。HashIterator在遍历时，首先找到第一个有元素的桶，然后顺着桶中的链表进行遍历，遍历完毕之后，再找到下一个
有元素的桶，重复之前的过程，直到遍历完所有的桶。
## 插入
### 插入逻辑
1. 首先确定桶数组是否还没有初始化，如果没有初始化那么初始化桶数组。否则通过 `(n - 1) & hash` 计算key的桶位置
2. 计算出来的桶位置上是否有数据，如果桶为空那么直接插入，如果不为空则需要将键值接到链表的最后(在jdk1.7中是插入到链表的头部)，或者更新键值对
这只是简单逻辑，HashMap的具体实现很复杂，设计到扩容，链表和红黑树的互相转化以及树的相关操作。
### 源码
```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    //判断桶数组是否初始化过了，这里就体现了数据结构延迟加载的地方，到真正插入数据的时候才去初始化数据结构
    if ((tab = table) == null || (n = tab.length) == 0)
        //初始化table，resize既是扩容函数也是初始化table函数
        n = (tab = resize()).length;
    //如果定位到的桶不包含元素，那么就直接将元素引用存入到桶中
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    //如果要插入的key的hash发生了冲突
    else {
        Node<K,V> e; K k;
        //发生冲突的桶位置上的第一个元素的hash相同
        //第一个元素的key和要插入的key相同或者两个key相等时
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            //则将e指向该桶的第一个元素
            e = p;
        //如果桶中的类型时TreeNode，则调用红黑树的插入方法
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            //对链表进行遍历
            for (int binCount = 0; ; ++binCount) {
                //链表中不包含要插入的键值对节点时，则将键值对插入到链表的最后
                //此时代表链表中没有要插入的元素，e=null，其余情况下e均不为null
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    //如果链表的长度不小于8时，将链表树化
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                //当为true时，说明链表中包含了该键值对，终止遍历
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                //之前p是第一个元素，e为p.next节点
                //将e赋值给p实现链表的传递
                p = e;
            }
        }
        //判断要插入的键值对是否在HashMap中
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            //onlyIfAbsent表示是否仅在oldValue为null的情况下更新value
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    //修改因子加一，以快速失败
    ++modCount;
    //判断HashMap中的元素的个数时候超过阈值
    //超过则扩容
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```
插入操作入口在 `put(K, V)` 但实际核心逻辑封装在 `putVal(int, K, V, boolean, boolean)` ，putVal主要做了以下几件事：
1. 当table桶数组为空时，初始化桶数组
2. 查找到要插入的table位置
3. 寻找是否要插入的键值对是否已经存在，如果不存在则插入到链表的尾部，如果存在根据条件判断是否覆盖旧值
4. 查看链表的长度，超过阈值的话，将链表树化
5. 检查HashMap中的元素个数，超过阈值，则进行扩容
## 扩容/初始化桶数组
在Java中数组的长度是不可变的，这意味着数组只能存放固定数量的数据。像ArrayList和HashMap就是变长的数据结构。
在HashMap中，桶的长度始终为2次幂。阈值的大小由HashMap中桶数组的长度和负载因子的乘积决定，当HashMap中的键值对超过阈值时进行扩容。
HashMap对于其他集合的扩容有点不一样，HashMap每次都扩容到原来的两倍，阈值也为原来的两倍(如果阈值溢出了，则按阈值计算公式重新计算)。扩容
之后，需要重新极端键值对的位置，并将他们移动到合适的位置上。具体代码如下：
```java
//初始化桶数组或将桶数组的大小变为原来的两倍
final Node<K,V>[] resize() {
    //最开始table是没有被初始化的，延迟加载
    Node<K,V>[] oldTab = table;
    //没有初始化的话，那么初始化旧的桶数组的长度为0
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    //如果桶数组已被初始化
    if (oldCap > 0) {
        //如果容量已达到Integer.MAX_VALUE - 8，那么不扩容
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        //否则将就容量扩容到原来的两倍，并重新计算阈值的大小
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            //新的阈值调整为原来的两倍
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        //此处是我们在实例化HashMap时指定了初始容量
        //初始化时，如果我们在构造方法中指定了初始容量，那么HashMap会计算出一个大于初始容量的二次幂的数，并将这个数赋值给threshold
        //其实这个threshold就是HashMap初始化的容量，只是暂存在了threshold中，后续会重新计算threshold(capacity * loadFactory)
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        //如果我们调用的时午餐构造器时
        //初始化桶数组的大小为默认大小，默认值是16
        newCap = DEFAULT_INITIAL_CAPACITY;
        //使用默认的负载因子计算threshold
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    //当threshold溢出为0时，重新计算threshold，兜底操作
    //最大值为Integer.MAX_VALUE
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    //重新校准threshold的值
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    //创建新的桶数组
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    //如果原来的数组不为空的话，那么将原来的数组中的元素复制到新的数组中
    if (oldTab != null) {
        //遍历旧桶
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            //如果桶上的元素不为空，说明此桶中有元素
            if ((e = oldTab[j]) != null) {
                //gc helper，将该桶清空
                oldTab[j] = null;
                //如果该桶中只有一个元素
                if (e.next == null)
                    //将旧桶中这一个元素进行重新定位桶
                    newTab[e.hash & (newCap - 1)] = e;
                //如果是红黑树节点
                else if (e instanceof TreeNode)
                    //调用红黑树的映射方法
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    //遍历链表，并将原链表按原来的顺序进行分组
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        } else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```
针对于resize方法，大致可将其逻辑分为以下几个步骤：
1. 计算新桶的容量newCap和新的阈值newThr
2. 根据计算出来的新桶的容量newCap创建新的桶数组，桶数组table也是在这里被初始化的
3. 将旧桶中的键值对映射到新桶中。如果节点是 TreeNode 类型，则需要拆分红黑树。如果是普通节点，则节点按原顺序进行分组。  
针对于以上三点中的，resize代码中有如下体现(略去了初始化table部分)：
```java
// 第一个条件分支
if (oldCap > 0) {
    // 嵌套条件分支
    if (oldCap >= MAXIMUM_CAPACITY) {...}
    else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY) {...}
} 
else if (oldThr > 0) {...}
else {...}

// 第二个条件分支
if (newThr == 0) {...}
```
### 计算容量和阈值分支一
通过这两个分支来对newCap和newThr进行计算，覆盖的范围如下所示：

|条件                  | 覆盖情况                          | 备注                                                                                                    |  
|:---------------------:|:-------------------------------:|:------------------------------------------------------------------------------------------------------:|  
|oldCap > 0           |桶数组已经被初始化过              |                                                                                                            |  
|oldThr > 0           |threshold>0，且桶数组没有被初始化  | 在调用HashMa(int)或HashMap(int, float)构造方法时产生这种情况，此种情况newCap=oldThr，newThr在第二个分支中计算得出 |  
|oldThr==0&&oldCap==0|桶没有被初始化，并且threshold也为0 | 调用HashMap默认的构造方法时会出现这种情况                                                                      |  

对于 `oldThr>0` 这种情况，oldThr会被赋值给oldCap，也就是 `oldCap = oldThr = tableSizeFor(initialCapacity)` 。我们实例化HashMap
时传入的initialCapacity经过threshold最终被赋值给了newCap，也就是HashMap为什么没有initialCapacity这个变量的原因，threshold一开始
作为一个临时的容量字段，在初始化后续会更正threshold的值。  
分之一中的嵌套分支：  

| 条件                       | 覆盖情况                                  | 备注                                                    |
| :----------------------------: | :---------------------------------------------: | :---------------------------------------------------------: |
| oldCap >= 2^30               | 桶数组容量大于或等于最大桶容量 2^30 | 这种情况下不再扩容                               |
| newCap < 2^30 && oldCap > 16 | 新桶数组容量小于最大值，且旧桶数组容量大于 16 | 该种情况下新阈值 newThr = oldThr << 1，移位可能会导致溢出 |

对于阈值移位导致溢出的操作：
```text
初始状态
loadFactor = 8.0
initialCapacity = 2^28                          0001 0000 0000 0000 0000 0000 0000 0000
threshold = 2 ^ 28                              0001 0000 0000 0000 0000 0000 0000 0000
-------------------------------------------------------------------------------------------
第一次扩容
oldCap = 0
oldThr = threshold = 2^28                       0001 0000 0000 0000 0000 0000 0000 0000
newCap = oldThr = 2^28                          0001 0000 0000 0000 0000 0000 0000 0000
newThr = newCap * loadFactory = 2^31            1000 0000 0000 0000 0000 0000 0000 0000
-------------------------------------------------------------------------------------------
第二次扩容
oldCap = 2^28                                   0001 0000 0000 0000 0000 0000 0000 0000
newCap = oldCap << 1 = 2^29                     0010 0000 0000 0000 0000 0000 0000 0000
oldThr = 2^31                                   1000 0000 0000 0000 0000 0000 0000 0000
newThr = oldThr << 1 = 0                        0000 0000 0000 0000 0000 0000 0000 0000
最终溢出为0
```
### 计算容量和阈值分支二

| 条件 | 覆盖情况 |
| -------- | -------- |
| newThr == 0   | 对于第一个分支未计算或溢出的newThr情况   |
对没有计算或溢出的阈值进行重新计算，最大的阈值时 `Integer.MAX_VALUE`
### 键值对重新映射
jdk1.8中HashMap中节点有两种类型，链表的节点或者是红黑树节点。对于红黑树节点而言，需要先对红黑树进行拆分再进行映射；对于链表节点，需要
先对节点进行分组，再映射，需要注意的是，分组后，组内节点相对位置没有发生改变。
#### 链表映射
对于链表的插入过程，底层一般是通过模运算获取所在桶的位置，接着将节点放入到桶中或放到链表的尾部即可。事实上，我们可以重新映射看作是插入
过程，在jdk1.7中也确实插入的过程，但在jdk1.8中，对这个过程进行了优化，过程较为复杂。对于hash过程：
```text
n - 1   0000 1111
hash1   1011 1001  &  -->  0000 1001 = 9
hash2   1010 1001  &  -->  0000 1001 = 9
桶数组大小为16，虽然hash1和hash2不相等，但是其低四位是相等，所以计算出来的桶的位置也就是相同的。
```
对HashMap进行扩容后，桶数组的长度由16变成了32，参与模运算的位数由4变成了5，所以计算出的桶的位置也就不一样了。
```text
扩容后
n - 1   0001 1111
hash1   1011 1001  &  -->  0001 1001 = 16 + 9 = oldCap + 原位置
hash2   1010 1001  &  -->  0000 1001 = 9 = 原位置
对于扩容后，寻找新桶，无非就是要不要再原来桶的基础上加oldCap
```
## 问题
### HashMap中的容量有限制吗？这个容量实际是干嘛用的？
HashMap中的容量其实对HashMap中能够存多少容量没有起到作用，只是用来初始化了桶数组长度和阈值计算。理论上HashMap能够存无限个元素，
元素存在的形式是链表或红黑树，链表和红黑树后面可以无限接数据，但是这样会降低增删改查的效率，为了不出现这种不利局面，
容量被用来了计算一个阈值，凭借着足够复杂的hash，不会出现单一链表的情况，分布的还算平均，当HashMap中的元素个数超过阈值时就进行扩容。