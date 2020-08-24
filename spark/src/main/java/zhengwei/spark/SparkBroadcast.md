# SparkBroadcast
Broadcast就是将数据从一个节点发送到其他节点上去。
## Spark中的Broadcast为什么是只读的？
这涉及到各节点之间的数据一直性问题，如果Broadcast在一个节点上被更新了，那其他节点的数据是否也要一起更新？如果每个节点都去更新，
那么更新的顺序是什么？如果一台节点更新失败怎么办？等诸多问题，
Spark在考虑到一致性问题，目前Spark只支持只读Broadcast。
## 为什么Broadcast只到节点而不是到Task？
因为每个task都是一个thread，每个task都是运行在Executor中的，Executor中的内存主要由Storage和Execution组成，
只要将Broadcast数据存储到Storage内存中就能被所有task访问到。
## 怎么使用Broadcast？
```scala
val data = List(1, 2, 3, 4, 5, 6)
val bdata = sc.broadcast(data)

val rdd = sc.parallelize(1 to 6, 2)
val observedSizes = rdd.map(_ => bdata.value.size)
```
**说明：**  
1. driver使用sc.broadcast()声明要broadcast的数据，
## 使用Broadcast的注意事项
1. 只能在算子中获取广播变量的值
2. 广播变量是只读变量，不要对其进行修改
3. 不要去broadcast一个RDD，因为广播RDD的作用
## Broadcast的实现方式
### 分发task的时候先分发broadcast data的元信息
1. Driver在本地创建一个文件夹，这个文件夹用来存放broadcast的data，并启动一个可以访问该文件夹的HttpServer
2. 当调用 `val bdata = sc.broadcast(data)` 时将data写入文件夹，同时写入自己的blockManager(StorageLevel为MEMORY_AND_DISK)，
获得一个blockId(BroadcastBlockId)
3. 当调用 `rdd.transformation(func)` 时，如果func用到了bdata，那么driver在submitTask()时，会将data一同func序列化得到serialized task，
**注意：在序列化时不会序列化bdata中的数据**
>driver为什么将data既放到磁盘中又放到内存中？放到磁盘中是为了方便HttpServer访问，放到blockManager中是为了driver自身使用方便
### 什么时候传递真正的data？
在Executor反序列化task的时候，同时也会反序列话task中的bdata对象，然后会调用readObject()方法。
该方法会去本地的blockManager那里查看data是否存在，如果不存在就使用以下两种方式去fetch广播变量的值。
得到data之后，会将其存入到本地的blockManager中，以便后面的操作只用。
fetch data有如下几种方式：
#### HttpBroadcast(2.4.4中已经没有了)
每个Executor通过Http协议来连接driver并从driver中fetch data。  
1. Driver先准备好broadcast data
2. 调用 `sc.broadcast(data)` 后会调用工厂方法，建立一个HttpBroadcast对象，该对象做的第一件事就是将data存到driver的blockManager里面，
StorageLevel为内存＋磁盘，blockId类型为BroadcastBlockId。
3. 同时driver也会将broadcast的data写到本地磁盘，例如写入后得到/var/.../broadcast_0，这个文件夹作为HttpServer的文件目录。
>Driver 和 executor 启动的时候，都会生成 broadcastManager 对象，调用 HttpBroadcast.initialize()，
>driver 会在本地建立一个临时目录用来存放 broadcast 的 data，并启动可以访问该目录的 httpServer。

