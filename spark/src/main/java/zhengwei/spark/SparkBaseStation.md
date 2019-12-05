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
  //RPC上下文引用
  this.context = Preconditions.checkNotNull(context);
  //指的是TransportConf，这里通过TransportContext的getConf来获取
  this.conf = context.getConf();
  //参数传递的TransportClientBootstrap列表
  this.clientBootstraps = Lists.newArrayList(Preconditions.checkNotNull(clientBootstraps));
  //每个SocketAddress的都会对应一个连接池
  //private final ConcurrentHashMap<SocketAddress, ClientPool> connectionPool;
  //针对每个SocketAddress都有一个ClientPool的缓存
  this.connectionPool = new ConcurrentHashMap<>();
  //
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
传输上下文配置信息，即RPC配置
```java
//配置真正的提供者
private final ConfigProvider conf;
//模块名
private final String module;
```
ConfigProvider是一个抽象类，其内部提供了一些获取值的方法，其具体代码如下所示：
```java
public abstract class ConfigProvider {
  /** Obtains the value of the given config, throws NoSuchElementException if it doesn't exist. */
  //需要子类去实现具体的获取值的方法
  public abstract String get(String name);

  /** Returns all the config values in the provider. */
  public abstract Iterable<Map.Entry<String, String>> getAll();

  public String get(String name, String defaultValue) {
    try {
      return get(name);
    } catch (NoSuchElementException e) {
      return defaultValue;
    }
  }

  public int getInt(String name, int defaultValue) {
    return Integer.parseInt(get(name, Integer.toString(defaultValue)));
  }

  public long getLong(String name, long defaultValue) {
    return Long.parseLong(get(name, Long.toString(defaultValue)));
  }

  public double getDouble(String name, double defaultValue) {
    return Double.parseDouble(get(name, Double.toString(defaultValue)));
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    return Boolean.parseBoolean(get(name, Boolean.toString(defaultValue)));
  }

}
```
其中的 `getInt` , `getLong` , `getDouble` , `getBoolean`方法都是居于 `get` 方法，具体逻辑由子类去实现。  
查看了TransportConf中两个重要的属性ConfigProvider和module，那么TransportConf是如何被创建的呢？Spark是通过SparkTransportConf来创建TransportConf的，具体代码如下：
```scala
object SparkTransportConf {
  private val MAX_DEFAULT_NETTY_THREADS = 8
  def fromSparkConf(_conf: SparkConf, module: String, numUsableCores: Int = 0): TransportConf = {
    //直接克隆一份SparkConf
    val conf = _conf.clone

    // Specify thread configuration based on our JVM's allocation of cores (rather than necessarily
    // assuming we have all the machine's cores).
    // NB: Only set if serverThreads/clientThreads not already set.
    val numThreads = defaultNumThreads(numUsableCores)
    //设置用于服务端传输线程数量
    conf.setIfMissing(s"spark.$module.io.serverThreads", numThreads.toString)
    //设置用于客户端传输线程数量
    conf.setIfMissing(s"spark.$module.io.clientThreads", numThreads.toString)
    //创建Transport时，传递了实现了ConfigProvider中get方法的匿名实现类
    new TransportConf(module, new ConfigProvider {
      override def get(name: String): String = conf.get(name)
      override def get(name: String, defaultValue: String): String = conf.get(name, defaultValue)
      override def getAll(): java.lang.Iterable[java.util.Map.Entry[String, String]] = {
        conf.getAll.toMap.asJava.entrySet()
      }
    })
  }
  private def defaultNumThreads(numUsableCores: Int): Int = {
    val availableCores = if (numUsableCores > 0) numUsableCores else Runtime.getRuntime.availableProcessors()
    math.min(availableCores, MAX_DEFAULT_NETTY_THREADS)
  }
}
```
从SparkTransportConf#fromSparkConf这个方法可以直到，创建TransportConf是从SparkConf中得到的。  
主要传递三个参数，Spark的配置信息SparkConf、module模块名和内核数numUsableCores。如果numUsableCores的数量小于0，那么就取当先可用的线程数量，用于网络传输的最大线程数量最大就为8个，最终确定了线程数量之后就回去设置，客户端传输线程数(spark.$module.io.clientThreads)和服务端传输线程数(spark.$module.io.serverThreads).  

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
TransportClientFactory由TransportConf#createClientFactory来创建，具体代码如下：
```java
public TransportClientFactory createClientFactory(List<TransportClientBootstrap> bootstraps) {
  return new TransportClientFactory(this, bootstraps);
}
public TransportClientFactory createClientFactory() {
  return createClientFactory(new ArrayList<>());
}
```
可以看到有两个重载的方法，但最终都是调用的TransportClientFactory的构造方法，具体构造方法代码如下：
```java
public TransportClientFactory(
    TransportContext context,
    List<TransportClientBootstrap> clientBootstraps) {
  this.context = Preconditions.checkNotNull(context);
  this.conf = context.getConf();
  this.clientBootstraps = Lists.newArrayList(Preconditions.checkNotNull(clientBootstraps));
  this.connectionPool = new ConcurrentHashMap<>();
  this.numConnectionsPerPeer = conf.numConnectionsPerPeer();
  this.rand = new Random();

  IOMode ioMode = IOMode.valueOf(conf.ioMode());
  this.socketChannelClass = NettyUtils.getClientChannelClass(ioMode);
  this.workerGroup = NettyUtils.createEventLoop(
      ioMode,
      conf.clientThreads(),
      conf.getModuleName() + "-client");
  this.pooledAllocator = NettyUtils.createPooledByteBufAllocator(
    conf.preferDirectBufs(), false /* allowCache */, conf.clientThreads());
  this.metrics = new NettyMemoryMetrics(
    this.pooledAllocator, conf.getModuleName() + "-client", conf);
}
```
* context：即TransportContext的引用
* conf：即TransportConf，通过TransportContext#getConf方法来获取
* clientBootstrap：参数传递的TransportClientBootstrap列表
* connectionPool：针对每个SocketAddress的ClientPool的缓存，connectionPool的数据结构为：SocketAddress->ClientPool,ClientPool->\[clients,objects\]
* numConnectionPerPeer：从TransportConf中获取key为 "spark.模块名.io.numConnectionsPerPeer" 的属性值，用于指定对等节点间的连接数。这里的模块名实际为TransportConf中的module字段，Spark有很多组件都是利用RPC框架构建的，他们之间使用模块名进行区分，例如，RPC模块的key为 "spark.模块名.io.numConnectionsPerPeer"
* rand：对Socket地址对应的连接池ClientPool中的缓存的TransportClient进行随机选择，做到负载均衡
* ioMode：IO模式，即从TransportConf获取key为 "spark.模块名.io.mode" 的属性值。默认为NIO，Spark还支持EPOLL
* socketChannelClass：客户端Channel被创建时使用的类，通过ioMode来匹配，默认NioSocketChannel，Spark还支持EpollEventLoopGroup
* workerGroup：根据Netty规范，客户端只有worker组，所以此处创建workerGroup。workerGroup实际类型时NioEventLoopGroup
* pooledAllocator：汇集ByteBuf但对本地线程缓存禁用的分配器。
##### 2.2.8.1 客户端引导程序-TransportClientBootstrap
TransportClientFactory的clientBootstraps属性的类型是TransportClientBootstrap。TransportClientBootstrap是TransportClient中执行的客户端启动引导程序，主要是做一些初始化动作。  
TransportClientBootstrap所做的操作都是比较昂贵的，好在建立好连接之后是可以复用的。TransportClientBootstrap接口定义如下：
```java
public interface TransportClientBootstrap {
  /** Performs the bootstrapping operation, throwing an exception on failure. */
  void doBootstrap(TransportClient client, Channel channel) throws RuntimeException;
}
```
TransportClientBootstrap接口有两个实现类，AuthClientBootstrap和SaslClientBootstrap.
##### 2.2.8.2 创建RPC客户端-TransportClient
有了TransportClientFactory之后。Spark的各个组件就可以使用它来创建用于通讯的TransportClient了。每个TransportClient只能和一个远端的RPC服务通讯，所以Spark中的组件如果想要和多个RPC服务通讯，就需要只有多个TransportClient实例。
创建TransportClient的方法代码如下(**实际是从缓存中获取TransportClient**)：
```java
//org.apache.spark.network.client.TransportClientFactory.createClient(java.lang.String, int)
public TransportClient createClient(String remoteHost, int remotePort)
    throws IOException, InterruptedException {
  // Get connection from the connection pool first.
  // If it is not found or not active, create a new one.
  // Use unresolved address here to avoid DNS resolution each time we creates a client.
  final InetSocketAddress unresolvedAddress =
    InetSocketAddress.createUnresolved(remoteHost, remotePort);

  // Create the ClientPool if we don't have it yet.
  ClientPool clientPool = connectionPool.get(unresolvedAddress);
  if (clientPool == null) {
    connectionPool.putIfAbsent(unresolvedAddress, new ClientPool(numConnectionsPerPeer));
    clientPool = connectionPool.get(unresolvedAddress);
  }

  int clientIndex = rand.nextInt(numConnectionsPerPeer);
  TransportClient cachedClient = clientPool.clients[clientIndex];

  if (cachedClient != null && cachedClient.isActive()) {
    // Make sure that the channel will not timeout by updating the last use time of the
    // handler. Then check that the client is still alive, in case it timed out before
    // this code was able to update things.
    TransportChannelHandler handler = cachedClient.getChannel().pipeline()
      .get(TransportChannelHandler.class);
    synchronized (handler) {
      handler.getResponseHandler().updateTimeOfLastRequest();
    }

    if (cachedClient.isActive()) {
      logger.trace("Returning cached connection to {}: {}",
        cachedClient.getSocketAddress(), cachedClient);
      return cachedClient;
    }
  }

  // If we reach here, we don't have an existing connection open. Let's create a new one.
  // Multiple threads might race here to create new connections. Keep only one of them active.
  final long preResolveHost = System.nanoTime();
  final InetSocketAddress resolvedAddress = new InetSocketAddress(remoteHost, remotePort);
  final long hostResolveTimeMs = (System.nanoTime() - preResolveHost) / 1000000;
  if (hostResolveTimeMs > 2000) {
    logger.warn("DNS resolution for {} took {} ms", resolvedAddress, hostResolveTimeMs);
  } else {
    logger.trace("DNS resolution for {} took {} ms", resolvedAddress, hostResolveTimeMs);
  }

  synchronized (clientPool.locks[clientIndex]) {
    cachedClient = clientPool.clients[clientIndex];

    if (cachedClient != null) {
      if (cachedClient.isActive()) {
        logger.trace("Returning cached connection to {}: {}", resolvedAddress, cachedClient);
        return cachedClient;
      } else {
        logger.info("Found inactive connection to {}, creating a new one.", resolvedAddress);
      }
    }
    clientPool.clients[clientIndex] = createClient(resolvedAddress);
    return clientPool.clients[clientIndex];
  }
}
```
1. 调用InetSocket的静态方法createUnresolved构建InetSocketAddress(这种方式创建创建InetSocketAddress，可以在缓存中已经有TransportClient时避免不必要的域名解析)，然后从connectionPool中获取与此地址对于的clientPool，如果没有，则需要新建ClientPool，并放入缓存connectionPool中。
2. 获取"spark.模块名.io.numConnectionsPerPeer" 的属性值，从ClientPool数组中随机获取一个TransportClient
3. 如果ClientPool的clients数组中在随机产生的索引处的TransportClient不存在或者没有激活，则进入第5步，否则对TransportClient进行第4步检查
4. 更新TransportClient的channel中配置的TransportChannelHandler的最后一次使用时间，确保channel没有超时，然后检查TransportClient是否时激活状态，最后返回TransportClient给调用这
5. 由于随机选取到的TransportClient不可用，直接实例化一个InetSocketAddress(调用构造器去实例化InetSocketAddress会进行域名解析)，在这一步多个线程会产生竞争(由于没有做同步处理，所以极有可能多个线程同时执行到这，都发现没有TransportClient可用，于是都使用InetSocketAddress构造器去构造实例)
6. 由于第5步创建InetSocketAddress的过程中产生的竞态条件如果处理不妥当，会产生线程安全问题，那么ClientPool中的locks就发挥作用了。按照随机产生的数组索引，locks中的对象可以对cliens中的TransportClient进行一对一的加锁。即便之前产生了竞态条件，但到这一步时只会有一个进行临界区。在临界区中，先进入的线程调用重载的createClient创建TransportClient对象并放入到clients数组中。之后再进入临界区的线程会发现此时索引处已有Transport对象，则不会再去创建而是直接去使用

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
#### 2.2.16 TransportServer
RPC框架的服务端，提供高效的、低级别的流服务