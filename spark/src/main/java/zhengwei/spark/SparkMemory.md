# Spark内存管理
Spark在1.6.x版本采用的是静态的内存管理机制，在2.x.x版本之后采用了统一的内存管理机制。
## 堆内内存和堆外内存规划
### 堆内内存
堆内内存的大小，由 Spark 应用程序启动时的 `-–executor-memory` 或 `spark.executor.memory` 参数配置。
Executor 内运行的并发任务共享 JVM 堆内内存，这些任务在缓存 RDD 数据和广播（Broadcast）数据时占用的内存被规划为存储(Storage)内存，
而这些任务在执行 Shuffle 时占用的内存被规划为执行(Execution)内存，Storage内存空间和Execution内存空间统称为SparkSpace，
由Spark进行内存使用，剩余的部分不做特殊规划，那些 Spark 内部的对象实例，或者用户定义的 Spark 应用程序中的对象实例，
均占用剩余的空间，即UserSpace，由用户来支配这部分内存的使用。不同的管理模式下，这三部分占用的空间大小各不相同  
Spark 对堆内内存的管理是一种逻辑上的"规划式"的管理，因为对象实例占用内存的申请和释放都由 JVM 完成，
Spark 只能在申请后和释放前记录这些内存，我们来看其具体流程：  
* 申请内存：
  1. Spark 在代码中 new 一个对象实例
  2. JVM 从堆内内存分配空间，创建对象并返回对象引用
  3. Spark 保存该对象的引用，记录该对象占用的内存
* 释放内存
  1. Spark 记录该对象释放的内存，删除该对象的引用
  2. 等待 JVM 的垃圾回收机制释放该对象占用的堆内内存
