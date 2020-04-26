# Netty专题
## Netty程序的大体流程
1. 首先声明两个事件循环组，bossGroup和workerGroup
2. bossGroup主要是接收请求，并不对请求进行处理，bossGroup接到请求后交给workerGroup去处理
3. 创建ServerBootstrap，用于后续的启动服务
4. 往ServerBootstrap中设置事件循环组、channel(管道类型)和处理器，绑定端口和关闭服务的相关设置
5. 在处理器中，我们可以在管道中添加相关处理类
## Netty能够做到的事情
1. 更为底层的http服务器开发，不同于Jetty和Tomcat这些服务器，它没有实现Servlet规范，而是自定义了一套规范
2. socket开发，RPC调度框架(开源项目应用广泛)，Spark底层的通讯组件
3. 支持http的长连接开发，客户端与服务器保持长时间的连接，在长连接下，客户端和服务端可以互传数据，一旦Netty长连接建立完毕，那么客户端和服务端之间的传输就可以是纯粹的数据而没有其他冗余的数据存在
4. 可以自定义协议
## Netty对于长连接WebSocket的支持
### 长连接
客户端与服务器端的链接一旦建立好之后，在没有外部原因的情况下，这个链接是不会断掉的，而且客户端和服务器端就变成了两个对等的实体，客户端与服务器端之间可以互发数据，从而实现服务器端向客户端push的操作。
而且只有在建立长连接的最开始是需要客户端向服务器端发送请求(包括请求头和请求体)，等长连接建立完毕之后，客户端和服务器端在这个长连接基础之上只需要发送真正的数据而不需要发送其他冗余的请求(请求头信息)，节省网络带宽。
长连接是http协议的升级协议，在建立长连接之前，还是会发送标准的http请求，只不过会在请求头中包含WebSocket的一些参数，以告知服务器端建立长连接。
### WebSocket
1. WebSocket是HTML5规范的一部分，为了解决http协议存在的一些不足。http协议是无状态的，是基于请求和响应的一种协议。
2. WebSocket本身是http协议的。
3. WebSocket也可以支持非浏览器的场合。(app和服务器端)服务器端向app推送数据。
### http协议
http协议存在的一些不足。http协议是无状态的，是基于请求和响应的一种协议。所谓的无状态就是相同的客户端向服务端发送一个请求之后再去发送另外一个请求，这两次请求是没有任何关系的，服务器是不会认为这两个请求是同一个客户端发送的；服务端无法追溯到客户端，由此产生cookie和session。<br/>
基于请求和响应模式的，一次请求的产生一定是由客户端发起的，在客户端发送请求之前，客户端与服务器端一定会建立好一个链接，所有的请求都会在此链接之上进行，服务器端收到请求之后进行相应的处理，处理完毕之后服务器就会构造出response对象进行相应。
对于http1.0的时候，一旦客户端与服务器端的请求交互结束之后，这个链接也就被关闭了；在http1.1的时候，增加了一个新的特性 `keep alive` 持续链接即客户端与服务器端保持着这样一个链接，在指定的时间范围之内，如果客户端与服务器端还会发送请求的时候，就会重用这个链接，但是超过时间没有再发送请求，那么这个链接就会被自动的关闭。
服务器端是无法主动向客户端发送消息的。
## rmi与RPC
### rmi(remote method invocation)
即远程方法调用，只针对Java，其中存在客户端与服务器端。  
客户端把需要调用的信息封装到stub中，服务器端依赖于skeleton，在客户端与服务器端之间的传输依赖于序列化与反序列化，即将Java对象编码成字节数据然后将字节数据反编码成Java对象。
### RPC(Remote Procedure Call)
即远程过程调用，RPC支持跨语言调用。
#### RPC编写模式
1. 定义一个接口说明文件(idl)：描述了对象(结构体)，对象成员，接口方法等一些列信息  
2. 通过RPC框架所提供的编译器，将接口说明文件编译成具体语言文件  
3. 在客户端与服务器端分别引入RPC编译器所生成的文件，即可像调用本地方法一样去调用远程方法  
## Netty中的回调方法解析
1. handlerAdded：一个处理器被添加  
2. handlerRemoved：一个处理器被移除  
3. channelRegistered：channel被注册，channel被绑定到线程(EventLoopGroup)上
4. channelActive：channel准备就绪
5. channelRead：channel有数据可读
6. channelReadComplete：channel读取数据完毕
7. channelInactive：channel被关闭
8. channelUnregistered：channel取消线程(NioEventLoop)绑定 
### 客户端连接服务器端时 
当一个客户端连接服务器端的时候，执行方法的顺序如下：handlerAdded() -> channelRegistered() -> channelActive() -> channelRead() -> channelReadComplete()  
1. handlerAdded() ：指的是当检测到新连接之后，调用 ch.pipeline().addLast(new LifeCyCleTestHandler()); 之后的回调，表示在当前的 channel 中，已经成功添加了一个 handler 处理器。  
2. channelRegistered()：这个回调方法，表示当前的 channel 的所有的逻辑处理已经和某个 NIO 线程建立了绑定关系，类似accept 到新的连接，然后创建一个线程来处理这条连接的读写，只不过 Netty 里面是使用了线程池的方式，只需要从线程池里面去抓一个线程绑定在这个 channel 上即可，这里的 NIO 线程通常指的是 NioEventLoop  
3. channelActive()：当 channel 的所有的业务逻辑链准备完毕（也就是说 channel 的 pipeline 中已经添加完所有的 handler）以及绑定好一个 NIO 线程之后，这条连接算是真正激活了，接下来就会回调到此方法。 
4. channelRead()：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读。  
5. channelReadComplete()：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕。  
### 客户端断开与服务器端的连接时
当我们把客户端关闭的时候，对于服务器端来说就是channel被关闭了channelInactive() -> channelUnregistered() -> handlerRemoved()
1. channelInactive(): 表面这条连接已经被关闭了，这条连接在 TCP 层面已经不再是 ESTABLISH 状态了  
2. channelUnregistered(): 既然连接已经被关闭，那么与这条连接绑定的线程就不需要对这条连接负责了，这个回调就表明与这条连接对应的 NIO 线程移除掉对这条连接的处理  
3. handlerRemoved()：最后，我们给这条连接上添加的所有的业务逻辑处理器都给移除掉。  
## Netty与Google Protocol Buffer
我们可以使用 `protoc --java_out=C:/Users/zhengwei/Desktop/asiainfo_work/playground/src/main/java student.proto` 命令生成Java对象文件
### Netty与Google Protocol Buffer
1. 首先需要定义 `.proto` 文件，这就是接口定义文件呢，文件中包含消息体
2. 使用 `protoc` 命令将 `.proto` 文件编译成Java代码
3. 在Netty的客户端与服务端进行引用
#### 最佳实践
使用Git作为版本控制系统：  
1. git submodule：git仓库里面的一个仓库
2. git subtree：
### Thrift
#### 简介
主要服务于各个服务之间的RPC通信，支持跨语言，如Java,C++,Python,C#登等。  
Thrift是一个典型的CS架构，客户端与服务端可以使用不同的语言进行开发，那么客户端与服务端之间的存在一种中间语言即IDL(Interface Description Language)。
Thrift不支持无符号类型，因为很多语言不存在无符号类型  
#### 支持的类型
1. byte：有符号字节
2. i16：16位有符号整数->short
3. i32：32位有符号整数->int
4. i64：64位有符号整数->long
5. double：64位浮点数
6. string：字符串
#### 支持的组件
1. struct->编译完之后就是类class，目的就是将一些数据结构聚合到一起，方便传输管理。其可以定义成如下形式：
```thrift
struct People {
    1: string name;
    2: i32 age;
    3: string gender;
}
```
2. service->客户端与服务端所用到的接口，若干个方法的集合，相当于Java中的Interface一样，创建service经过代码生成命令之后就会生成客户端与服务端的框架代码，其可以定义成如下形式：
```thrift
service HelloWorldService {
    //service中定义的函数，相当于Java中Interface中定义的方法
    string doAction(1: string name, 2: i32 age);
}
```
3. exception->客户端与服务端交互式可能会抛出的异常，Thrift支持自定义的异常，规则和struct一致，其可以定义成如下形式：
```thrift
exception MyException {
    1: i32 code;
    2: string reason;
}
```
4. enum->枚举，定义的形式和Java类似，其可以定义成如下形式：
```thrift
enum Gender {
    MALE,
    FEMALE
}
```
5. 类型定义->Thrift支持C++一样的typeof定义，其可以定义成如下形式：  
```thrift
typeof i32 int
typeof i64 long
```
6. 常量->Thrift也支持常量定义，使用const关键字，其可以定义成如下形式：
```thrift
const i32 MAX_RETRIS_TIME=10
const string MY_WEBSITE="www.awei.com"
```
7. 命名空间->Java中的package，主要是用来组织代码的，使用namespace关键字来定义命名空间，其可以定义成如下形式：
```thrift
namespace java com.zhengwei.netty.thrift.demo
namespace 语言名 路径
```
8. 文件包含->Thrift也支持命名包含，相当于C/C++中的include，Java中的import，使用关键字include定义，其可以定义成如下形式：
```thrift
include "global.thrift"
```
9. 注释->支持#、//和/**/形式的注释
10. 可选与必选->Thrift提供两个关键字required，optional，分别表示字段必选和可选，其可以定义成如下形式：
```thrift
struct People {
    1: required string name;
    2: optional i32 age;
}
```
#### 容器类型
1. list：一系列由T类型数据组成的有序列表，元素可以重复
2. set：一些列由T类型数据组成的无序集合，元素不可重复
3. map：一个字典结构，key为K类型，value为V类型，相当于Java中的Map结构
#### 如何实现多语言通信
数据传输使用Socket，数据以特定格式发送，接收方语言进行解析。  
定义thrift文件，由thrift文件(IDL)生成双方语言的接口、model，在生成的model以及接口中会有解码和解码的代码。  
#### 生成代码
首先需要编写定义thrift接口的描述文件。
#### Thrift的传输格式-protocol
* TBinaryProtocol:二进制传输
* TCompactProtocol：压缩格式传输
* TJSONProtocol：json格式传输
* TSimpleJSONProtocol：提供JSON只写协议，生成的文件很容易通过脚本语言解析
* TDebugProtocol：使用易懂的可读的文本格式，以便于debug
#### 数据传输方法-transport
* TSocket：阻塞式socket
* TFramedTransport：以frame为单位进行传输，非阻塞式服务中使用
* TFileTransport：以文件形式传输
* TMemoryTransport：将内存用于I/O，Java实现时内部实际采用了简单的ByteArrayOutputStream
* TZlibTransport：使用zlib压缩，以其他传输方式联合使用。当前无Java实现
#### Thrift支持的服务模型-server
* TSimpleServer：简单的单线程服务模型，常用于测试
* TThreadPoolServer：多线程服务模型，使用标准的阻塞式I/O
* TNonblockingServer：所线程服务模型。使用非阻塞式I/O(需要使用TFramedTransport数据传输方式)
* THsHaServer：THsHaServer引入了线程池去处理，其模型把读写任务放到线程池中去处理；Half-Sync/Half-Async的处理模式，Half-Sync是在处理IO事件上(accept/read/write IO)，Half-Async用于handler对RPC的同步处理
TCompactProtocol-TFramedTransport-THsHaServer
### Google Protocol Buffer和Thrift的一些区别
1. protobuf只是作为一种协议而存在，只承载消息即message，我们可以使用诸如Netty或者http的方式去传输(transport)这个消息体
2. thrift相比较protobuf则更进一步，thrift不仅可以承载消息，也可以定义客户端与服务端之间的一些方法。
## Java中的IO
### 流
#### 1. 流的概念
Java程序通过流来完成输入和输出。流是生成或消费信息的抽象。流通过Java的输入/输出系统物理设备连接。尽管与它们连接的物理设备不同，所有流的行为都具有同样的方式。这样相同的输入/输出类与方法适用于所有类型的外设。这意味着一个输入流能够抽象多种不同类型的输入：从磁盘文件、从键盘或网络套接字。同样，一个输出流可以输出到控制台、磁盘文件或相连的网络。流是处理输入/输出的洁净方法，例如它不需要代码理解键盘和网络的不同。Java中流的实现在java.io包定义的类层次结构内部。
#### 2. 输入/输出流的概念
* 输入/输出时，数据在数据通道中流动。所谓数据流(stream)指的是所有数据在通信通道中，即数据的起点和终点。信息的通道就是一个数据流。只要是数据从一个地方流到另一个地方，这种流动的通道都可以称为数据流。
* 输入和输出是相对于程序来说的。程序在使用数据时所扮演的角色有两个：一个是源，一个是目的。若程序是输入流的源，即数据的提供者，这个数据流对于程序来说就是一个"输出数据流"(数据从程序流出)，若程序是数据流的终点，这个数据流对程序而言就是一个"输入数据流"(数据从程序外流向程序)。
#### 3. 输入/输出类
1. Java提供丰富的类供我们使用
2. 主要分为两大类：输入流和输出流
3. 从流的结构上分为字节流(以字节为处理单位或面向字节)和字符流(以字符为处理单位或称为面向字符)
4. 字节流的输入流和输出流基础是InputStream和OutputStream这两个抽象类，字节流的输入输出操作由两个类的子类实现。字符流是Java1.1之后推出的以字符为单位的进行输入和输出的处理的流，字符流输入输出的基础是Reader和Writer这两个抽象类。
#### 字节流和字符流
Java2定义类两种类型的流：字节流和字符流。字节流(byte stream)为处理字节的输入和输出提供了方便的方法。例如使用字节流来读取或写入二进制数据。字符流(character stream)为字符流的输入和输出提供了方便。它们采用了统一的编码标准，因而可以国际化。当然在某些场合，字符流比字节流更有效。  
注意：在最底层，所有的输入和输出都是以字节的形式进行的。基于字符的流只为处理字符提供方便有效的方法。
#### 输入流
读数据的逻辑：open stream->while more info->read info->close stream
#### 输出流
写数据的逻辑：open stream->while more info->write info->close stream
#### 流的分类
流处理可以分为输入流和输出流，还可以从流的形式分类
1. 节点流：从特定的地方读写的流类，例如：磁盘或一块内存区域。FileInputStream
2. 过滤流：使用节点流作为输入或输出。过滤流是使用一个已经存在的输入流或输出流连接创建的。BufferedInputStream
#### InputStream
1. 在InputStream中，除了FilterInputStream之外都是节点流，而FilterInputStream和FilterInputStream的子类都是过滤流
2. InputStream中包含一套字节输入流需要的方法，可以完成最基本的从输入流读入数据的功能。当Java程序需要外设的数据时，可以根据数据的不同形式，创建一个适当的InputStream的子类类型的实例完成与外设的连接，然后再调用执行这个InputStream实例的特定输入方法来实现对应外设的输入操作。
3. InputStream的子类常用的方法有：读取数据的read()方法，获取输入流字节数的available()方法，定位输入位置指针的skip(),reset(),mark()方法等。
#### OutputStream
##### 三个基本的写方法：
1. abstract void write(int b)：往输出流中写数据
2. void write(byte[] b)：往输出流中写入数据b的数据
3. void write(byte[] b,int off,int len)：往输出流中写入数组b中从偏移量off开始的len长度的字节的数据
##### 其他方法：
1. void flush()：刷新输出流，强制写出缓冲区中的数据
2. void close()：关闭输出流，释放和这个输出流的相关的资源
OutputStream时定义了流式字节输出模式的抽象类，该类的所有方法都是void类型的，并都会抛出IOException
FilterOutputStream是一个过滤流，其子类也都是过滤流，需要依附在节点流使用
#### IO流的连接
##### Input Stream Chain
FileInputStream->BufferedInputStream->DataInputStream  
new DataInputStream(new BufferedInputStream(new FileInputStream()))
##### Output Stream Chain
DataOutputStream->BufferedOutputStream->FileOutputStream  
new DataOutpurStream(new BufferedOutputStream(new FileOutputStream()))
#### Java中IO库的设计原则
1. Java的IO库提供了一种称作连接的机制，可以将一个流与另一个首尾相接，形成一个流管道的链接，这种机制实际上被称作Decorator(装饰)设计模式的应用。
2. 通过流的链接，可以动态的增加流的功能，而这种功能的增加是通过组合一些流的基本功能而动态获取的。
3. 我们要获取一个IO对象，往往好产生多个IO对象，这也是JavaIO库不太好掌握的原因，但是IO库在Decorator模式的运用，给我们提供了实现上的灵活性。
## Java中的NIO
### Java的IO与NIO对比
java.io中最核心的一个概念就是流(stream)，面向流的编程，一个流要么是输入流要么就是输出流，不能既是输入流又是输出流。  
java.nio中有三个核心的概念:selector,channel,buffer，在java.nio中是面向块(block)或是缓冲区(buffer)来编程的。
### buffer
buffer本身就是一块内存，底层实现是用数组实现，数据的读写都是用buffer来实现的，一个buffer既可以读数据也可以写数据，一个程序想要从读取数据，**必须**先从channel中把数据读到buffer中，然后再从buffer中读取数据；那写数据同理。  
除了数组之外，buffer还提供了对于数据的结构化访问方式，并且可以追踪到系统的读写过程。  
Java中的额7种数据类型都有对应的buffer类型，入IntBuffer,LongBuffer,ByteBuffer,CharBuffer...除了boolean没有buffer类型。
### channel
channel指的是可以向其写入数据或从中读取数据的对象，它类似于java.io中的stream  
所有数据的读写都是通过buffer来进行的，永远不会出现直接向channel写入数据的情况，或是直接从channel读取数据的情况  
与stream不同的是，channel是双向的，一个stream只能是InputStream或是OutputStream；channel打开之后可以读写操作，由于channel是双向的，因此更能反映出底层操作系统的真实情况，在Linux系统中，底层操作系统的通道就是双向的
### 三个状态属性
#### capacity
A buffer's capacity is the number of elements it contains. Thecapacity of a buffer is never negative and never changes.  
一个buffer的capacity是这个buffer的最大容量，且capacity永远不会小于0，且不会改变
#### limit
A buffer's limit is the index of the first element that should not be read or written. A buffer's limit is never negative and is never greater than its capacity.
一个buffer中，要读或要写的最后一个元素的下一个元素的索引，limit永远不可能为负数和永远不糊大于capacity。  
比如上一次写入buffer的最后一个元素是5，那么它的下一个索引位就是6，所以limit就是指向6这个位置。
#### position
A buffer's position is the index of the next element to be read or written.  A buffer's position is never negative and is never greater than its limit.
一个buffer的position指的是下一个将要读或写的索引位置。position不可能为负数，并且不会大于limit
比如现在buffer中的最后一个元素的索引是5，那么如果还有数据需要读的话，那么接下来的元素索引就是6，那么position所指向的索引位置就是6.
### 从nio中读取文件的步骤
1. 从FileInputStream中获取FileChannel对象
2. 创建Buffer
3. 将数据冲Channel中读入到Buffer中
### 绝对方法与相对方法
#### 相对方法
limit与position的值会在操作时被考虑到，程序会去检查其值是否符合相应规范
#### 绝对方法
完全忽略limit与position，直接根据Buffer的索引去获取数据
##零拷贝
### 什么是零拷贝？
对于零拷贝，不是字面意义上的意思，它是针对于用户空间和内核空间之间的拷贝次数，零拷贝指的是用户空间与内核空间的拷贝的次数是0次，而不是不拷贝数据。
数据总归是要从磁盘拷贝到内核空间中的，至于要不要拷贝到用户空间取决于调用的是什么系统方法。
### 什么时候可以使用零拷贝？
当我们的需求只是将一个文件不做任何修改的发送给另一个系统时，我们就可以使用零拷贝；
但是如果我们需要对文件进行修改的话，那就不能使用零拷贝，还是要将文件从内核空间拷贝到用户空间，修改完毕之后再从用户空间拷贝到内核空间，再发送出去。
### 零拷贝调用的系统调用

