# Spark Base Station(Spark基础设施) 
>"道生一，一生二，二生三，三生万物" ——《道德经》
## 1. Spark配置
```scala
//Spark配置以key-value形式存在，由ConcurrentHashMap确保线程安全
private val settings = new ConcurrentHashMap[String, String]()
```
* Spark可以获取系统参数中以 `spark.` 开头的那部分属性
* 使用SparkConf提供的API来设置
* 从其他SparkConf中复制
### 1.1 系统属性中的配置
在SparkConf的构造函数中，有一个Boolean的属性loadDefaults，当loadDefaults为true时，Spark会从系统属性中获取配置信息，，loadDefaults默认为true
```scala
if (loadDefaults) {
  loadFromSystemProperties(false)
}

private[spark] def loadFromSystemProperties(silent: Boolean): SparkConf = {
  // Load any spark.* system properties
  for ((key, value) <- Utils.getSystemProperties if key.startsWith("spark.")) {
    set(key, value, silent)
  }
  this
}
```
### 1.2 利用SparkConf提供的API进行配置
除了通过指定系统属性配置Spark，还可以通过SparkConf提供的一些API来对SparkConf进行配置。
#### 1.2.1 set方法
SparkConf中提供一系列的 `set()` 方法来配置Spark，具体代码如下：
```scala
def set(key: String, value: String): SparkConf = {
  set(key, value, false)
}
private[spark] def set(key: String, value: String, silent: Boolean): SparkConf = {
  if (key == null) {
    throw new NullPointerException("null key")
  }
  if (value == null) {
    throw new NullPointerException("null value for " + key)
  }
  if (!silent) {
    logDeprecationWarning(key)
  }
  settings.put(key, value)
  this
}
```
还有一些最常用的方法，例如 `setMaster(),setAppName()` 实际上都是调用了上面的 `set()` 方法代码如下：
```scala
  def setMaster(master: String): SparkConf = {
    set("spark.master", master)
  }
  def setAppName(name: String): SparkConf = {
    set("spark.app.name", name)
  }
```
### 1.3 克隆SparkConf配置
最后一种配置Spark的方法就是克隆一个SparkConf。有些情况下，有多个组件需要使用到SparkConf实例，例如，现在有一个SparkConf实例A，这时组件O1需要使用到A中的配置信息，组件O2也需要使用到A信息，那我们最先想到的解决方案就是将SparkConf的实例设置成全局变量或者通过参数的形式传递出去，但是这回引发并发问题，虽然SparkConf内部的数据结构使用ConcurrentHasMap确保了线程安全问题，而且ConcurrentHashMap被证明在高并发下的性能表现不错，但只要有并发存在就会有性能损耗问题。
Spark采用了另外一种方式，就是再拷贝一封SparkConf实例，即新创建一个SparkConf实例B，将A中的配置信息全部复制给B，这种方式虽然不优雅，复制的SparkConf实例可能会到处都是，但不存在并发问题。
现在我们来看看SparkConf的构造器的详细情况。
```scala
class SparkConf(loadDefaults: Boolean) extends Cloneable with Logging with Serializable {
  def this() = this(true)
}
/** Copy this object，克隆SparkConf配置 */
override def clone: SparkConf = {
  val cloned = new SparkConf(false)
  settings.entrySet().asScala.foreach { e =>
    cloned.set(e.getKey(), e.getValue(), true)
  }
  cloned
}
```
我们可以在任何地方拷贝一封SparkConf来进行操作了。
## 2. RPC框架
Spark时分布式计算框架，每个计算节点之间需要进行通讯，每个组件之间也需要通讯，例如：用户文件或Jar包上传、节点之间Shuffle过程、Block数据的复制和b备份等。  
在Spark1.6.x之前，Spark采用的时Akka框架来支撑Spark内部通讯服务，但在Spark2.x.x版本之后，Spark将Akka移除，采用Netty来支撑RPC框架。  
### 2.1 RPC基本架构
#### 2.1.1 TransportContext-传输上下文
TransportContext中包含上下文配置信息TransportConf和对客户端请求信息进行处理的RpcHandler，构造方法如下所示：
```java
//TransportContext的构造方法
public TransportContext(
    TransportConf conf,//配置信息
    RpcHandler rpcHandler,//处理客户端请求的消息
    boolean closeIdleConnections//是否关闭空闲连接) {
  this.conf = conf;
  this.rpcHandler = rpcHandler;
  this.closeIdleConnections = closeIdleConnections;
}
```
#### 2.1.2 TransportClientFactory-传输客户端工厂类
`TransportClientFactory` 是由 `TransportContext` 的 `createClientFactory` 方法创建而来，具体代码如下：
```java
public TransportClientFactory createClientFactory(List<TransportClientBootstrap> bootstraps) {
  //还会传递客户端引导程序TransportClientBootstrap的列表
  return new TransportClientFactory(this, bootstraps);
}
```
而createClientFactory方法所调用的TransportClientFactory的构造方法如下所示：
```java
public TransportClientFactory(
    TransportContext context,
    List<TransportClientBootstrap> clientBootstraps) {
  this.context = Preconditions.checkNotNull(context);
  this.conf = context.getConf();
  this.clientBootstraps = Lists.newArrayList(Preconditions.checkNotNull(clientBootstraps));
  //每个SocketAddress的都会对应一个连接池
  //private final ConcurrentHashMap<SocketAddress, ClientPool> connectionPool;
  this.connectionPool = new ConcurrentHashMap<>();
  this.numConnectionsPerPeer = conf.numConnectionsPerPeer();
  this.rand = new Random();
  IOMode ioMode = IOMode.valueOf(conf.ioMode());
  this.socketChannelClass = NettyUtils.getClientChannelClass(ioMode);
  //工作线程组
  this.workerGroup = NettyUtils.createEventLoop(
      ioMode,
      conf.clientThreads(),
      conf.getModuleName() + "-client");
  //创建
  this.pooledAllocator = NettyUtils.createPooledByteBufAllocator(
    conf.preferDirectBufs(), false /* allowCache */, conf.clientThreads());
  this.metrics = new NettyMemoryMetrics(
    this.pooledAllocator, conf.getModuleName() + "-client", conf);}
```
有TransportClientFactory的构造方法中可以看出，每个SocketAddress都会对应一个连接池ClientPool，这个连接池的定义如下：
```java
/** A simple data structure to track the pool of clients between two peer nodes. */
private static class ClientPool {
  //ClientPool实际是由TransportClient组成
  TransportClient[] clients;
  Object[] locks;
  ClientPool(int size) {
    clients = new TransportClient[size];
    //locks数组与TransportClient数组按照数据索引一一对应关系，通过对每个TransportClient分别采用独立的锁，降低并发情况下的锁竞争，减少阻塞，提高并发度
    //这个实现方式有点类似于ConcurrentHashMap的分段锁的实现方式
    locks = new Object[size];
    for (int i = 0; i < size; i++) {
      locks[i] = new Object();
    }
  }
}
```
由此可见，ClientPool实际是由TransportClient组成，而且对每个ClientPool采用了类似于ConcurrentHashMap的分段锁实现的方式进行上锁，提高并发度。
#### 2.1.3 TransportServer-传输服务端
`TransportServer` 是由 `TransportContext` 中的 `createServer` 方法创建而来，具体代码如下：
```java
/* Create a server which will attempt to bind to a specific host and port. */
public TransportServer createServer(String host, int port, List<TransportServerBootstrap> bootstraps) {
  return new TransportServer(this, host, port, rpcHandler, bootstraps);
}
```
需要传递TransportContext、host、port、RpcHandler以及服务端引导程序TransportServerBootstrap列表。
### 2.2 RPC包含的组件
#### 2.2.1 TransportContext
传输上下文，包含了用于创建传输服务端(TransportServer)和传输客户端工厂(TransportClientFactory)的上下文信息。并支持使用TransportChannelHandler设置Netty提供的SocketChannel的Pipline实现。
#### 2.2.2 TransConf
传输上下文配置信息
#### 2.2.3 RpcHandler
对调用传输客户端(TransportClient)的sendRPC方法发送的消息进行处理的程序
#### 2.2.4 MessageEncoder
消息编码器，将消息放入pipline之前需要先对消息内容进行编码，防止管道另一端读取时丢包或解析错误
#### 2.2.5 MEssageDecoder
消息解码器，对从管道中读取的ByteBuf进行解析，防止丢包或解析错误
#### 2.2.6 TransportFrameDecoder
对管道中读取到的ByteBuf按照数据帧进行解析
#### 2.2.7 RpcResponseCallback
RpcHandler对请求的消息处理完毕之后会回调的接口
#### 2.2.8 TransportClientFactory
创建TransportClient的传输客户端工厂类
#### 2.2.9 ClientPool
在两个对等节点间维护的关于TransportClient的池子。ClientPool时TransportClientFactory的内部组件
#### 2.2.10 TransportClient
RPC框架的客户端，用于获取预先协商好的流中的连续块。旨在允许有效传输大量数据，这些数据将被拆分成几百KB到几百MN的块。当TransportClient处理从流中获取的块时，实际的设置在传输层之外完成的。sendRPC方法能够在客户端和服务端的统一水平线的通讯进行这些设置。
#### 2.2.11 TransportClientBootstrap
当服务端响应客户端连接时，在客户端执行一次的引导程序
#### 2.2.12 TransportRequestHandler
用于处理客户端的请求并在写完块数据后返回的处理程序
#### 2.2.13 TransportResponseHandler
用于处理服务端的响应，并且在对发出请求的客户端进行响应的处理程序
#### 2.2.14 TransportChannelHandler
代理由TransportRequestHandler处理的请求和由TransportResponseHandler处理的响应，并加入传输层处理
#### 2.2.15 TransportServerBootstrap
当客户端连接到服务端时在服务端执行一次的引导程序
#### TransportServer
RPC框架的服务端，提供高效的、低级别的流服务