**Fetch data**：在 executor 反序列化 task 的时候，会同时反序列化 task 中的 bdata 对象，这时候会调用 bdata 的 readObject() 方法。
该方法先去本地 blockManager 那里询问 bdata 的 data 在不在 blockManager 里面，如果不在就使用 http 协议连接 driver 上的 httpServer，
将 data fetch 过来。得到 data 后，将其存放到 blockManager 里面，这样后面运行的 task 如果需要 bdata 就不需要再去 fetch data 了。
如果在，就直接拿来用了。  
HttpBroadcast 最大的问题就是 **driver 所在的节点可能会出现网络拥堵**，因为 worker 上的 executor 都会去 driver 那里 fetch 数据。
#### TorrentBroadcast
为了解决Http的网络瓶颈问题，Spark 又设计了一种 broadcast 的方法称为 TorrentBroadcast，**这个类似于大家常用的 BitTorrent 技术**。
##### 基本思想
将 data 分块成 data blocks，然后假设有 executor fetch 到了一些 data blocks，那么这个 executor 就可以被当作 data server 了，
随着 fetch 的 executor 越来越多，有更多的 data server 加入，data 就很快能传播到全部的 executor 那里去了。TorrentBroadcast使用
blockManager.getRemote()=> NIO ConnectionManager 传数据的方法来传递,读取数据的过程与读取 cached rdd 的方式类似
##### 细节
###### Driver
1. Driver 先把 data 序列化到 byteArray，然后切割成 BLOCK_SIZE（由 `spark.broadcast.blockSize` = 4MB 设置）大小的 data block，
每个 data block 被 TorrentBlock 对象持有。
2. 切割完 byteArray 后，会将其回收，因此内存消耗虽然可以达到 2 * Size(data)，但这是暂时的。
3. 完成分块切割后，就将分块信息（称为 meta 信息）存放到 driver 自己的 blockManager 里面，StorageLevel 为内存＋磁盘，
同时会通知 driver 自己的 blockManagerMaster 说 meta 信息已经存放好。
4. **通知 blockManagerMaster 这一步很重要，因为 blockManagerMaster 可以被 driver 和所有 executor 访问到，
信息被存放到 blockManagerMaster 就变成了全局信息。**
5. 之后将每个分块 data block 存放到 driver 的 blockManager 里面，StorageLevel 为内存＋磁盘。
存放后仍然通知 blockManagerMaster 说 blocks 已经存放好。到这一步，driver 的任务已经完成。
###### Executor
1. executor 收到 serialized task 后，先反序列化 task，这时候会反序列化 serialized task 中包含的 bdata 类型是 TorrentBroadcast，
也就是去调用 TorrentBroadcast.readObject()。
2. 这个方法首先得到 bdata 对象，然后发现 bdata 里面没有包含实际的 data。怎么办？
    1. 先询问所在的executor里的blockManager 是会否包含data(通过查询data的broadcastId)，包含就直接从本地 blockManager读取data。
    2. 否则，就通过本地blockManager去连接driver的blockManagerMaster获取data分块的meta信息，获取信息后，就开始了BT过程。

**BT过程：**
1. task 先在本地开一个数组用于存放将要 fetch 过来的 data blocks `arrayOfBlocks = new Array[TorrentBlock](totalBlocks)`，
TorrentBlock 是对 data block 的包装
2. 然后打乱要 fetch 的 data blocks 的顺序，比如如果 data block 共有 5 个，那么打乱后的 fetch 顺序可能是 3->1->2->4->5。
>至于为什么要打乱顺序，因为根据Torrent的特性，加入到server的Executor的数量越多，那么传输数据的速度越快，每个Executor上打乱顺序，
>每个Executor下载的块就会不一样，这样每当下好一个数据块之后，Executor就会加入到server中，加快数据的传输速度。
3. 然后按照打乱后的顺序去 fetch 一个个 data block。
4. fetch 的过程就是通过 
“本地 blockManager->本地 connectionManager->driver/executor 的 connectionManager->driver/executor 的 blockManager->data” 
得到 data，这个过程与 fetch cached rdd 类似。
5. **每fetch到一个block就将其存放到executor的blockManager里面，同时通知driver上的blockManagerMaster说该data block多了一个存储地址。**
6. 这一步通知非常重要，意味着 blockManagerMaster 知道 data block 现在在 cluster 中有多份，
下一个不同节点上的 task 再去 fetch 这个 data block 的时候，可以有两个选择了，而且会随机选择一个去 fetch。
7. 这个过程持续下去就是 BT 协议，随着下载的客户端越来越多，data block 服务器也越来越多，就变成 p2p下载了。
8. 整个 fetch 过程结束后，task 会开一个大 `Array[Byte]`，大小为 data 的总大小，然后将 data block 都 copy 到这个 Array，
然后对 Array 中 bytes 进行反序列化得到原始的 data，这个过程就是 driver 序列化 data 的反过程。
9. 最后将 data 存放到 task 所在 executor 的 blockManager 里面，StorageLevel 为内存＋磁盘(存在内存的部分存在Storage空间中)。
显然，这时候 data 在 blockManager 里存了两份，不过等全部 executor 都 fetch 结束，存储 data blocks 那份可以删掉了.
## 总结
对于 Spark 来讲，broadcast 时考虑的不仅是如何将公共 data 分发下去的问题，还要考虑如何让同一节点上的 task 共享 data。

对于第一个问题，Spark 设计了两种 broadcast 的方式，传统存在单点瓶颈问题的 HttpBroadcast，和类似 BT 方式的 TorrentBroadcast。
HttpBroadcast 使用传统的 client-server 形式的 HttpServer 来传递真正的 data，而 TorrentBroadcast 使用 blockManager 自带的 NIO 
通信方式来传递 data。
**TorrentBroadcast 存在的问题是慢启动和占内存**，慢启动指的是刚开始data只在driver上有，要等executors fetch很多轮 data block后，
data server才会变得可观，后面的fetch速度才会变快。executor所占内存的在fetch完data blocks后进行反序列化时需要将近两倍data size的
内存消耗。不管哪一种方式，driver 在分块时会有两倍 data size 的内存消耗。

对于第二个问题，每个executor都包含一个blockManager用来管理存放在executor里的数据，将公共数据存放在blockManager中
(StorageLevel 为内存(Storage)＋磁盘)，可以保证在 executor 执行的tasks 能够共享data。