## Netty
### 组件
#### EventLoopGroup
事件循环组，最终继承自 `java.util.concurrent.ExecutorService`，并对其进行扩展，内部包含若干个EventLoop类似于EventLoop数组(本质是EventExecutor数组)，其声明如下，并提供了获取内部的EventLoop对象的方法、注册Channel的方法，
一般服务器端有两个EventLoopGroup，分别是bossGroup和workerGroup，bossGroup通常只接受连接，然后分配给workerGroup去处理，这其中涉及到Reactor模式。最常用的一个实现类是 `NioEventLoopGroup`
#### EventLoop
一个比较虚的组件，在构造NioEventLoopGroup时，其本质时EventLoop数组，并在构造方法中被初始化。
#### NioEventLoopGroup
Nio的事件循环组，接受一个 `int` 类型的参数，此参数的作用是在启动事件循环组时启动多少个线程，默认是CPU个数的两倍。
这个类主要是初始化一些Netty的一些参数，最终调用的是 `protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args)` ，即初始化了启动的线程个数、
线程池、事件执行器选择工厂和
#### Future
Netty中的Future继承自jdk中的Future，Netty在jdk的基础上进行了扩展。jdk中的Future支持对线程完全异步操作，不会阻塞住，会立即返回，需要自己手动调用 `V get()` 或 `V get(long timeout, TimeUnit unit)` 进行获取最后的结果，
当最终结果还没有计算出来时，调用get方法还是会阻塞的；如果结果已经被计算出来，那么调用get时会立即返回结果，不会立即返回；而jdk的Future的最大的一个问题就是get方法应该在何时调用，Netty的Future解决了这个通电。
而Netty的Future的接口对jdk的Future进行了扩展，可以准确的获取到任务的执行的状态，和添加了相应的回调方法，类似于观察者模式。
#### ChannelFuture
* 继承自Netty的Future<Void>接口
* ChannelFuture中承载了异步的Future
* 官方建议不要去调用 `await` 方法，有可能造成死锁情况；建议调用 `addListener(GenericFutureListener)` 方法，此方法有回调机制，当一个I/O操作完成时`addListener(GenericFutureListener)`会通知，而且不会阻塞。
```text
                                     +---------------------------+
                                     | Completed successfully    |
                                     +---------------------------+
                                +---->      isDone() = true      |
+--------------------------+    |    |   isSuccess() = true      |
|        Uncompleted       |    |    +===========================+
+--------------------------+    |    | Completed with failure    |
|      isDone() = false    |    |    +---------------------------+
|   isSuccess() = false    |----+---->      isDone() = true      |
| isCancelled() = false    |    |    |       cause() = non-null  |
|       cause() = null     |    |    +===========================+
+--------------------------+    |    | Completed by cancellation |
                                |    +---------------------------+
                                +---->      isDone() = true      |
                                     | isCancelled() = true      |
                                     +---------------------------+
```
#### ChannelFactory
顾名思义，是创建Channel的工厂类，其主要功能就是创建Channel，通常是通过反射创建Channel
#### ReflectiveChannelFactory
专门反射创建Channel，提供的class对象需要提供无参构造器，最常用的是NioServerSocketChannel.class这个对象
#### Channel

