# Java集合
## ArrayList
使用数组实现的List，具有自动扩容机制，在实际使用的过程中，需要尽量避免List的频繁扩容以免损失性能。
### 重要属性
#### 1. private static final int DEFAULT_CAPACITY = 10;
默认的ArrayList的容量大小，如果在初始化ArrayList时没有指定初始容量，或者指定的初始容量为0时，在下一次添加数据时，将会将容量设置为该值。
#### 2. private static final Object[] EMPTY_ELEMENTDATA = {};
当初始化ArrayList时，指定的容量为0时，则将内部数组实例化为该值
#### 3. private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
默认的空数据，当使用空构造器初始化ArrayList时，内部数组被初始化该值
#### 4. transient Object[] elementData
ArrayList中真正存数据的数组
#### 5. private int size
ArrayList中的元素的个数
### 重要方法
#### public boolean add(E e)
##### 1. 源码
````java
public boolean add(E e) {
    //当前需要的最小容量size+1
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}
private void ensureCapacityInternal(int minCapacity) {
    ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}
private static int calculateCapacity(Object[] elementData, int minCapacity) {
    //如果当前数组为默认的额空数组，那么就取当前需要的最小容量和默认的容量的最大值
    //一旦扩容过一次之后，原本存放数据的数组就不再是DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        //默认容量和当前最小容量进行比对，取较大值
        return Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    return minCapacity;
}
private void ensureExplicitCapacity(int minCapacity) {
    //修改次数加一，方便快速失败的判断
    modCount++;
    // overflow-conscious code
    //如果当前需要的最小容量大于当前存放数据的数组的长度，则进行扩容
    if (minCapacity - elementData.length > 0)
        //扩容
        grow(minCapacity);
}
````
##### 2. 解析
在ArrayList的末尾添加一个元素，在添加元素之前都会对当前需要的最小容量(size+1)和当前的存储数据的数组的容量进行比对，
如果当前需要的最小容量大于则进行扩容，否则将元素添加到数组的末尾。
该方法会导致modCount的值加一。
#### private void grow(int minCapacity)
##### 1. 源码
```java
private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    //扩容为原来容量的1.5倍
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    //如果扩容之后的容量小于最小需要的容量，那么就将容量扩容到需要的最小容量
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    //如果扩容之后的容量比最大容量的值还大，那么就将新同容量扩容到最大允许容量
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    //将旧数据拷贝到新数组中
    elementData = Arrays.copyOf(elementData, newCapacity);
}
private static int hugeCapacity(int minCapacity) {
    //如果需要的最小容量溢出了就抛出异常
    if (minCapacity < 0) // overflow
        throw new OutOfMemoryError();
    //返回ArrayList的容量
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
}
```
##### 2. 解析
扩容的策略还是比较容易理解的，有以下几个步骤：
1. 首先扩容到近似原来数组容量的1.5倍
2. 需要的最小容量和扩容之后的大小进行比对。
    1. 如果需要的最小容量的大小小于扩容之后的容量的话，则将旧数据拷贝到新数组中即可
    2. 如果需要的最小容量小于扩容之后的容量的话，则将存储数据的数组容量扩容到需要的最小容量之后再进行数据拷贝
    3. 如果需要的容量超过了最大容量，则将数组扩容到最大容量
#### public void add(int index, E element)
##### 1. 源码
```java
public void add(int index, E element) {
    //越界检查
    rangeCheckForAdd(index);
    //扩容校验，最小需要的容量为size+1
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    //将就数组中index以后的数据向后移一位
    System.arraycopy(elementData, index, elementData, index + 1, size - index);
    elementData[index] = element;
    size++;
}
private void rangeCheckForAdd(int index) {
    //同时检查上界和下界
    if (index > size || index < 0)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```
