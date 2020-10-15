# ThreadLocal
首先思考几个问题  
* 什么是 ThreadLocal ？用来解决什么问题？  
* 说说你对 ThreadLocal 的理解？  
* ThreadLocal 是如何实现线程隔离的？  
* 为什么 ThreadLocal 会造成内存溢出？  
* ThreadLocal 还有哪些应用场景？

## ThreadLocal 简介
>This class provides thread-local variables. These variables differ from their normal counterparts in that each thread 
>that accesses one (via its {@code get} or {@code set} method) has its own, independently initialized copy of the 
>variable. {@code ThreadLocal} instances are typically private static fields in classes that wish to associate state 
>with a thread (e.g., a user ID or Transaction ID)

>该类提供了线程局部 (thread-local) 变量。这些变量不同于它们的普通对应物，因为访问某个变量(通过其 get 或 set 方法)的每个线程都有自己
>的局部变量，它独立于变量的初始化副本。ThreadLocal 实例通常是类中的 private static 字段，它们希望将状态与某一个线程
>(例如，用户 ID 或事务 ID)相关联。

总结而言：ThreadLocal 是一个将在多线程中为每一个线程创建单独的变量副本的类; 当使用 ThreadLocal 来维护变量时, 
ThreadLocal 会为每个线程创建单独的变量副本, 避免因多线程操作共享变量而导致的数据不一致的情况。

## ThreadLocal 理解
>提到 ThreadLocal 被提到应用最多的是 session 管理和数据库连接，这里以数据库连接举例

* 如下数据库管理类在单线程环境下使用是没有问题的
```java
class ConnectionManager {
    private static Connection connect = null;

    public static Connection openConnection() {
        if (connect == null) {
            connect = DriverManager.getConnection();
        }
        return connect;
    }

    public static void closeConnection() {
        if (connect != null)
            connect.close();
    }
}
```
很显然，在多线程中使用h会存在线程安全问题：
1. 这里面的两个方法没有做同步处理，很可能在 openConnection 时创建多个connection
2. 由于 connection 是共享变量，那么必然在调用 connection 需要使用同步来保障线程安全问题，因为很有可能一个线程在使用 connection 时
另外一个线程关闭了 connection

* 为了解决上述线程安全问题，第一考虑：互斥同步  
最直接的方法就是在 openConnection 方法和 closeConnection 方法声明上和调用 connection 的地方加上 
Synchronized 或 ReentrantLock 互斥锁

* 是否真的需要加上互斥锁？  
事实上是不需要的，加入每个线程都持有一个 connection 对象，各个线程之间对 connection 对象的访问实际上是没有依赖关系的，即一个线程不
需要关系其他线程是否对这个 connection 进行了修改，修改后的代码如下：
```java
class ConnectionManager {
    private Connection connect = null;

    public Connection openConnection() {
        if (connect == null) {
            connect = DriverManager.getConnection();
        }
        return connect;
    }

    public void closeConnection() {
        if (connect != null)
            connect.close();
    }
}

class Dao {
    public void insert() {
        ConnectionManager connectionManager = new ConnectionManager();
        Connection connection = connectionManager.openConnection();

        // 使用connection进行操作

        connectionManager.closeConnection();
    }
}
```
这样的处理方式也没有什么不妥，由于每次都是在方法内部创建的连接，那么线程之间自然不会产生线程安全问题。但是缺点也很明显：那就是频繁的创建
和销毁 connection 极其消耗性能，会给服务器带来巨大的压力，影响服务器性能。

* ThreadLocal 登场
这种情况下使用 ThreadLocal 再合适不过，因为 ThreadLocal 在每个线程中对该变量创建一份副本，即每个线程内部都会有一个改变量，且在线程
内部任何地方都能使用它，线程互不影响，这样一来就不存在线程安全问题，也不会影响服务器的性能，例子如下：
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static final ThreadLocal<Connection> dbConnectionLocal = new ThreadLocal<Connection>() {
        @Override
        protected Connection initialValue() {
            try {
                return DriverManager.getConnection("", "", "");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    public Connection getConnection() {
        return dbConnectionLocal.get();
    }
}
```
**需要注意下 ThreadLocal 的修饰符，为 static final**


ThreadLocal 的 JDK 文档中说明：
ThreadLocal instances are typically private static fields in classes that wish to associate state with a thread。
如果我们希望通过某个类将状态(例如用户ID、事务ID)与线程关联起来，那么通常在这个类中定义private static类型的ThreadLocal 实例。

>但是要注意，虽然ThreadLocal能够解决上面说的问题，但是由于在每个线程中都创建了副本，所以要考虑它对资源的消耗，
>比如内存的占用会比不使用ThreadLocal要大。

## ThreadLocal 原理
### 如何实现线程隔离
主要是通过 Thread 中的 `ThreadLocal.ThreadLocalMap threadLocals` 来存储副本变量，以上面的例子为例，将 `dbConnectionLocal` 作为 
key，以新建的 Connection 作为 value，这样的话，线程第一次读取的时候如果不存在的话就会 ThreadLocal 的 `initialValue` 方法创建一个 
Connection 对象并返回
```java
public T get() {
    // 获取当前线程
    Thread t = Thread.currentThread();
    // 获取当前线程的 threadLocals 属性
    ThreadLocalMap map = getMap(t);
    // 如果
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}

ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}

// 初始化 ThreadLocalMap 或者为 ThreadLocalMap 赋值
private T setInitialValue() {
    // 由我们自己实现，创建一个需要的对象
    T value = initialValue();
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    // ThreadLocalMap 已经初始化
    if (map != null)
        // 把新建的对象设置到 ThreadLocalMap 中
        map.set(this, value);
    // ThreadLocalMap 还未初始化
    else
        // 初始化 ThreadLocalMap，并把新建的 value 以 ThreadLocal 为 key 设置到 ThreadLocalMap 中
        createMap(t, value);
    return value;
}

void createMap(Thread t, T firstValue) {
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```
具体步骤如下：
1. 首先获取当前线程
2. 获取当前线程的 `ThreadLocal.ThreadLocalMap threadLocals` 属性
3. 如果当前线程的 `ThreadLocalMap` 已经初始化过了，并且存在以当前 `ThreadLocal` 为 key 的 value 的话，那么直接返回当前线程要获取
的对象值(本例中是 Connection 对象)
4. 如果当前线程的 `ThreadLocalMap` 已经初始化过了，但是不存在以当前 `ThreadLocal` 为 key 的 value，即本例中不存在 Connection 对象，
那么就新建一个 value (Connection) 对象，并设置到 `ThreadLocalMap` 中去
5. 如果 ThreadLocalMap 没有初始化，则新建一个 ThreadLocalMap 对象，并把新建的 value 设置到 ThreadLocalMap 中并返回