#### ChannelPipeline
它是一个增强版的拦截过滤器，每个ChannelHandler都和一个Channel关联，每当一个Channel创建的时候一个与之关联的ChannelPipeline就会自动创建。
它由一系列ChannelHandler组成，每个ChannelHandler处理完数据之后，都会将事件广播给下一个ChannelHandler，pipeline中由两个方向，一个是inbound和outbound
```text
                                                I/O Request
                                           via {@link Channel} or
                                       {@link ChannelHandlerContext}
                                                     |
 +---------------------------------------------------+---------------+
 |                           ChannelPipeline         |               |
 |                                                  \|/              |
 |    +---------------------+            +-----------+----------+    |
 |    | Inbound Handler  N  |            | Outbound Handler  1  |    |
 |    +----------+----------+            +-----------+----------+    |
 |              /|\                                  |               |
 |               |                                  \|/              |
 |    +----------+----------+            +-----------+----------+    |
 |    | Inbound Handler N-1 |            | Outbound Handler  2  |    |
 |    +----------+----------+            +-----------+----------+    |
 |              /|\                                  .               |
 |               .                                   .               |
 | ChannelHandlerContext.fireIN_EVT() ChannelHandlerContext.OUT_EVT()|
 |        [ method call]                       [method call]         |
 |               .                                   .               |
 |               .                                  \|/              |
 |    +----------+----------+            +-----------+----------+    |
 |    | Inbound Handler  2  |            | Outbound Handler M-1 |    |
 |    +----------+----------+            +-----------+----------+    |
 |              /|\                                  |               |
 |               |                                  \|/              |
 |    +----------+----------+            +-----------+----------+    |
 |    | Inbound Handler  1  |            | Outbound Handler  M  |    |
 |    +----------+----------+            +-----------+----------+    |
 |              /|\                                  |               |
 +---------------+-----------------------------------+---------------+
                 |                                  \|/
 +---------------+-----------------------------------+---------------+
 |               |                                   |               |
 |       [ Socket.read() ]                    [ Socket.write() ]     |
 |                                                                   |
 |  Netty Internal I/O Threads (Transport Implementation)            |
 +-------------------------------------------------------------------+
```
##### inbound
inbound的方向是从系统外部进入到系统内部的，即 `SocketChannel#read(ByteBuffer)` 读入客户端的数据进入系统内部，经过每一层ChannelHandler处理，最终到最后一个ChannelHandler处理完毕。  
那么哪些处理器是inbound方向上的处理器呢？  
即是那些实现了ChannelInboundHandler的处理器类是会被Netty认为是inbound上的处理器，在事件从外部进入时，事件就会被这些Handler处理。
##### outbound
outbound的方向是从系统内部出去到系统外部去，即 `SocketChannel#write(ByteBuffer)` ，与inbound的方向完全相反，最终处理完的数据输出给客户端。  
那么哪些处理器时outbound方向上的处理器呢？  
即是那些实现了ChannelOutboundHandler的处理器类是会被Netty认为是outbound上的处理器，在事件从内部出去到外部时，事件就会被这些Handler处理。
##### Netty对于inbound和outbound方向上handler的选择
例如有如下代码：
```java
ChannelPipeline p = ...
p.addLast("1", new InboundHandlerA());
p.addLast("2", new InboundHandlerB());
p.addLast("3", new OutboundHandlerA());
p.addLast("4", new OutboundHandlerB());
p.addLast("5", new InboundOutboundHandlerX());
```
以Inbound打头的类代表是inbound方向的处理器，以Outbound打头的类代表是outbound方向的处理器。  
例子里面给了1,2,3,4,5这五个处理器，inbound方向的处理顺序就是1->2->3->4->5，那outbound的方向的处理顺序就是5->4->3->2->1，但是原则上，ChannelPipeline会跳过一些处理器，那是因为：
* 3和4没有实现 `ChannelInboundHandler` 所以在inbound方向上最终的处理顺序是1->2->5
* 1和2没有实现 `ChannelOutboundHandler` 所以在outbound方向上最终的处理顺序是5->4->3
* 而5即实现了 `ChannelInboundHandler` 又实现了 `ChannelOutboundHandler` 所以不论是inbound还是outbound都会经过处理器5的处，即1->2->5和5->4->3
#### NioServerSocketChannel
* 在NioServerSocketChannel中，在获取ServerSocketChannel时没有使用 `java.nio.channels.spi.SelectorProvider#provider` 方法，而是使用了 `java.nio.channels.spi.SelectorProvider.openServerSocketChannel` 方法，
原因是因为 `SelectorProvider#provider` 中有同步代码块，会造成性能下降，每增加5000个连接性能就会下降1%；而 `SelectorProvider.openServerSocketChannel` 没有同步代码块，每次直接生成一个新的ServerSocketChannel对象，但是会消耗内存空间
* 在NioServerSocketChannel的构造方法中，首先确定了对于 `OP_ACCEPT` 事件感兴趣，以确保连接可以正常的接入。
```java
public NioServerSocketChannel(ServerSocketChannel channel) {
        super(null, channel, SelectionKey.OP_ACCEPT);
        config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}
```
#### ServerBootstrap
* 服务端启动引导类，初始化一系列参数，为启动信息做一些封装，为服务器启动做准备
##### 
##### group
这个方法有两个重载方法，一个是接受一个EventLoopGroup参数还有一个是接受两个EventLoopGroup。
###### group(EventLoopGroup)
通常在客户端使用，仅仅是启动线程往服务端发送请求
###### group(EventLoopGroup, EventLoopGroup)
通常在服务端使用，一个是bossGroup和workGroup，bossGroup只是接受客户端发送过来的请求，接受到请求之后，交由workGroup去处理具体的业务.
##### channel(Class<? extends C> channelClass)
```java
public B channel(Class<? extends C> channelClass) {
    if (channelClass == null) {
        throw new NullPointerException("channelClass");
    }
    return channelFactory(new ReflectiveChannelFactory<C>(channelClass));
}
```
指定Channel的一个class对象，将会在后续利用反射创建具体的Channel对象，具体的实现类是ReflectiveChannelFactory，因为其中调用的是 `clazz.getConstructor().newInstance();` 方法生成实例对象，需要确保传入的class对象有一个无参构造器，
否则会无法生成实例，被实例化的对象用于以后创建Channel的工厂类。
##### bind(int)
这个方法实际去启动服务器，最终调用的是 `private ChannelFuture doBind(final SocketAddress localAddress)` 代码如下：
```java
private ChannelFuture doBind(final SocketAddress localAddress) {
    //
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }
    if (regFuture.isDone()) {
        // At this point we know that the registration was complete and successful.
        ChannelPromise promise = channel.newPromise();
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else {
        // Registration future is almost always fulfilled already, but just in case it's not.
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    // Registration on the EventLoop failed so fail the ChannelPromise directly to not cause an
                    // IllegalStateException once we try to access the EventLoop of the Channel.
                    promise.setFailure(cause);
                } else {
                    // Registration was successful, so set the correct executor to use.
                    // See https://github.com/netty/netty/issues/2586
                    promise.registered();

                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```
