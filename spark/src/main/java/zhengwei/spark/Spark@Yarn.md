# Spark on Yarn
## Spark中的Executor和Yarn中的Container的关系
Spark中的Executor与Yarn中的Container是一一对应的关系。即在 `spark-submit` 提交Spark任务的时候，在指定 `--num-executors` 的个数时也就等于指定了Yarn中的Container的个数。
Spark在向ResourceManager申请资源的时候，申请的核数由 `--executor-core` 指定，申请的内存大小由 `--executor-memory` 指定。
需要注意的是，在Spark@Yarn模式下，不论是client还是cluster模式，ResourceManager都会启动一个Container来运行ApplicationMaster，在申请资源的时候需要考虑到这一点
## Spark@Yarn的内存分配
### Spark@Yarn中的一些基本参数
* `spark.driver.memory` 默认值为512M
* `spark.executor.memory` 默认值为512M
* `spark.yarn.am.memory` 默认值为512M
* `spark.yarn.executor.memoryOverhead` 默认值为 `max(384M, executorMemory * 0.1)`
* `spark.yarn.driver.memoryOverhead` 默认值为 `max(384M, driverMemory * 0.1)`
* `spark.yarn.am.memoryOverhead` 默认值为 `max(384M, amMemory * 0.1)`
### Yarn中的关键参数
因为Spark@Yarn之上，Spark的task运行在Yarn的NodeManager之上，对于Spark申请资源，Yarn中的配置也起到关键作用，由如下关键配置：
* `yarn.app.mapreduce.am.resource.mb`:ApplicationMaster能够申请到的最大内存，默认1536M
* `yarn.nodemanager.resource.memory-mb`:NodeManager能够申请的最大内存，默认8192M
* `yarn.scheduler.minimum-allocate-mb`:调度一个Container时，能够申请的最小内存，默认为1024M
* `yarn.scheduler.maximum-allocate-mb`:调度一个Container时，能够申请的最大内存，默认8192M
在向Yarn申请内存时，申请的内存时，yarn会向上取 `yarn.scheduler.minimum-allocate-mb` 的整数倍
## Spark内存分布
JVM中实际可用内存要比我们申请的内存要小，因为JVM内部也需要占用内存， `Runtime.getRuntime.maxMemory` 获取到的内存之比实际申请的少。
#### Spark1.6x
Spark1.6之前使用的时静态的内存分布，每个内存区域都是固定的，不可变动。
整个内存区域被分成3哥区域，storage区，shuffle区和unroll区
storage空间：用于缓存RDD数据和broadcast数据，大小占整个JVM内存比例为 `spark.storage.memoryFraction` 默认0.6  
storage预留内存：防止OOM，大小占整个JVM的内存比例为 `spark.storage.memoryFraction * (1-spark.storage.safetyFraction)` 默认 0.6 * (1-0.9)=0.06  
storage安全可用空间：真正用于存储RDD数据和broadcast数据的内存，大小占整个JVM内存比例为 `spark.stotage.memory * spark.storage.safetyFraction` 默认 0.6 * 0.9=0.54  
unroll空间：用于缓存iterator形式的block数据，大小占整个JVM内存比例为 `spark.storage.memory * spark.storage.safetyFraction * spark.storage.unrollFractioon` 默认 0.6 * 0.9*0.2=0.108  
execution空间：用于缓存shuffle过程中的中间数据，大小占整个JVM内存比例为 `spark.shuffle.memoryFraction` 默认0.2  
execution可用空间：真正用于缓存shuffle过程中的中间数据，大小占整个JVM内存比例为 `spark.shuffle.memoryFraction * spark.shuffle.safetyFraction` 默认0.2 * 0.9=0.18  
execution预留空间：当值execution发生OOM，大小占整个JVM内存比例为 `spark.shuffle.memoryFraction * (1 - spark.shuffle.safetyFraction)` 默认 0.2 * 0.1=0.02  
user空间：用于存储用户自定义数据结构或Spark内部元数据，数据数据在此区域被计算，大小占整个JVM内存比例为 `1 - spark.storage.memoryFraction - spark.shuffle.memoryFraction` 默认0.2  
#### Spark2.x
在是实例中，Spark的Executor页面上显示的每个Executor的内存有11.3G可用  
Spark2.x之后使用统一内存管理机制,storage和execution空间的内存可以相互借用，不再是一成不变的  
预留300M内存，保证系统稳定运行，作用与user空间类似，存储用户自定义数据结构和Spark内部元数据
统一内存：用于Storage空间和Execution空间，两者共用此空间，大小占整个JVM内存比例为 `(java heap - 300M) * spark.memory.fraction` 默认0.6  
Storage空间：用于缓存RDD数据和broadcast数据，大小占整个JVM内存比例为 `(java heap - 300M) * (spark.memory.fraction * spark.storage.storageFraction)` 默认0.6 * 0.5=0.3  
Execution空间：用于缓存shuffle过程中的中间数据，大小占整个JVM内存比例为 `(java headp - 300M) * (spark.memory.fraction * (1 - spark.storage.storageFraction))` 默认0.6 * (1 - 0.5)=0.3  
>Storage空间和Execution空间的内存是可以互相借用的，如果计算过程中只有RDD数据和broadcast数据，那么Storage空间将会占用大部分空间，反之亦然。

