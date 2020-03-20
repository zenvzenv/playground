# Spark内存管理
Spark在1.6.x版本采用的是静态的内存管理机制，在2.x.x版本之后采用了统一的内存管理机制。
## 堆内内存和堆外内存规划
### 堆内内存
  堆内内存的大小，由 Spark 应用程序启动时的 –executor-memory 或 spark.executor.memory 参数配置。Executor 内运行的并发任务共享 JVM 堆内内存，这些任务在缓存 RDD 数据和广播（Broadcast）数据时占用的内存被规划为存储(Storage)内存，而这些任务在执行 Shuffle 时占用的内存被规划为执行(Execution)内存，Storage内存空间和Execution内存空间统称为SparkSpace，由Spark进行内存使用，剩余的部分不做特殊规划，那些 Spark 内部的对象实例，或者用户定义的 Spark 应用程序中的对象实例，均占用剩余的空间，即UserSpace，由用户来支配这部分内存的使用。不同的管理模式下，这三部分占用的空间大小各不相同
  Spark 对堆内内存的管理是一种逻辑上的"规划式"的管理，因为对象实例占用内存的申请和释放都由 JVM 完成，Spark 只能在申请后和释放前记录这些内存，我们来看其具体流程：
  	* 申请内存：
	  1. Spark 在代码中 new 一个对象实例
	  2. JVM 从堆内内存分配空间，创建对象并返回对象引用
	  3. Spark 保存该对象的引用，记录该对象占用的内存
	* 释放内存
	  1. Spark 记录该对象释放的内存，删除该对象的引用
	  2. 等待 JVM 的垃圾回收机制释放该对象占用的堆内内存
  JVM 的对象可以以序列化的方式存储，序列化的过程是将对象转换为二进制字节流，本质上可以理解为将非连续空间的链式存储转化为连续空间或块存储，在访问时则需要进行序列化的逆过程——反序列化，将字节流转化为对象，序列化的方式可以节省存储空间，但增加了存储和读取时候的计算开销。
  对于 Spark 中序列化的对象，由于是字节流的形式，其占用的内存大小可直接计算，而对于非序列化的对象，其占用的内存是通过周期性地采样近似估算而得，即并不是每次新增的数据项都会计算一次占用的内存大小，这种方法降低了时间开销但是有可能误差较大，导致某一时刻的实际内存有可能远远超出预期。此外，在被 Spark 标记为释放的对象实例，很有可能在实际上并没有被 JVM 回收，导致实际可用的内存小于 Spark 记录的可用内存。所以 Spark 并不能准确记录实际可用的堆内内存，从而也就无法完全避免内存溢出(OOM, Out of Memory)的异常。
  虽然不能精准控制堆内内存的申请和释放，但 Spark 通过对存储内存和执行内存各自独立的规划管理，可以决定是否要在存储内存里缓存新的 RDD，以及是否为新的任务分配执行内存，在一定程度上可以提升内存的利用率，减少异常的出现。
### 堆外内存
  为了进一步优化内存的使用以及提高 Shuffle 时排序的效率，Spark 引入了堆外(Off-heap)内存，使之可以直接在工作节点的系统内存中开辟空间，存储经过序列化的二进制数据。利用 JDK Unsafe API(从 Spark 2.0 开始，在管理堆外的存储内存时不再基于 Tachyon，而是与堆外的执行内存一样，基于 JDK Unsafe API 实现)，Spark 可以直接操作系统堆外内存，减少了不必要的内存开销，以及频繁的 GC 扫描和回收，提升了处理性能。堆外内存可以被精确地申请和释放，而且序列化的数据占用的空间可以被精确计算，所以相比堆内内存来说降低了管理的难度，也降低了误差。
  在默认情况下堆外内存并不启用，可通过配置 `spark.memory.offHeap.enabled` 参数启用，并由 `spark.memory.offHeap.size` 参数设定堆外空间的大小。除了没有 other 空间，堆外内存与堆内内存的划分方式相同，所有运行中的并发任务共享存储内存和执行内存。
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
  MemoryManager 的具体实现上，Spark 1.6 之后默认为统一管理(Unified Memory Manager)方式，1.6 之前采用的静态管理(Static Memory Manager)方式仍被保留，可通过配置 `spark.memory.useLegacyMode` 参数启用。两种方式的区别在于对空间分配的方式。
