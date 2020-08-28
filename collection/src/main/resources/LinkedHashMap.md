# LinkedHashMap(jdk1.8)
jdk1.7中的LinkedHashMap相比较jdk1.8中的LinkedHashMap在数据结构上稍有不同
## 概述
相比HashMap的无序存放key和value，LinkedHashMap中的key，value是有序的。LinkedHashMap通过维护一个**双向链表**来实现内部元素的有序性，
该迭代顺序可以是**插入顺序(保证插入顺序)**和**读访问顺序(保证访问顺序)**，LinkedHashMap默认实现是按插入的顺序进行排序。  
本质上，HashMap和双向链表合二为一就组成了LinkedHashMap。所谓LinkedHashMap，其落脚点在HashMap，因此更准确地说，
它是一个将所有Entry节点链入一个双向链表双向链表的HashMap。在LinkedHashMapMap中，所有put进来的Entry都保存在和HashMap一样的哈希表中，
但由于它又额外定义了一个以head(head节点不存数据)为头结点的双向链表，因此对于每次put进来Entry，除了将其保存到哈希表中对应的位置上之外，
还会将其插入到双向链表的尾部。  
## 继承结构
```text
AbstractMap (java.util)
    HashMap (java.util)
        LinkedHashMap (java.util)
```
## 原理
LinkedHashMap继承自HashMap，先看下HashMap的数据结构。如下图所示：  
!(HashMap数据结构.jpg)[src\main\resources\image\HashMap数据结构.jpg]  
LinkedHashMap在HashMap的基础上新加了一条双向链表，使得上面的结构可以保持键值对的插入顺序、同时可以通过链表进行相应的操作，实现了相关
的访问顺序的逻辑。LinkedHashMap的数据结构如下所示：
!(LinkedHashMap数据结构.jpg)[src\main\resources\image\LinkedHashMap数据结构.jpg]  
淡蓝色箭头表示前驱引用，红色箭头表示后继引用，每当有新的节点插入时，新节点最终会接在`tail`引用指向的节点的后面，而`tail`指针则会指向
新的节点，这样一个双向链表就建立起来了。  
## 源码分析
### Entry的继承体系
Entry继承体系如下：  
!(Entry继承体系.jpg)[src\main\resources\image\Entry继承体系.jpg]  
上面的继承体系乍一看还是有点复杂的，同时也有点让人迷惑。HashMap 的内部类 TreeNode 不继承它的了一个内部类 Node，却继承自Node的子类
LinkedHashMap内部类Entry。这里这样做是有一定原因的，这里先不说。先来简单说明一下上面的继承体系。LinkedHashMap 内部类 Entry 继承自
HashMap 内部类 Node，并新增了两个引用，分别是 before 和 after。这两个引用的用途不难理解，也就是用于维护双向链表。同时，TreeNode 继
承 LinkedHashMap 的内部类 Entry 后，就具备了和其他 Entry 一起组成链表的能力。但是这里需要大家考虑一个问题。当我们使用 HashMap 时，
TreeNode 并不需要具备组成链表能力。如果继承 LinkedHashMap 内部类 Entry ，TreeNode 就多了两个用不到的引用，这样做不是会浪费空间吗？
简单说明一下这个问题（水平有限，不保证完全正确），这里这么做确实会浪费空间，但与 TreeNode 通过继承获取的组成链表的能力相比，
这点浪费是值得的。在 HashMap 的设计思路注释中，有这样一段话：
>Because TreeNodes are about twice the size of regular nodes, we
 use them only when bins contain enough nodes to warrant use
 (see TREEIFY_THRESHOLD). And when they become too small (due to
 removal or resizing) they are converted back to plain bins.  In
 usages with well-distributed user hashCodes, tree bins are rarely used.

