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
传输上下文，包含了用于创建传输服务端(TransportServer)和传输客户端工厂(TransportClientFactory)的上下文信息。并支持使用TransportChannelHandler设置Netty提供的SocketChannel的Pipeline实现。
管道初始化，在 `org.apache.spark.network.client.TransportClientFactory.createClient(java.net.InetSocketAddress)` 和 `org.apache.spark.network.server.TransportServer.init` 中都调用了TransportContext的initializePipeline方法，主要是调用Netty的API对管道进行初始化，其代码如下：
```java
/*
 * Initializes a client or server Netty Channel Pipeline which encodes/decodes messages and
 * has a {@link org.apache.spark.network.server.TransportChannelHandler} to handle request or
 * response messages.
 *
 * @param channel The channel to initialize.
 * @param channelRpcHandler The RPC handler to use for the channel.
 *
 * @return Returns the created TransportChannelHandler, which includes a TransportClient that can
 * be used to communicate on this channel. The TransportClient is directly associated with a
 * ChannelHandler to ensure all users of the same channel get the same TransportClient object.
 */
public TransportChannelHandler initializePipeline(
    SocketChannel channel,
    RpcHandler channelRpcHandler) {
  try {
    TransportChannelHandler channelHandler = createChannelHandler(channel, channelRpcHandler);
    channel.pipeline()
      .addLast("encoder", ENCODER)
      .addLast(TransportFrameDecoder.HANDLER_NAME, NettyUtils.createFrameDecoder())
      .addLast("decoder", DECODER)
      .addLast("idleStateHandler", new IdleStateHandler(0, 0, conf.connectionTimeoutMs() / 1000))
      // NOTE: Chunks are currently guaranteed to be returned in the order of request, but this
      // would require more logic to guarantee if this were not part of the same event loop.
      .addLast("handler", channelHandler);
    return channelHandler;
  } catch (RuntimeException e) {
    logger.error("Error while initializing Netty pipeline", e);
    throw e;
  }
}
private TransportChannelHandler createChannelHandler(Channel channel, RpcHandler rpcHandler) {
  TransportResponseHandler responseHandler = new TransportResponseHandler(channel);
  //真正的去创建TransportClient
  //TransportClient和RpcHandler并没有关系，TransportClient只与TransportChannelHandler存在直接关系
  TransportClient client = new TransportClient(channel, responseHandler);
  TransportRequestHandler requestHandler = new TransportRequestHandler(channel, client, rpcHandler, conf.maxChunksBeingTransferred());
  //TransportChannelHandler在服务端将代理TransportRequestHandler对请求进行处理，并在客户端代理TransportResponseHandler对响应消息进行处理
  return new TransportChannelHandler(client, responseHandler, requestHandler, conf.connectionTimeoutMs(), closeIdleConnections);
}
```
initializePipeline的执行过程如下：
1. 调用createChannelHandler方法创建TransportChannelHandler，从createChannelHandler的代码中可以看出，TransportChannelHandler中包含一个TransportClient，这个TransportClient直接与ChannelHandler关联是用来确保同一通道的所有用户获得相同的TransportClient对象。
2. 对管道进行设置，这里的 `ENCODER(MessageEncoder)` 派生自Netty的ChannelOutboundHandler接口， `DECODER(MessageDecoder)` 、TransportChannelHandler以及TransportFrameDecoder(由NettyUtils的NettyUtils.createFrameDecoder()创建)派生自Netty的ChannelInboundHandler接口；IdleStateHandler同时实现了ChannelOutboundHandler和ChannelInboundHandler
根据Netty的API行为，通过addLast方法注册多个Handler的时候ChannelInboundHandler按照注册的先后顺序执行，ChannelOutboundHandler按照注册的先后顺序逆序执行。
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
#### 2.2.5 MessageDecoder
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
那么TransportClientFactory是怎么创建TransportClient的呢？在获取TransportClientFactory中有几个createClient重载方法，而真正去创建TransportClient的重载方法是 `org.apache.spark.network.client.TransportClientFactory.createClient(java.net.InetSocketAddress)` 方法，其具体实现如下：
```java
  /* Create a completely new {@link TransportClient} to the remote address. */
  private TransportClient createClient(InetSocketAddress address)
      throws IOException, InterruptedException {
    logger.debug("Creating new connection to {}", address);
    //客户端引导程序，并对引导程序进行设置
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(workerGroup)
      .channel(socketChannelClass)
      // Disable Nagle's Algorithm since we don't want packets to wait
      .option(ChannelOption.TCP_NODELAY, true)
      .option(ChannelOption.SO_KEEPALIVE, true)
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, conf.connectionTimeoutMs())
      .option(ChannelOption.ALLOCATOR, pooledAllocator);

    if (conf.receiveBuf() > 0) {
      bootstrap.option(ChannelOption.SO_RCVBUF, conf.receiveBuf());
    }

    if (conf.sendBuf() > 0) {
      bootstrap.option(ChannelOption.SO_SNDBUF, conf.sendBuf());
    }

    final AtomicReference<TransportClient> clientRef = new AtomicReference<>();
    final AtomicReference<Channel> channelRef = new AtomicReference<>();
    //为根引导程序设置管道初始化回调函数
    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      public void initChannel(SocketChannel ch) {
        TransportChannelHandler clientHandler = context.initializePipeline(ch);
        clientRef.set(clientHandler.getClient());
        channelRef.set(ch);
      }
    });

    // Connect to the remote server
    long preConnect = System.nanoTime();
    //连接远程服务器
    ChannelFuture cf = bootstrap.connect(address);
    if (!cf.await(conf.connectionTimeoutMs())) {
      throw new IOException(
        String.format("Connecting to %s timed out (%s ms)", address, conf.connectionTimeoutMs()));
    } else if (cf.cause() != null) {
      throw new IOException(String.format("Failed to connect to %s", address), cf.cause());
    }

    TransportClient client = clientRef.get();
    Channel channel = channelRef.get();
    assert client != null : "Channel future completed successfully with null client";

    // Execute any client bootstraps synchronously before marking the Client as successful.
    long preBootstrap = System.nanoTime();
    logger.debug("Connection to {} successful, running bootstraps...", address);
    try {
      for (TransportClientBootstrap clientBootstrap : clientBootstraps) {
        //给TransportClient设置客户端引导程序
        clientBootstrap.doBootstrap(client, channel);
      }
    } catch (Exception e) { // catch non-RuntimeExceptions too as bootstrap may be written in Scala
      long bootstrapTimeMs = (System.nanoTime() - preBootstrap) / 1000000;
      logger.error("Exception while bootstrapping client after " + bootstrapTimeMs + " ms", e);
      client.close();
      throw Throwables.propagate(e);
    }
    long postBootstrap = System.nanoTime();

    logger.info("Successfully created connection to {} after {} ms ({} ms spent in bootstraps)",
      address, (postBootstrap - preConnect) / 1000000, (postBootstrap - preBootstrap) / 1000000);

    return client;
  }
```
真正创建TransportClient的步骤如下：
1. 构建客户端引导程序，并对其进行设置
2. 在根引导程序的初始化回调函数中调用TransportContext的initializePipeline方法对Channel的pipeline进行初始化
3. 使用根引导程序连接远程服务器成功并且初始化Channel的pipeline成功时，将TransportClient和Channel的引用设置到TransportClient原子引用和Channel原子中
4. 给TransportClient设置引导程序，即设置TransportClientFactory中的TransportBootstrap列表
5. 返回此TransportClient对象
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
TransportChannelHandler实现了Netty的ChannelInboundHandler接口，以便对管道中的消息进行处理，重写了channelRead方法，Netty采用工作链模式对每个ChannelInboundHandler的实现类的channelRead进行链式调用。  
TransportChannelHandler实现的channelRead方法的具体代码如下：
```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {
  //如果处理的消息是RequestMessage的话则交由TransportRequestHandler做进一步处理
  if (request instanceof RequestMessage) {
    requestHandler.handle((RequestMessage) request);
  //如果处理的消息是ResponseMessage的话则交由TransportResponseHandler做进一步处理
  } else if (request instanceof ResponseMessage) {
    responseHandler.handle((ResponseMessage) request);
  } else {
    ctx.fireChannelRead(request);
  }
}
```
##### 2.2.14.1 MessageHandler的继承体系
TransportRequestHandler和TransportResponseHandler都继承自抽象类MessageHandler，MessageHandler定义了字类的规范，其具体代码如下：
```java
public abstract class MessageHandler<T extends Message> {
  /** Handles the receipt of a single message. */
  //用于对接受到的单个消息进行处理
  public abstract void handle(T message) throws Exception;
  /** Invoked when the channel this MessageHandler is on is active. */
  //当channel被激活时调用
  public abstract void channelActive();
  /** Invoked when an exception was caught on the Channel. */
  //当捕获一个异常时调用
  public abstract void exceptionCaught(Throwable cause);
  /** Invoked when the channel this MessageHandler is on is inactive. */
  //当channel非激活状态时调用
  public abstract void channelInactive();
}
```
##### 2.2.14.2 Message的集成体系
MessageHandler能够处理的都是Message的子类，Message中重要的方法如下：
```java
public interface Message extends Encodable {
  /** Used to identify this request type. */
  //返回消息的类型
  Type type();
  /** An optional body for the message. */
  //返回消息可选的内容体
  ManagedBuffer body();
  /** Whether to include the body of the message in the same frame as the message. */
  //用于判断消息的主题是否包含在消息的同一帧
  boolean isBodyInFrame();
}
```
Message继承自Encodable,Encodable的定义如下：
```java
public interface Encodable {
  /** Number of bytes of the encoded form of this object. */
  int encodedLength();
  /**
   * Serializes this object by writing into the given ByteBuf.
   * This method must write exactly encodedLength() bytes.
   */
  void encode(ByteBuf buf);
}
```
实现了Encodable的类可以被转换成ByteBuf，多个对象被存储到单个的、预先分配的ByteBuf中，这里的encodedLength方法返回对象编码之后的字节数
* ChunkFetchRequest：请求获取流的单个块的序列。
* RpcRequest：此消息类型由远程的RPC服务端进行处理，是一种需要服务端向客户端回复的RPC请求信息类型。
* OneWayMessage：此消息也需要由远程的RPC服务端进行处理，与RpcRequest不同的是不需要服务端向客户端回复。
* StreamRequest：此消息表示向远程的服务发起请求，以获取流式数据。
由于OneWayMessage 不需要响应，所以ResponseMessage的对于成功或失败状态的实现各有三种，分别是：
* ChunkFetchSuccess：处理ChunkFetchRequest成功后返回的消息；
* ChunkFetchFailure：处理ChunkFetchRequest失败后返回的消息；
* RpcResponse：处理RpcRequest成功后返回的消息；
* RpcFailure：处理RpcRequest失败后返回的消息；
* StreamResponse：处理StreamRequest成功后返回的消息；
* StreamFailure：处理StreamRequest失败后返回的消息；
##### 2.2.14.3 MessageBuffer的集成体系
在Message中，返回的内容体为MessageBuffer，MessageBuffer提供了由字节构成数据的不可变视图(也就是说MessageBuffer并不提供数据，也不是数据的实际来源，这同关系型数据库类似)，MessageBuffer定义的行为如下：
```java
public abstract class ManagedBuffer {
  /**
   * Number of bytes of the data. If this buffer will decrypt for all of the views into the data,
   * this is the size of the decrypted data.
   * 返回数据的字节数
   */
  public abstract long size();
  /**
   * Exposes this buffer's data as an NIO ByteBuffer. Changing the position and limit of the
   * returned ByteBuffer should not affect the content of this buffer.
   * 将数据按照nio的ByteBuffer类型返回
   */
  // TODO: Deprecate this, usage may require expensive memory mapping or allocation.
  public abstract ByteBuffer nioByteBuffer() throws IOException;
  /**
   * Exposes this buffer's data as an InputStream. The underlying implementation does not
   * necessarily check for the length of bytes read, so the caller is responsible for making sure
   * it does not go over the limit.
   * 将数据按照InputStream返回
   */
  public abstract InputStream createInputStream() throws IOException;
  /**
   * Increment the reference count by one if applicable.
   * 当有新的使用者使用此视图时，增加此视图的引用次数
   */
  public abstract ManagedBuffer retain();
  /**
   * If applicable, decrement the reference count by one and deallocates the buffer if the
   * reference count reaches zero.
   * 当有使用者不再使用此视图时，减少此视图的引用次数，当引用数为0时释放缓冲区
   */
  public abstract ManagedBuffer release();
  /**
   * Convert the buffer into an Netty object, used to write the data out. The return value is either
   * a {@link io.netty.buffer.ByteBuf} or a {@link io.netty.channel.FileRegion}.
   *
   * If this method returns a ByteBuf, then that buffer's reference count will be incremented and
   * the caller will be responsible for releasing this new reference.
   * 将缓冲区的数据转换为Netty的对象，用来将数据写到外部，此方法返回的数据类型要么是io.netty.buffer.ByteBuf，要么是io.netty.channel.FileRegion。
   */
  public abstract Object convertToNetty() throws IOException;
}
```
具体的MessageBuffer的继承关系如下：
```text
ManagedBuffer (org.apache.spark.network.buffer)
    FileSegmentManagedBuffer (org.apache.spark.network.buffer)
    NettyManagedBuffer (org.apache.spark.network.buffer)
    NioManagedBuffer (org.apache.spark.network.buffer)
    EncryptedManagedBuffer (org.apache.spark.storage)
    BlockManagerManagedBuffer (org.apache.spark.storage)
```
下面以FileSegmentManagedBuffer为例，
#### 2.2.15 TransportServerBootstrap
当客户端连接到服务端时在服务端执行一次的引导程序
#### 2.2.16 TransportServer
RPC框架的服务端，提供高效的、低级别的流服务。org.apache.spark.network.TransportContext.createServer(java.lang.String, int, java.util.List<org.apache.spark.network.server.TransportServerBootstrap>)用于创建TransportServer。  
其具体代码如下：
```java
/* Create a server which will attempt to bind to a specific host and port. */
public TransportServer createServer(
    String host, int port, List<TransportServerBootstrap> bootstraps) {
  return new TransportServer(this, host, port, rpcHandler, bootstraps);
}

/* Creates a new server, binding to any available ephemeral port. */
public TransportServer createServer(List<TransportServerBootstrap> bootstraps) {
  return createServer(0, bootstraps);
}

public TransportServer createServer() {
  return createServer(0, new ArrayList<>());
}
```
createServer方法有几个重载方法，其最终调用TransportServer的构造器创建一个实例。TransportServer的构造器代码如下：
```java
/*
 * Creates a TransportServer that binds to the given host and the given port, or to any available
 * if 0. If you don't want to bind to any special host, set "hostToBind" to null.
 * */
public TransportServer(
    TransportContext context,
    String hostToBind,
    int portToBind,
    RpcHandler appRpcHandler,
    List<TransportServerBootstrap> bootstraps) {
  this.context = context;
  this.conf = context.getConf();
  this.appRpcHandler = appRpcHandler;
  this.bootstraps = Lists.newArrayList(Preconditions.checkNotNull(bootstraps));

  boolean shouldClose = true;
  try {
    init(hostToBind, portToBind);
    shouldClose = false;
  } finally {
    if (shouldClose) {
      JavaUtils.closeQuietly(this);
    }
  }
}
```
TransportServer构造器中的各变量的含义：
* context:TransportContext的引用
* conf:TransportConf，这里通过TransportContext来获取相关配置信息
* appRpcHandler:RPC请求处理器RpcHandler
* bootstraps:参数传递的服务端引导程序
TransportServer构造器中的init方法，是对TransportServer进行初始化操作，其代码如下：
```java
private void init(String hostToBind, int portToBind) {
  IOMode ioMode = IOMode.valueOf(conf.ioMode());
  //根据Netty规范，服务端需要有两个线程组，bossGroup和workGroup
  EventLoopGroup bossGroup = NettyUtils.createEventLoop(ioMode, conf.serverThreads(), conf.getModuleName() + "-server");
  EventLoopGroup workerGroup = bossGroup;
  //创建一个汇集ByteBuf但对本地线程缓存禁用的分配器
  PooledByteBufAllocator allocator = NettyUtils.createPooledByteBufAllocator(conf.preferDirectBufs(), true /* allowCache */, conf.serverThreads());
  //创建Netty的服务端根引导程序并对其进行配置
  bootstrap = new ServerBootstrap()
    .group(bossGroup, workerGroup)
    .channel(NettyUtils.getServerChannelClass(ioMode))
    .option(ChannelOption.ALLOCATOR, allocator)
    .option(ChannelOption.SO_REUSEADDR, !SystemUtils.IS_OS_WINDOWS)
    .childOption(ChannelOption.ALLOCATOR, allocator);
  this.metrics = new NettyMemoryMetrics(allocator, conf.getModuleName() + "-server", conf);
  if (conf.backLog() > 0) {
    bootstrap.option(ChannelOption.SO_BACKLOG, conf.backLog());
  }
  if (conf.receiveBuf() > 0) {
    bootstrap.childOption(ChannelOption.SO_RCVBUF, conf.receiveBuf());
  }
  if (conf.sendBuf() > 0) {
    bootstrap.childOption(ChannelOption.SO_SNDBUF, conf.sendBuf());
  }
  //为根引导程序设置管道初始化回调函数
  bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
    @Override
    protected void initChannel(SocketChannel ch) {
      logger.debug("New connection accepted for remote address {}.", ch.remoteAddress());
      RpcHandler rpcHandler = appRpcHandler;
      for (TransportServerBootstrap bootstrap : bootstraps) {
        rpcHandler = bootstrap.doBootstrap(ch, rpcHandler);
      }
      context.initializePipeline(ch, rpcHandler);
    }
  });
  //给根引导程序绑定Socket监听端口
  InetSocketAddress address = hostToBind == null ? new InetSocketAddress(portToBind): new InetSocketAddress(hostToBind, portToBind);
  channelFuture = bootstrap.bind(address);
  channelFuture.syncUninterruptibly();
  port = ((InetSocketAddress) channelFuture.channel().localAddress()).getPort();
  logger.debug("Shuffle server started on port: {}", port);
}
```
>根据Netty的API规范，服务端需要有两组线程组bossGroup和workGroup

从以上代码可以看出TransportServer的创建过程如下：
1. 创建bossGroup和workGroup线程组
2. 创建一个ByteBuf但对本地线程缓存禁用的分配器
3. 调用Netty的API，实例化服务端的根引导器并对其进行配置
4. 为服务端设置管道初始化回调函数，此回调函数首先设置TransportServerBootstrap到根引导程序中，然后调用TransportContext的initializePipeline方法去初始化Channel的pipeline
5. 给服务端根引导程序设置监听的端口