JVM 的对象可以以序列化的方式存储，序列化的过程是将对象转换为二进制字节流，本质上可以理解为将非连续空间的链式存储转化为连续空间或块存储，
在访问时则需要进行序列化的逆过程——反序列化，将字节流转化为对象，序列化的方式可以节省存储空间，但增加了存储和读取时候的计算开销。  
对于 Spark 中序列化的对象，由于是字节流的形式，其占用的内存大小可直接计算，而对于非序列化的对象，
其占用的内存是通过周期性地采样近似估算而得，即并不是每次新增的数据项都会计算一次占用的内存大小，
这种方法降低了时间开销但是有可能误差较大，导致某一时刻的实际内存有可能远远超出预期。此外，
在被 Spark 标记为释放的对象实例，很有可能在实际上并没有被 JVM 回收，导致实际可用的内存小于 Spark 记录的可用内存。
所以 Spark 并不能准确记录实际可用的堆内内存，从而也就无法完全避免内存溢出(OOM, Out of Memory)的异常。  
虽然不能精准控制堆内内存的申请和释放，但 Spark 通过对存储内存和执行内存各自独立的规划管理，可以决定是否要在存储内存里缓存新的 RDD，
以及是否为新的任务分配执行内存，在一定程度上可以提升内存的利用率，减少异常的出现。
### 堆外内存
为了进一步优化内存的使用以及提高 Shuffle 时排序的效率，Spark 引入了堆外(Off-heap)内存，使之可以直接在工作节点的系统内存中开辟空间，
存储经过序列化的二进制数据。利用 JDK Unsafe API(从 Spark 2.0 开始，在管理堆外的存储内存时不再基于 Tachyon，而是与堆外的执行内存一样，
基于 JDK Unsafe API 实现)，Spark 可以直接操作系统堆外内存，减少了不必要的内存开销，以及频繁的 GC 扫描和回收，提升了处理性能。
堆外内存可以被精确地申请和释放，而且序列化的数据占用的空间可以被精确计算，所以相比堆内内存来说降低了管理的难度，也降低了误差。
在默认情况下堆外内存并不启用，可通过配置 `spark.memory.offHeap.enabled` 参数启用，
并由 `spark.memory.offHeap.size` 参数设定堆外空间的大小。除了没有 other 空间，堆外内存与堆内内存的划分方式相同，
所有运行中的并发任务共享存储内存和执行内存。
### 内存管理接口
Spark 为存储内存和执行内存的管理提供了统一的接口——MemoryManager，同一个 Executor 内的任务都调用这个接口的方法来申请或释放内存:
```scala
//申请存储内存
def acquireStorageMemory(blockId: BlockId, numBytes: Long, memoryMode: MemoryMode): Boolean
//申请展开内存
def acquireUnrollMemory(blockId: BlockId, numBytes: Long, memoryMode: MemoryMode): Boolean
//申请执行内存
def acquireExecutionMemory(numBytes: Long, taskAttemptId: Long, memoryMode: MemoryMode): Long
//释放存储内存
def releaseStorageMemory(numBytes: Long, memoryMode: MemoryMode): Unit
//释放执行内存
def releaseExecutionMemory(numBytes: Long, taskAttemptId: Long, memoryMode: MemoryMode): Unit
//释放展开内存
def releaseUnrollMemory(numBytes: Long, memoryMode: MemoryMode): Unit
```
我们看到，在调用这些方法时都需要指定其内存模式（MemoryMode），这个参数决定了是在堆内还是堆外完成这次操作。  
MemoryManager 的具体实现上，Spark 1.6 之后默认为统一管理(Unified Memory Manager)方式，
1.6 之前采用的静态管理(Static Memory Manager)方式仍被保留，可通过配置 `spark.memory.useLegacyMode` 参数启用。
两种方式的区别在于对空间分配的方式。
## Spark 1.6.x静态内存管理
### 堆内内存
在 Spark 最初采用的静态内存管理机制下，存储内存、执行内存和其他内存的大小在 Spark 应用程序运行期间均为固定的，
但用户可以应用程序启动前进行配置，堆内内存的分配如下图所示：
![spark_static_memory_model](https://github.com/zw030301/playground/blob/master/spark/src/main/resources/mdimage/spark_static_memory_model.png)
可以看到，可用的堆内内存的大小需要按照下面的方式计算：
```text
可用的存储内存 = systemMaxMemory * spark.storage.memoryFraction * spark.storage.safetyFraction
可用的执行内存 = systemMaxMemory * spark.shuffle.memoryFraction * spark.shuffle.safetyFraction
```
其中 systemMaxMemory 取决于当前 JVM 堆内内存的大小，由 `--executor-memory` 指定其大小，
最后可用的执行内存或者存储内存要在此基础上与各自的 memoryFraction 参数和 safetyFraction 参数相乘得出。
上述计算公式中的两个 safetyFraction 参数，其意义在于在逻辑上预留出 1-safetyFraction 这么一块保险区域，
降低因实际内存超出当前预设范围而导致 OOM 的风险（上文提到，对于非序列化对象的内存采样估算会产生误差）。
值得注意的是，这个预留的保险区域仅仅是一种逻辑上的规划，在具体使用时 Spark 并没有区别对待，和"其它内存"一样交给了 JVM 去管理。  
堆外的空间分配较为简单，只有存储内存和执行内存。可用的执行内存和存储内存占用的空间大小直接由参数`spark.memory.storageFraction`决定，
由于堆外内存占用的空间可以被精确计算，所以无需再设定保险区域。  
静态内存管理机制实现起来较为简单，但如果用户不熟悉 Spark 的存储机制，或没有根据具体的数据规模和计算任务或做相应的配置，
很容易造成"一半海水，一半火焰"的局面，即存储内存和执行内存中的一方剩余大量的空间，而另一方却早早被占满，
不得不淘汰或移出旧的内容以存储新的内容。由于新的内存管理机制的出现，这种方式目前已经很少有开发者使用，出于兼容旧版本的应用程序的目的，
Spark 仍然保留了它的实现。
### 涉及到的Spark配置属性
`spark.shuffle.memoryFraction = 0.2` Exception内存区域，该区域用于缓存shuffle过程中产生的临时数据
`spark.shuffle.safety.memoryFraction = 0.8` Exception内存区域中并不是所有的内存都是可用的，
Spark会留存一个安全区域用于防止OOM的发生，该系数就是控制Exception内存区域中的可用区域即Exception区域的0.8，
即占全部内存的 `spark.shuffle.memoryFraction * spark.shuffle.safetyFraction = 0.16`  
`1 - spark.shuffle.safety.memoryFraction` Execution的安全区域，为了防止发生OOM错误  
`spark.storage.memoryFraction = 0.6` Storage内存区域， 用于缓存RDD数据和broadcast数据  
`spark.storage.safetyFraction = 0.9` Storage内存区域可用空间，
占全部内存空间的 `spark.storage.memoryFraction * spark.storage.safety.memoryFraction = 0.54`  
`1 - spark.storage.safetyFraction` Storage内存区域的安全空间
`user memory = 1- spark.shuffle.memoryFraction - spark.storage.memoryFraction`  
## Spark 2.x.x统一内存管理
### 堆内内存
对于统一内存管理的内存分布图如下所示：  
![spark_unite_memory_model](https://github.com/zw030301/playground/blob/master/spark/src/main/resources/mdimage/spark_unite_memory_model.png)
在默认情况下，Spark会只会使用堆内内存，Executor会将内存划分成以下几块：
* Execution内存：主要用于缓存Shuffle过程中产生的中间数据，即join、sort、aggregate等计算过程中的临时数据
* Storage内存：主要用于缓存broadcast和RDD
* User Memory：主要用于存储RDD转换操作所需要的数据，例如RDD的依赖信息
* Reserve Memory：系统预留内存，会用来存储Spark的内部对象  
### 堆外内存
统一内存模型的堆外内存在刚开始的划分是和静态内存的堆外内存是一样的，Execution和Storage的占比各位50%，
初始的Storage占比为 `spark.memory.storageFraction` 控制，默认为0.5，那么Execution的占比为 `1 - spark.memory.storageFraction`.
具体的分布如下图所示：
![spark_unite_off_heap_memory_model](https://github.com/zw030301/playground/blob/master/spark/src/main/resources/mdimage/spark_unite_off_heap_memory_model.png)  
### Execution 内存和 Storage 内存动态调整
![spark_unite_memory_management](https://github.com/zw030301/playground/blob/master/spark/src/main/resources/mdimage/spark_unite_memory_management.png)
* 设定基本的存储内存和执行内存区域（spark.storage.storageFraction 参数），该设定确定了双方各自拥有的空间的范围
* 双方的空间都不足时，则存储到硬盘；若己方空间不足而对方空余时，可借用对方的空间;(存储空间不足是指不足以放下一个完整的 Block)
* Execution内存的空间被对方占用后，可让对方将占用的部分转存到硬盘，然后"归还"借用的空间
* Storage内存的空间被对方占用后，无法让对方"归还"，因为需要考虑 Shuffle 过程中的很多因素，实现起来较为复杂;
而且 Shuffle 过程产生的文件在后面一定会被使用到，而 Cache 在内存的数据不一定在后面使用。
**上面说的借用对方的内存需要借用方和被借用方的内存类型都一样，都是堆内内存或者都是堆外内存，不存在堆内内存不够去借用堆外内存的空间。**
### Task之间内存分布
为了更好地使用使用内存，**Executor内运行的Task之间共享着Execution内存**。具体的Spark内部维护了一个HashMap用于记录每个Task占用的内存。
当Task需要像Execution申请内存时，具体过程如下：  
1. 判断HashMap中是否维护着这个Task的内存使用情况，如果没有，则将这个Task内存设置为0，并且以TaskId为key，
内存使用为value加入到HashMap里面
2. 为这个Task申请numBytes内存，如果 Execution 内存区域正好有大于 numBytes 的空闲内存，
则在 HashMap 里面将当前 Task 使用的内存加上 numBytes，然后返回
3. 如果当前 Execution 内存区域无法申请到每个 Task 最小可申请的内存，则当前 Task 被阻塞，直到有其他任务释放了足够的执行内存，该任务才可以被唤醒  
每个 Task 可以使用 Execution 内存大小范围为 1/2N ~ 1/N，其中 N 为当前 Executor 内正在运行的 Task 个数。
一个 Task 能够运行必须申请到最小内存为 (1/2N * Execution 内存)；当 N = 1 的时候，Task 可以使用全部的 Execution 内存。
> 比如如果 Execution 内存大小为 10GB，当前 Executor 内正在运行的 Task 个数为5，
>则该 Task 可以申请的内存范围为 10 / (2 * 5) ~ 10 / 5，也就是 1GB ~ 2GB的范围。
## 对于实际内存会比预期内存不一致的问题
1. Spark在获取 `--executor-memory` 参数的值时使用的是 `Runtime.getRuntime.maxMemory` 方法来获取可用内存的，
因为JVM的GC一般采用分代收集算法，所以JVM中是由Eden,From Survivor,To Survivor和Old区组成，其中一个Survivor空间采用的复制算法，
所以会有一个Survivor是不存数据的，所以`Runtime.getRuntime.maxMemory` 得到的实际容量会小。
2. Spark在计算内存时使用的进位时1000，而不是1024
3. 堆外内存是申请多少则就会有多少
## 存储内存管理
### RDD持久化机制
弹性分布式数据集(RDD)作为 Spark 最根本的数据抽象，是只读的分区记录(Partition)的集合，只能基于在稳定物理存储中的数据集上创建，
或者在其他已有的 RDD 上执行转换(Transformation)操作产生一个新的 RDD。转换后的 RDD 与原始的 RDD 之间产生的依赖关系，
构成了血统(Lineage)。凭借血统，Spark 保证了每一个 RDD 都可以被重新恢复。但 RDD 的所有转换都是惰性的，
即只有当一个返回结果给 Driver 的行动(Action)发生时，Spark 才会创建任务读取 RDD，然后真正触发转换的执行。  

Task 在启动之初读取一个分区时，会先判断这个分区是否已经被持久化，如果没有则需要检查 Checkpoint 或按照血统重新计算。
所以如果一个 RDD 上要执行多次行动，可以在第一次行动中使用 persist 或 cache 方法，在内存或磁盘中持久化或缓存这个 RDD，
从而在后面的行动时提升计算速度。事实上，cache 方法是使用默认的 MEMORY_ONLY 的存储级别将 RDD 持久化到内存，故缓存是一种特殊的持久化。
堆内和堆外存储内存的设计，便可以对缓存 RDD 时使用的内存做统一的规划和管理(存储内存的其他应用场景，如缓存 broadcast 数据，
暂时不在本文的讨论范围之内)。  

RDD 的持久化由 Spark 的 Storage 模块负责，实现了 RDD 与物理存储的解耦合。Storage 模块负责管理 Spark 在计算过程中产生的数据，
将那些在内存或磁盘、在本地或远程存取数据的功能封装了起来。在具体实现时 Driver 端和 Executor 端的 Storage 模块构成了**主从式的架构**，
即 Driver 端的 BlockManager 为 Master，Executor 端的 BlockManager 为 Slave。Storage 模块在逻辑上以 Block 为基本存储单位，
RDD 的每个 Partition 经过处理后唯一对应一个 Block(BlockId 的格式为 rdd_RDD-ID_PARTITION-ID)。Master 负责整个 Spark 应用程序的 
Block 的元数据信息的管理和维护，而 Slave 需要将 Block 的更新等状态上报到 Master，同时接收 Master 的命令，例如新增或删除一个 RDD。
### 存储级别
```java
class StorageLevel private(
    private var _useDisk: Boolean,//是否使用磁盘
    private var _useMemory: Boolean,//是否使用堆内存
    private var _useOffHeap: Boolean,//是否使用堆外内存
    private var _deserialized: Boolean,//是否序列化
    private var _replication: Int = 1)//副本数量
```
* 存储位置：磁盘／堆内内存／堆外内存。如 MEMORY_AND_DISK 是同时在磁盘和堆内内存上存储，实现了冗余备份。OFF_HEAP 则是只在堆外内存存储，
目前选择堆外内存时不能同时存储到其他位置。
* 存储形式：Block 缓存到存储内存后，是否为非序列化的形式。如 MEMORY_ONLY 是非序列化方式存储，OFF_HEAP 是序列化方式存储。
* 副本数量：大于 1 时需要远程冗余备份到其他节点。如 DISK_ONLY_2 需要远程备份 1 个副本。
### RDD缓存过程
RDD 在缓存到存储内存之前，Partition 中的数据一般以迭代器的数据结构来访问，这是 Scala 语言中一种遍历数据集合的方法。
通过 Iterator 可以获取分区中每一条序列化或者非序列化的数据项(Record)，这些 Record 的对象实例在逻辑上占用了 JVM堆内内存的**other** 
部分的空间，同一 Partition 的不同 Record 的**空间并不连续**。

RDD 在缓存到存储内存之后，Partition 被转换成 Block，Record 在堆内或堆外存储内存中占用一块**连续的空间**。
**将 Partition 由不连续的存储空间转换为连续存储空间的过程，Spark 称之为”展开”(Unroll)**，Block 有序列化和非序列化两种存储格式，
具体以哪种方式取决于该RDD的存储级别。非序列化的 Block 以一种 DeserializedMemoryEntry 的数据结构定义，用一个数组存储所有的对象实例；
序列化的 Block 则以 SerializedMemoryEntry的数据结构定义，用字节缓冲区(ByteBuffer)来存储二进制数据。每个 Executor 的 Storage 
模块用一个链式 Map 结构(LinkedHashMap)来管理堆内和堆外存储内存中所有的 Block 对象的实例，
对这个 LinkedHashMap 新增和删除间接记录了内存的申请和释放。

因为不能保证存储空间可以一次容纳 Iterator 中的所有数据，当前的计算任务在 Unroll 时要向 MemoryManager 申请足够的 Unroll 空间来临时
占位，空间不足则 Unroll 失败，空间足够时可以继续进行。对于序列化的 Partition，其所需的 Unroll 空间可以直接累加计算，一次申请。
而非序列化的 Partition 则要在遍历 Record 的过程中依次申请，即每读取一条 Record，采样估算其所需的 Unroll 空间并进行申请，
空间不足时可以中断，释放已占用的 Unroll 空间。

在静态内存管理时，Spark 在存储内存中专门划分了一块 Unroll 空间，其大小是固定的，统一内存管理时则没有对 Unroll 空间进行特别区分，
当存储空间不足时会根据动态占用机制进行处理。

### 淘汰和落盘
由于同一个 Executor 的所有的计算任务共享有限的存储内存空间，当有新的 Block 需要缓存但是剩余空间不足且无法动态占用时，
就要对 LinkedHashMap 中的旧 Block 进行淘汰（Eviction），而被淘汰的 Block 如果其存储级别中同时包含存储到磁盘的要求，
则要对其进行落盘(Drop)，否则直接删除该 Block。

存储内存的淘汰规则为：

* 被淘汰的旧 Block 要与新 Block 的 MemoryMode 相同，即同属于堆外或堆内内存
* 新旧 Block 不能属于同一个 RDD，避免循环淘汰
* 旧 Block 所属 RDD 不能处于被读状态，避免引发一致性问题
* 遍历 LinkedHashMap 中 Block，按照最近最少使用(LRU)的顺序淘汰，直到满足新 Block 所需的空间。其中 LRU 是 LinkedHashMap 的特性。

落盘的流程则比较简单，如果其存储级别符合_useDisk 为 true 的条件，再根据其_deserialized 判断是否是非序列化的形式，
若是则对其进行序列化，最后将数据存储到磁盘，在 Storage 模块中更新其信息。

## 执行内存管理
### 多任务间内存分配
Executor 内运行的任务同样**共享执行内存**，Spark 用一个 HashMap 结构保存了任务到内存耗费的映射。每个任务可占用的执行内存大小的范围
为 1/2N ~ 1/N，其中 N 为当前 Executor 内正在运行的任务的个数。每个任务在启动之时，要向 MemoryManager 请求申请最少为 1/2N 的执行内存，
如果不能被满足要求则该任务被阻塞，直到有其他任务释放了足够的执行内存，该任务才可以被唤醒。

### Shuffle的内存占用
执行内存主要用来存储任务在执行 Shuffle 时占用的内存，Shuffle 是按照一定规则对 RDD 数据重新分区的过程，我们来看 Shuffle 的 Write 和 
Read 两阶段对执行内存的使用：
* Shuffle Write
    1. 若在 map 端选择普通的排序方式，会采用 ExternalSorter 进行外排，在内存中存储数据时主要占用堆内执行空间。
    2. 若在 map 端选择 Tungsten 的排序方式，则采用 ShuffleExternalSorter 直接对以**序列化形式**存储的数据排序，
    在内存中存储数据时可以占用堆外或堆内执行空间，取决于用户是否开启了堆外内存以及堆外执行内存是否足够。
* Shuffle Read
    1. 在对 reduce 端的数据进行聚合时，要将数据交给 Aggregator 处理，在内存中存储数据时占用堆内执行空间。
    2. 如果需要进行最终结果排序，则要将再次将数据交给 ExternalSorter 处理，占用堆内执行空间。

在 ExternalSorter 和 Aggregator 中，Spark 会使用一种叫 AppendOnlyMap 的哈希表在堆内执行内存中存储数据，
但在 Shuffle 过程中所有数据并不能都保存到该哈希表中，当这个哈希表占用的内存会进行周期性地采样估算，当其大到一定程度，
无法再从 MemoryManager 申请到新的执行内存时，Spark 就会将其全部内容存储到磁盘文件中，这个过程被称为溢存(Spill)，
溢存到磁盘的文件最后会被归并(Merge)。

Shuffle Write 阶段中用到的 Tungsten 是 Databricks 公司提出的对 Spark 优化内存和 CPU 使用的计划，
解决了一些 JVM 在性能上的限制和弊端。Spark 会根据 Shuffle 的情况来自动选择是否采用 Tungsten 排序。
Tungsten 采用的页式内存管理机制建立在 MemoryManager 之上，即 Tungsten 对执行内存的使用进行了一步的抽象，
这样在 Shuffle 过程中无需关心数据具体存储在堆内还是堆外。每个内存页用一个 MemoryBlock 来定义，并用 Object obj 和 long offset 
这两个变量统一标识一个内存页在系统内存中的地址。堆内的 MemoryBlock 是以 long 型数组的形式分配的内存，其 obj 的值为是这个数组的对象引
用，offset 是 long 型数组的在 JVM 中的初始偏移地址，两者配合使用可以定位这个数组在堆内的绝对地址；堆外的 MemoryBlock 是直接申请到的
内存块，其 obj 为 null，offset 是这个内存块在系统内存中的64位绝对地址。Spark 用 MemoryBlock 巧妙地将堆内和堆外内存页统一抽象封装，
并用页表(pageTable)管理每个 Task 申请到的内存页。

Spark 的存储内存和执行内存有着截然不同的管理方式：对于存储内存来说，Spark 用一个 LinkedHashMap 来集中管理所有的 Block，
Block 由需要缓存的 RDD 的 Partition 转化而成；而对于执行内存，Spark 用 AppendOnlyMap 来存储 Shuffle 过程中的数据，
在 Tungsten 排序中甚至抽象成为页式内存管理，开辟了全新的 JVM 内存管理机制。
