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
/**
 * The default initial capacity - MUST be a power of two.
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
/**
 * The next size value at which to resize (capacity * load factor).
 */
int threshold;
/**
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
/**
 * Returns a power of two size for the given target capacity.
 * 此方法就是为了
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