### Reactor模式
Reactor模式可以分为5个组成部分，如下图所示  
![reactor_model](https://github.com/zw030301/playground/blob/master/netty/src/main/resources/image/reactor_model.png)
#### Handle(句柄或文件描述符)
本质上表示一种资源即文件描述符，由操作系统提供，该资源表示一个一个事件，比如Linux中的文件描述符或每一个Socket连接都会产生一个文件描述符。事件既可以来自外部也可以来自内部；
外部事件有客户端连接请求或客户端发送的数据等；内部事件有操作系统定时器事件等，Handle是事件的发源地。
#### Synchronous Event Demultiplexer(同步事件多路复用器)
本身是系统调用，用于等待事件的发送(事件可能是一个也可能是多个)。调用方会在调用它的时候被阻塞，直到事件分离器上产生事件为止。
对于Linux来说，同步事件多路复用器指的是I/O的多路复用，比如说select,poll,epoll等系统调用。
在Java NIO的领域中，同步事件多路复用器对用的就是Selector，对应的方法就是select()方法。
#### Event Handler(事件处理器)
由多个回调方法组成，这些回调方法构成了与应用相关的某个特定的事件的反馈机制。
Java NIO中不存在Event Handler，但是Netty弥补了这一个缺陷，Netty中提供了大量的回调方法(比如SimpleChannelInboundHandler中提供的方法)，供我们在特定事件产生时实现响应的回调方法来处理业务逻辑。
### Concrete Event Handler(具体事件处理器)
是事件处理器的实现。本身实现了事件处理器的各个回调方法，从而实现了特定业务的逻辑。本质上是我们编写的一个个的Handler
### Initiation Dispatcher(初始分发器)
实际上是Reactor角色。本身定义了一些规范，这些规范用于控制事件的调度方式，同时提供了应用进行事件的注册、删除等操作。Initiation Dispatcher会通过Synchronous Event Demultiplexer来等待事件的发生(即select方法得到返回)。
一但有事件发生，Initiation Dispatcher会分离出每一个事件(即遍历select中得到的每一个事件)，然后调用事件处理器，最后调用回调方法来处理这些事件
### Reactor模式的流程
1. 当应用想Initiation Dispatcher注册具体的Event Handler时，应用会标识出该Event Handler希望Initiation Dispatcher
在未来的某个事件发生时向Event Handler发送通知，事件与Handler是相关联的。对应于Netty中是将自己写的Handler加入到pipeline中。
2. Initiation Dispatcher会要求每个Event Handler向Initiation Dispatcher传递内部的Handle，该Handle向OS标识Event Handler
3. 当所有事件处理完毕之后，应用会调用Initiation Dispatcher内部的 `handle_events` 方法来启动Initiation Dispatcher的事件循环
(对应到Netty中的就是启动bossGroup和workerGroup)。这时，Initiation Dispatcher会将所有的Event Handler的Handle合并起来(放到集合中)，
并使用Synchronous Event Demultiplexer等待这些事件的发生。比如说：TCP协议层会使用select来等待客户端发送过来的数据到达socket handle上。
4. 当某个事件的Handle变为ready时(TCP的socket变为等待读状态)，Synchronous Event Demulitiplexer会通知Initiation Dispatcher
5. Initiation Dispatcher会触发Event Handler的回调方法，从而响应这个处于ready状态的Handle。当事件发生时，Initiation Dispatcher
会将Handle作为key来寻找和分发到恰当的Event Handler上的回调方法去处理
6. Initation Dispatcher会回调Event Handler中的 `handle_event(type)` 回调方法来执行特定于业务的代码(开发者自行编写的代码),
从而响应这个Handle。所发生的Handle类型可以作为 `handle_event(type)` 参数并被该方法内部使用来执行额外的特定与服务的分离与分发