user空间：用于缓存用户自定义数据结构或Spark内部元数据，大小占整个JVM内存比例为 `(java head - 300M) * (1 - spark.memory.fraction)` 默认0.4
### 注意点：
1. 在提交Spark任务的时候指定的 `--executor-memory` 和 `spark.executor.memory` 控制executor堆的大小。
JVM本身会占用一部分内存， `spark.yarn.xxx.memoryOverhead` 指定了在向yarn申请内存时在指定的内存容量的基础上再加上额外的内存再去申请内存
2. 如果Executor设置的内存过大，会导致GC缓慢，官方推荐64G内存为一个Executor的内存上限
3. hdfs client对于多线程高并发存在性能问题，建议一个Executor设置不多于5个core
4. 对于yarn-client模式下的driver的内存默认为1G的说法是有问题的，从spark-yarn-client模式下的日志来看 `Will allocate AM container, with 896 MB memory including 384 MB overhead` ，
可以看出来，spark在向yarn申请内存的时候多申请了384M，因为spark中的driver的默认申请内存大小为512M，然后再额外申请max(384M, driverMemory * 0.1)，即896M内存，
然而，yarn在分配内存的时候会向上取 `yarn.scheduler.minimum-allocate-mb` 的整数倍，又 `yarn.scheduler.minimum-allocate-mb` 默认1G，所以driver会申请1G内存。
5. Spark的Executor被实例化为 `CoarseGrainedExecutorBackend` ， ApplicationMaster被实例化为 `ExecutorLauncher` 
5. 虽然Spark在向yarn申请内存时会冗余申请内存，即申请22G内存，但是实际在启动Executor时传入的JVM参数还是申请的内存值，即20G内存，从进程信息可以看出来。
通过命令 `jps -v | grep CoarseGrainedExecutorBackend` 可以看到传入到JVM中的参数 `CoarseGrainedExecutorBackend -Xmx20480m` ，实际申请了20G堆内存大小
### 实例说明
具体的提交命令如下：
```bash
SPARK_OPTS="--master yarn \
 --deploy-mode client \
 --executor-memory 20G \
 --num-executors 3 \
 --executor-cores 7 \
 --driver-memory 2g \
 --queue root.spark_queue
```
该命令中，我们申请了3个executor，每个executor申请20G和3个核心。  
按照预期，我们申请的总container的个数为3个，总memory为60G，总core为31个。  
我们查看yarn的管理界面，看到如下  
![spark_on_yarn_core_memory](https://github.com/zw030301/playground/blob/master/spark/src/main/resources/mdimage/yarn_core_memory.png)  
可以看到申请的资源情况，总共有4个container，22个core，67G memory，为什么会比我们预想的要多呢？  
因为ApplicationMaster需要独占一个container，这个独占的container独立于我们申请资源之外，这个container占用1个core，1G内存(为什么是1G内存，前面已经说明了)，
这样container和core的个数就能够对上了，但是内存还是会对不上，这样算的话，总内存应该是61G，但是页面显示有67G，多出来了6G内存。
这6G内存是因为，Spark在为Executor申请内存的时候，为在 `--executor-memory` 的基础上再额外申请 `spark.yarn.executor.memoryOverhead` 的内存，即 `max(20G * 0.1 * 1024, 384M) = 2G`，
所以Executor申请了22G内存，即3个executor申请了66G内存，再加1G的ApplicationMaster的内存，正好是67G内存。  