##### 2. 解析
需要检查上界和下界，将插入的index包括index索引位的值，往后移动一位，将需要插入的元素插入到index索引位即可。
#### public E remove(int index)
##### 1. 源码
```java
public E remove(int index) {
    //越界检查
    rangeCheck(index);
    //修改次数加一，方便快速失败
    modCount++;
    //取出旧值
    E oldValue = elementData(index);
    //需要移动的元素的个数
    int numMoved = size - index - 1;
    if (numMoved > 0)
        //将index位置上的元素覆盖掉
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    //将最后一位置为null，方便GC进行回收
    elementData[--size] = null; // clear to let GC do its work
    return oldValue;
}
```
##### 2. 解析
移除指定索引位置上的元素，会导致修改次数加一，并且返回原来索引位上的旧值。将最后一位置为null，方便GC回收无用对象
#### public boolean remove(Object o)
##### 1. 源码
```java
public boolean remove(Object o) {   
    //如果要删除的元素为null
    if (o == null) {
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                //快速删除
                fastRemove(index);
                return true;
            }
    //如果删除的元素不为null
    } else {
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                //快速删除
                fastRemove(index);
                return true;
            }
    }
    return false;
}
//快速删除，不校验边界，直接移除元素
private void fastRemove(int index) {
    //修改次数加一
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    //最后一个元素置为null，方便GC回收无用对象
    elementData[--size] = null; // clear to let GC do its work
}
```
##### 2. 解析
快速删除在ArrayList中第一个与Object对象相等的值
#### public E set(int index, E element)
##### 1. 源码
```java
public E set(int index, E element) {
    //边界检查
    rangeCheck(index);
    //取出旧值
    E oldValue = elementData(index);
    //将新新值覆盖到旧值
    elementData[index] = element;
    //返回旧值
    return oldValue;
}
E elementData(int index) {
    return (E) elementData[index];
}
private void rangeCheck(int index) {
    if (index >= size)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```
##### 2. 解析
将指定索引位的值修改为指定的值并返回旧值
#### public boolean removeAll(Collection<?> c) / public boolean retainAll(Collection<?> c)
##### 1. 源码
```java
public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return batchRemove(c, false);
}
public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return batchRemove(c, true);
}
private boolean batchRemove(Collection<?> c, boolean complement) {
    final Object[] elementData = this.elementData;
    int r = 0, w = 0;
    boolean modified = false;
    try {
        for (; r < size; r++)
            if (c.contains(elementData[r]) == complement)
                elementData[w++] = elementData[r];
    } finally {
        // Preserve behavioral compatibility with AbstractCollection,
        // even if c.contains() throws.
        if (r != size) {
            System.arraycopy(elementData, r,
                             elementData, w,
                             size - r);
            w += size - r;
        }
        if (w != size) {
            // clear to let GC do its work
            for (int i = w; i < size; i++)
                elementData[i] = null;
            modCount += size - w;
            size = w;
            modified = true;
        }
    }
    return modified;
}
```
### 内部类
#### Itr
Itr实现了Iterator接口，提供遍历功能
#### 注意事项
在使用Itr提供的迭代功能的时候，不可以使用ArrayList提供的remove方法，如果使用了ArrayList提供的方法，会导致modCount发生变化，但是Itr中的next方法中，每次都会去校验modCount和expectModCount，如果两者不相等，
则表示List在迭代过程中发生了变化，会抛出ConcurrentModifyException。
```java
public E next() {
    //校验modCount和expectModCount，校验ArrayList是否被修改过
    checkForComodification();
    int i = cursor;
    if (i >= size)
        throw new NoSuchElementException();
    Object[] elementData = ArrayList.this.elementData;
    if (i >= elementData.length)
        throw new ConcurrentModificationException();
    cursor = i + 1;
    return (E) elementData[lastRet = i];
}
final void checkForComodification() {
    if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
}
```
如果想在迭代过程中删除元素，需要使用Itr提供的remove方法，实际还是调用的ArrayList中的remove方法，但此方法会在remove之后校准modCount和expect，以免下一次next时报错。
```java
public void remove() {
    if (lastRet < 0)
        throw new IllegalStateException();
    checkForComodification();
    try {
        //实际调用ArrayList中的remove方法
        ArrayList.this.remove(lastRet);
        //将最后一个元素的下一个元素的索引往前挪一位
        cursor = lastRet;
        //重置最后一个元素的索引
        lastRet = -1;
        //校准expectModCount和modCount，以免下一次的next报错
        expectedModCount = modCount;
    } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
    }
}
```
在Java8中新提供的forEachRemaining方法，同样的此方法不能够对元素进行移除.
```java
@Override
public void forEachRemaining(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    final int size = ArrayList.this.size;
    int i = cursor;
    if (i < size) {
        final Object[] es = elementData;
        if (i >= es.length)
            throw new ConcurrentModificationException();
        for (; i < size && modCount == expectedModCount; i++)
            action.accept(elementAt(es, i));
        // update once at end to reduce heap write traffic
        cursor = i;
        lastRet = i - 1;
        checkForComodification();
    }
}
```
#### ListItr
ListItr继承自Itr，实现了ListIterator接口，提供从后向前遍历的能力
## LinkedList