## Spark 1.6.x静态内存管理
  在 Spark 最初采用的静态内存管理机制下，存储内存、执行内存和其他内存的大小在 Spark 应用程序运行期间均为固定的，但用户可以应用程序启动前进行配置，堆内内存的分配如下图所示：
  ![spark_static_memory_model](https://github.com/zw030301/playground/blob/master/spark/src/main/resources/mdimage/spark_static_memory_model.png)
  可以看到，可用的堆内内存的大小需要按照下面的方式计算：
  ```text
  可用的存储内存 = systemMaxMemory * spark.storage.memoryFraction * spark.storage.safetyFraction
  可用的执行内存 = systemMaxMemory * spark.shuffle.memoryFraction * spark.shuffle.safetyFraction
  ```
其中 systemMaxMemory 取决于当前 JVM 堆内内存的大小，由-executor-memory，最后可用的执行内存或者存储内存要在此基础上与各自的 memoryFraction 参数和 safetyFraction 参数相乘得出。上述计算公式中的两个 safetyFraction 参数，其意义在于在逻辑上预留出 1-safetyFraction 这么一块保险区域，降低因实际内存超出当前预设范围而导致 OOM 的风险（上文提到，对于非序列化对象的内存采样估算会产生误差）。值得注意的是，这个预留的保险区域仅仅是一种逻辑上的规划，在具体使用时 Spark 并没有区别对待，和"其它内存"一样交给了 JVM 去管理。
堆外的空间分配较为简单，只有存储内存和执行内存，如图 3 所示。可用的执行内存和存储内存占用的空间大小直接由参数 spark.memory.storageFraction 决定，由于堆外内存占用的空间可以被精确计算，所以无需再设定保险区域。
静态内存管理机制实现起来较为简单，但如果用户不熟悉 Spark 的存储机制，或没有根据具体的数据规模和计算任务或做相应的配置，很容易造成"一半海水，一半火焰"的局面，即存储内存和执行内存中的一方剩余大量的空间，而另一方却早早被占满，不得不淘汰或移出旧的内容以存储新的内容。由于新的内存管理机制的出现，这种方式目前已经很少有开发者使用，出于兼容旧版本的应用程序的目的，Spark 仍然保留了它的实现。
### 涉及到的Spark配置属性
`spark.shuffle.memoryFraction = 0.2` Exception内存区域，该区域用于缓存shuffle过程中产生的临时数据，
`spark.shuffle.safety.memoryFraction = 0.8` Exception内存区域中并不是所有的内存都是可用的，Spark会留存一个安全区域用于防止OOM的发生，该系数就是控制Exception内存区域中的可用区域即Exception区域的0.8，即占全部内存的 `spark.shuffle.memoryFraction * spark.shuffle.safetyFraction = 0.16`
`1 - spark.shuffle.safety.memoryFraction` Execution的安全区域，为了防止发生OOM错误
`spark.storage.memoryFraction = 0.6` Storage内存区域， 用于缓存RDD数据和broadcast数据
`spark.storage.safetyFraction = 0.9` Storage内存区域可用空间，占全部内存空间的 `spark.storage.memoryFraction * spark.storage.safety.memoryFraction = 0.54`
`1 - spark.storage.safetyFraction` Storage内存区域的安全空间
`user memory = 1- spark.shuffle.memoryFraction - spark.storage.memoryFraction`
## Spark 2.x.x统一内存管理
在默认情况下，Spark会只会使用堆内内存，Executor会将内存划分成以下几块：
* Execution内存：主要用于缓存Shuffle过程中产生的中间数据，即join、sort、aggregate等计算过程中的临时数据
* Storage内存：主要用于缓存broadcast和RDD