大致的意思是 TreeNode 对象的大小约是普通 Node 对象的2倍，我们仅在桶（bin）中包含足够多的节点时再使用。当桶中的节点数量变少时
(取决于删除和扩容) TreeNode 会被转成 Node。当用户实现的 hashCode 方法具有良好分布性时，树类型的桶将会很少被使用。  
通过上面的注释，我们可以了解到。一般情况下，只要 hashCode 的实现不糟糕，Node 组成的链表很少会被转成由 TreeNode 组成的红黑树。
也就是说 TreeNode 使用的并不多，浪费那点空间是可接受的。假如 TreeNode 机制继承自 Node 类，那么它要想具备组成链表的能力，
就需要 Node 去继承 LinkedHashMap 的内部类 Entry。这个时候就得不偿失了，浪费很多空间去获取不一定用得到的能力。  
### 链表建立过程
链表的建立过程是在插入键值对节点时开始的，初始情况下，让 LinkedHashMap 的 head 和 tail 引用同时指向新节点，链表就算建立起来了。
随后不断有新节点插入，通过将新节点接在 tail 引用指向节点的后面，即可实现链表的更新。  
Map 类型的集合类是通过 put(K,V) 方法插入键值对，LinkedHashMap 本身并没有覆写父类的 put 方法，而是直接使用了父类的实现。
但在 HashMap 中，put 方法插入的是 HashMap 内部类 Node 类型的节点，该类型的节点并不具备与 LinkedHashMap 内部类 Entry 
及其子类型节点组成链表的能力。那么，LinkedHashMap 是怎样建立链表的呢？在展开说明之前，我们先看一下 LinkedHashMap 插入操作相关的代码：
```java
//HashMap中实现
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
//HashMap中实现
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
//HashMap中实现
Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
    return new Node<>(hash, key, value, next);
}
//LinkedHashMap中覆写
Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
    LinkedHashMap.Entry<K,V> p =
        new LinkedHashMap.Entry<K,V>(hash, key, value, e);
    linkNodeLast(p);
    return p;
}
//LinkedHashMap实现
// link at the end of list
private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
    LinkedHashMap.Entry<K,V> last = tail;
    tail = p;
    if (last == null)
        head = p;
    else {
        p.before = last;
        last.after = p;
    }
}
```
从上面的代码中可以看出，LinkedHashMap插入的流程如下：
```text
       开始
        ↓
java.util.HashMap.put
        ↓
java.util.HashMap.putVal
        ↓
**java.util.LinkedHashMap.newNode**
        ↓
java.util.LinkedHashMap.linkNodeLast
        ↓
       结束
```
newNode方法比较关键，LinkedHashMap 覆写了该方法。在这个方法中，LinkedHashMap 创建了 Entry，并通过 linkNodeLast 方法将 Entry 
接在双向链表的尾部，实现了双向链表的建立。双向链表建立之后，我们就可以按照插入顺序去遍历 LinkedHashMap。  
LinkedHashMap中的三个回调方法：
```java
// Callbacks to allow LinkedHashMap post-actions
void afterNodeAccess(Node<K,V> p) { }
void afterNodeInsertion(boolean evict) { }
void afterNodeRemoval(Node<K,V> p) { }
```
这些方法的用途是在增删查等操作后，通过回调的方式，让 LinkedHashMap 有机会做一些后置操作。
### 链表节点的删除过程
与插入操作一样，LinkedHashMap 删除操作相关的代码也是直接用父类的实现。在删除节点时，父类的删除逻辑并不会修复 LinkedHashMap 
所维护的双向链表，这不是它的职责。那么删除及节点后，被删除的节点该如何从双链表中移除呢？在HashMap中删除节点时，HashMap会调用
LinkedHashMap中的回调方法 `afterNodeRemoval` ，LinkedHashMap 覆写该方法，并在该方法中完成了移除被删除节点的操作。相关源码如下：
```java
//HashMap中实现
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}
//HashMap中实现
final Node<K,V> removeNode(int hash, Object key, Object value,
                           boolean matchValue, boolean movable) {
    Node<K,V>[] tab; Node<K,V> p; int n, index;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (p = tab[index = (n - 1) & hash]) != null) {
        Node<K,V> node = null, e; K k; V v;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            node = p;
        else if ((e = p.next) != null) {
            if (p instanceof TreeNode)
                node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
            else {
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key ||
                         (key != null && key.equals(k)))) {
                        node = e;
                        break;
                    }
                    p = e;
                } while ((e = e.next) != null);
            }
        }
        if (node != null && (!matchValue || (v = node.value) == value ||
                             (value != null && value.equals(v)))) {
            if (node instanceof TreeNode)
                ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
            else if (node == p)
                tab[index] = node.next;
            else
                p.next = node.next;
            ++modCount;
            --size;
            afterNodeRemoval(node);//回调LinkedHashMap中的方法
            return node;
        }
    }
    return null;
}
//LinkedHashMap中实现的回调方法
void afterNodeRemoval(Node<K,V> e) { // unlink
    LinkedHashMap.Entry<K,V> p =
        (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
    //将 p(当前要删除的节点) 节点的前后节点全部置为null
    p.before = p.after = null;
    //如果前驱节点为null，说明 p 是头节点
    if (b == null)
        head = a;
    //不然删除 p 节点
    else
        b.after = a;
    //a为null表示p是尾节点
    if (a == null)
        tail = b;
    else
        a.before = b;
}
```
删除的过程并不复杂，上面这段代码只做了三件事：
1. 根据key的hash找到桶
2. 遍历链表或调用红黑树删除节点的操作删除要删除的节点
3. 维护LinkedHashMap中的链表，将需要删除的节点删除
### 访问顺序维护
默认情况下，LinkedHashMap 是按**插入顺序**维护链表。不过我们可以在初始化 LinkedHashMap，指定 accessOrder 参数为 true，
即可让它按**访问顺序**维护链表。访问顺序的原理上并不复杂，当我们调用 `get/getOrDefault/replace` 等方法时，即对这个节点进行了访问时，
就会对该节点进行访问顺序维护，只需要将这些方法访问的节点移动到链表的尾部即可。相关代码如下：
```java
//LinkedHashMap中覆写
public V get(Object key) {
    Node<K,V> e;
    if ((e = getNode(hash(key), key)) == null)
        return null;
    //如果 accessOrder 为 true 的话，在访问过该节点的时候就会把该节点放到链表的最后
    if (accessOrder)
        //将访问的节点放到链表的最后
        afterNodeAccess(e);
    return e.value;
}
//LinkedHashMap中实现
void afterNodeAccess(Node<K,V> e) { // move node to last
    LinkedHashMap.Entry<K,V> last;
    if (accessOrder && (last = tail) != e) {
        //当前节点，即被访问的节点
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;
        //如果b为null，代表p为头节点
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a != null)
            a.before = b;
        else
            last = b;
        if (last == null)
            head = p;
        else {
            //将p连到链表最后
            p.before = last;
            last.after = p;
        }
        tail = p;
        ++modCount;
    }
}
```
## LRU缓存设计

## 总结
本文从 LinkedHashMap 维护双向链表的角度对 LinkedHashMap 的源码进行了分析，并在文章的结尾基于 LinkedHashMap 实现了一个简单的 Cache。
在日常开发中，LinkedHashMap 的使用频率虽不及 HashMap，但它也个重要的实现。在 Java 集合框架中，HashMap、LinkedHashMap 和 TreeMap 
三个映射类基于不同的数据结构，并实现了不同的功能。HashMap 底层基于拉链式的散列结构，并在 JDK 1.8 中引入红黑树优化过长链表的问题。
基于这样结构，HashMap 可提供高效的增删改查操作。LinkedHashMap 在其之上，通过维护一条双向链表，实现了散列数据结构的有序遍历。
TreeMap 底层基于红黑树实现，利用红黑树的性质，实现了键值对排序功能。我在前面几篇文章中，
对 HashMap 和 TreeMap 以及他们均使用到的红黑树进行了详细的分析，有兴趣的朋友可以去看看。