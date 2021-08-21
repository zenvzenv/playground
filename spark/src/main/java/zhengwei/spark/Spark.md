# Spark专栏
**_学习和实验spark一些功能的时候写的一些代码_**
## RDD
1. RDD(Resilient Distributed Dataset)：弹性分布式数据集
    1. 是Spark的运算基石，为用户屏蔽了底层对数据的复杂抽象和处理，为用户提供了一组方便的数据转换和求值方法。RDD**不可变、可分区以及里面的元素可以并行计算的集合**。
    2. RDD的特点：
        1. 自容错
        2. 位置感知
        3. 可伸缩
        4. 可缓存，提高效率
        5. A list of partitions (一系列分区，分区有编号，有顺序)
        6. A function for computing each split (每一个切片都会有一个函数作业在上面用于对数据进行处理)
        7. A list of dependencies on other RDDs (RDD与RDD之间存在依赖关系，形成linkage血统)
        8. Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned) (可选，key-value形式RDD才会有RDD[(K,V)])，如果是KV形式的RDD，会有一个分区器，默认是hash-partition)
        9. Optionally, a list of preferred locations to compute each split on (e.g. block locations foran HDFS file) (可以师从hdfs中获取数据，会得到数据的最优位置(向Name Node请求元数据))
    3. RDD的弹性表现：
        1. 存储的弹性：内存与磁盘的自动切换
        2. 容错的弹性：数据丢失可以根据血统重新计算出来
        3. 计算的弹性：计算出错重试机制
        4. 分片的弹性：根据需要对RDD进行重新分区
    4. RDD都做了什么
        1. RDD的创建：我们可以从hdfs、hive、hbase和集合数据类型(makeRDD,parallelize)中获取读取数据并获得RDD，也可以通过一个RDD的转换得到另一个RDD
        2. RDD的转换：Spark提供一系列的转换RDD算子，用来对数据的转换，例如map,flatMap,reduceByKey...，转换算子是懒执行的，需要有行动算子触发转换算子的执行
        3. RDD的缓存：如果一个RDD的血统过长，那么在数据都是进行重算时效率将会很低，那么我们可以把后续需要用到的RDD进行缓存起来，这样Spark会切断血统，提升重算效率
        4. RDD的行动：行动算子用来触发转换算子的执行，一个application中有几个job取决于有几个行动算子，**有几个Action算子就会有几个Job，Job与Job之间的Stage是不共用的，每个Job的Stage划分是独立划分的。**
        5. RDD的输出：Spark可以将得到的结果输出到hdfs上，也可以时本地文件系统，还可以收集最终结果到一个集合中以供后续操作
    5. RDDInfo(RDD对象信息)，实现类： `org.apache.spark.storage.RDDInfo`
        1. 实现源码
        ```scala
        @DeveloperApi
        class RDDInfo(
            val id: Int,//RDD的id
            var name: String,//RDD的name
            val numPartitions: Int,//RDD的分区数量
            var storageLevel: StorageLevel,//RDD的存储级别，即org.apache.spark.storage.StorageLevel
            val parentIds: Seq[Int],//RDD的父RDD的id序号，这里说明一个RDD会有一个到多个父RDD
            val callSite: String = "",//RDD的用户调用栈
            val scope: Option[RDDOperationScope] = None)//RDD的操作范围。scope的类型为RDDperationScope，每一个RDD都有一个RDDperationScope。RDDperationScope与Stage和Job之间并无特殊关系，一个RDDperationScope可以存在一个Stage中或存在多个Stage中。
          extends Ordered[RDDInfo] {
          //缓存的分区数量
          var numCachedPartitions = 0
          //使用的内存大小
          var memSize = 0L
          //使用的磁盘大小
          var diskSize = 0L
          //block存储在外部的大小
          var externalBlockStoreSize = 0L
          //此RDD是否已经被缓存
          def isCached: Boolean = (memSize + diskSize > 0) && numCachedPartitions > 0
        
          override def toString: String = {
            import Utils.bytesToString
            ("RDD \"%s\" (%d) StorageLevel: %s; CachedPartitions: %d; TotalPartitions: %d; " +
              "MemorySize: %s; DiskSize: %s").format(
                name, id, storageLevel.toString, numCachedPartitions, numPartitions,
                bytesToString(memSize), bytesToString(diskSize))
          }
          //由于RDDInfo继承了Ordered，所以重写了compare方法用于排序
          override def compare(that: RDDInfo): Int = {
            this.id - that.id
          }
        }
        //RDDInfo的伴生对象
        private[spark] object RDDInfo {
          def fromRdd(rdd: RDD[_]): RDDInfo = {
            //如果没有指定RDD的名字，则调用org.apache.spark.util.Utils中的getFormattedClassName方法自动生成一个随机名字
            val rddName = Option(rdd.name).getOrElse(Utils.getFormattedClassName(rdd))
            //获取当前RDD依赖的所有父RDD的身份标识作为RDDInfo的parentIds的属性
            val parentIds = rdd.dependencies.map(_.rdd.id)
            val callsiteLongForm = Option(SparkEnv.get)
              .map(_.conf.get(EVENT_LOG_CALLSITE_LONG_FORM))
              .getOrElse(false)
        
            val callSite = if (callsiteLongForm) {
              rdd.creationSite.longForm
            } else {
              rdd.creationSite.shortForm
            }
            //创建RDDInfo对象
            new RDDInfo(rdd.id, rddName, rdd.partitions.length,
              rdd.getStorageLevel, parentIds, callSite, rdd.scope)
          }
        }
        ```
## RDD的五大属性
1. partition(分区)
    1. 每个RDD都包含多个partition，这是RDD中最基本的单位，也是最小的计算粒度，每个partition有一条Task线程去处理，即有多少个分区就会有多少个Task。
    2. 在RDD创建的时候可以指定partition的个数，也可以不指定，那么将会有一个默认值，默认分区个数有 `spark.default.parallelism` 指定，如果没有设置的话，Spark将会执行指定默认值，这个默认值为2
    ```scala
    /**
       * Default min number of partitions for Hadoop RDDs when not given by user
       * Notice that we use math.min so the "defaultMinPartitions" cannot be higher than 2.
       * The reasons for this are discussed in https://github.com/mesos/spark/pull/718
       */
      def defaultMinPartitions: Int = math.min(defaultParallelism, 2)
    ```
   2. 获取分区信息，获取分区集合信息是线程安全的
   ```scala
     /**
      * Implemented by subclasses to return the set of partitions in this RDD. This method will only
      * be called once, so it is safe to implement a time-consuming computation in it.
      * 一个抽象方法，由子类去实现，返回此RDD中的分区集合。这个方法只会被调用一次，因此就算是耗时的操作也是安全的。
      *
      * The partitions in this array must satisfy the following property:
      *   `rdd.partitions.zipWithIndex.forall { case (partition, index) => partition.index == index }`
      */
     protected def getPartitions: Array[Partition]
     /**
      * Get the array of partitions of this RDD, taking into account whether the
      * RDD is checkpointed or not.
      */
     final def partitions: Array[Partition] = {
      checkpointRDD.map(_.partitions).getOrElse {
       if (partitions_ == null) {
          partitions_ = getPartitions
          partitions_.zipWithIndex.foreach { case (partition, index) =>
          require(partition.index == index,
            s"partitions($index).partition == ${partition.index}, but it should equal $index")
          }
        }
        partitions_
      }
    }
    ```
   3. 当RDD处于检查点时，分区信息和依赖信息都会被重写
   ```scala
     // Our dependencies and partitions will be gotten by calling subclass's methods below, and will
     // be overwritten when we're checkpointed
     private var dependencies_ : Seq[Dependency[_]] = _
     @transient private var partitions_ : Array[Partition] = _
    ```
   4. Partition的实现如下
   ```scala
    /**
     * An identifier for a partition in an RDD.
     */
    trait Partition extends Serializable {
      /**
       * Get the partition's index within its parent RDD
       */
      def index: Int
    
      // A better default implementation of HashCode
      override def hashCode(): Int = index
    
      override def equals(other: Any): Boolean = super.equals(other)
    }
    ```
   5. partition和iterator，类似于mapPartitions,foreachPartition等一次操作一个分区的算子，都会使用到iterator方法以取出一整个partition的数据，提高效率，但是需要注意的是，我们取出的数据要小于User Memory的大小，否则会内存溢出
   RDD的 `iterator(split: Partition, context: TaskContext): Iterator[T]` 方法来获取一个partition中的数据的迭代器，有了这个迭代器就能一条一条取出来按compute chain来执行一个转换操作
   ```scala
      /**
       * Internal method to this RDD; will read from cache if applicable, or otherwise compute it.
       * This should ''not'' be called by users directly, but is available for implementors of custom
       * subclasses of RDD.
       */
      final def iterator(split: Partition, context: TaskContext): Iterator[T] = {
        //先判断RDD的storageLevel是否为NONE
        if (storageLevel != StorageLevel.NONE) {
          //若不是，则尝试从缓存中读取，读取不到则通过计算来获取该partition对应的数据的迭代器
          getOrCompute(split, context)
        } else {
          //若是，尝试从checkpoint中获取partition对应的数据迭代器，若checkpoint不存在则通过计算获取
          computeOrReadCheckpoint(split, context)
        }
      }
    ```
2. partitioner(分区器)
    1. RDD的分区方法即分区器，分区方法将数据分配到指定的分区中，目前Spark中有主要的实现是 `org.apache.spark.HashPartitioner` 和 `org.apache.spark.RangePartitioner` 和 `org.apache.spark.sql.execution.CoalescedPartitioner`
    2. 只有K-V形式的RDD才会有分区器，不是K-V形式的RDD的分区器为none，分区器不仅决定了当前分片的个数，同时决定了parent shuffle RDD的输出的个数
    ```scala
    /** Optionally overridden by subclasses to specify how they are partitioned.
        可选，由子类去重写他们的分区器
     */
    @transient val partitioner: Option[Partitioner] = None
    ```
   3. `org.apache.spark.HashPartitioner` 中对于数据的分配实现如下，HashPartitioner是基于Object的hashcode来分区的，所以不应该对集合类型进行哈希分区
   ```scala
      //计算出下游RDD的各个分区
      def getPartition(key: Any): Int = key match {
        case null => 0
        case _ => Utils.nonNegativeMod(key.hashCode, numPartitions)
      }
    /* Calculates 'x' modulo 'mod', takes to consideration sign of x,
     * i.e. if 'x' is negative, than 'x' % 'mod' is negative too
     * so function return (x % mod) + mod in that case.
     */
     def nonNegativeMod(x: Int, mod: Int): Int = {
       val rawMod = x % mod
       //其中nonNegativeMod考虑到了key的符号，如果key是负数，就返回key % numPartitions + numPartitions(补数)
       rawMod + (if (rawMod < 0) mod else 0)
     }
    ```
   4. `org.apache.spark.RangePartitioner` 中对于数据如何分区实现如下，
   ```scala
      def getPartition(key: Any): Int = {
        val k = key.asInstanceOf[K]
        var partition = 0
        if (rangeBounds.length <= 128) {
          // If we have less than 128 partitions naive search
          while (partition < rangeBounds.length && ordering.gt(k, rangeBounds(partition))) {
            partition += 1
          }
        } else {
          // Determine which binary search method to use only once.
          partition = binarySearch(rangeBounds, k)
          // binarySearch either returns the match location or -[insertion point]-1
          if (partition < 0) {
            partition = -partition-1
          }
          if (partition > rangeBounds.length) {
            partition = rangeBounds.length
          }
        }
        if (ascending) {
          partition
        } else {
          rangeBounds.length - partition
        }
      }
    ```
   其中rangeBounds是各个分区上的边界Array。而rangeBounds的具体计算是通过抽样来进行估计的，RangePartitioner是根据key值大小进行分区的，所以支持RDD排序类算子。
3. dependencies(依赖关系)
    1. RDD是如何记录依赖的
    ```scala
       // Our dependencies and partitions will be gotten by calling subclass's methods below, and will
       // be overwritten when we're checkpointed
       //我们的依赖项和分区将通过调用下面的子类方法获得，并且在我们检查点时将被覆盖
       private var dependencies_ : Seq[Dependency[_]] = _
       @transient private var partitions_ : Array[Partition] = _
      /**
       * Get the list of dependencies of this RDD, taking into account whether the
       * RDD is checkpointed or not.
       * 获取此RDD的依赖项列表，同时考虑下一个RDD是否为检查点。
       */
      final def dependencies: Seq[Dependency[_]] = {
        checkpointRDD.map(r => List(new OneToOneDependency(r))).getOrElse {
          if (dependencies_ == null) {
            dependencies_ = getDependencies
          }
          dependencies_
        }
      }
    ```
   2. 宽窄依赖具体参见#宽依赖和窄依赖
4. compute(计算)
    1. 当调用org.apache.spark.rdd.RDD.iterator方法无法从缓存或checkpoint中获取指定partition的迭代器时，就会调用compute去计算获取
    2. 每个RDD都会实现compute方法，compute都会和迭代器(RDD之间转换的迭代器)进行复合，这样就不需要保存compute每次计算的结果
    ```scala
      /**
       * Implemented by subclasses to compute a given partition.
       * 由子类去实现，去计算给定的分区的数据
       * 分区中split: Partition装的就是实际需要计算的数据
       */
      @DeveloperApi
      def compute(split: Partition, context: TaskContext): Iterator[T]
    ```
   3. 举例
        1. map
        ```scala
        private var dependencies_ : Seq[Dependency[_]] = _
        /**
         * Get the list of dependencies of this RDD, taking into account whether the
         * RDD is checkpointed or not.
         */
        final def dependencies: Seq[Dependency[_]] = {
            checkpointRDD.map(r => List(new OneToOneDependency(r))).getOrElse {
              if (dependencies_ == null) {
                dependencies_ = getDependencies
              }
              dependencies_
            }
        }
        /** Returns the first parent RDD */
        protected[spark] def firstParent[U: ClassTag]: RDD[U] = {
          //返回dependencies_中的第一个依赖的RDD
          dependencies.head.rdd.asInstanceOf[RDD[U]]
        }
        private[spark] class MapPartitionsRDD[U: ClassTag, T: ClassTag](
            var prev: RDD[T],
            f: (TaskContext, Int, Iterator[T]) => Iterator[U],  // (TaskContext, partition index, iterator)
            preservesPartitioning: Boolean = false,
            isFromBarrier: Boolean = false,
            isOrderSensitive: Boolean = false)
          extends RDD[U](prev) {
          override val partitioner = if (preservesPartitioning) firstParent[T].partitioner else None
          override def getPartitions: Array[Partition] = firstParent[T].partitions
          //f->是我们自定义的算子函数
          //split: Partition->split中会存放我们需要计算的真正的数据
          //firstParent->是指本RDD依赖的private var dependencies_ : Seq[Dependency[_]] = _中的第一个
          override def compute(split: Partition, context: TaskContext): Iterator[U] =
            f(context, split.index, firstParent[T].iterator(split, context))
        
          override def clearDependencies() {
            super.clearDependencies()
            prev = null
          }
        
          @transient protected lazy override val isBarrier_ : Boolean =
            isFromBarrier || dependencies.exists(_.rdd.isBarrier())
        
          override protected def getOutputDeterministicLevel = {
            if (isOrderSensitive && prev.outputDeterministicLevel == DeterministicLevel.UNORDERED) {
              DeterministicLevel.INDETERMINATE
            } else {
              super.getOutputDeterministicLevel
            }
          }
        }
        ```
      2. groupByKey：与map、union不同，groupByKey产生的是宽依赖(ShuffleDependency)的transformation，其最终生成的是 `org.apache.spark.rdd.ShuffledRDD` ，来看看其最终实现：
      ```scala
        override def compute(split: Partition, context: TaskContext): Iterator[(K, C)] = {
          val dep = dependencies.head.asInstanceOf[ShuffleDependency[K, V, C]]
          //ShuffledRDD的compute使用shuffleManager来获取一个reader，该reader将从本地或远程拉取map outer的file数据
          SparkEnv.get.shuffleManager.getReader(dep.shuffleHandle, split.index, split.index + 1, context)
            .read()
            .asInstanceOf[Iterator[(K, C)]]
        }  
      ```
      
5. preferLocations(优先分配节点列表)
    1. 对于分区而言，返回数据本地化的计算列表，也就是说，每个RDD会持有一个列表(Seq)，而这个列表保存着分片有限分配给哪个worker节点计算，spark遵循着计算向数据靠拢的原则，也就是尽量在存有数据的节点上进行计算。
    2. 要注意，并不是每个RDD都会有prefer locations，比如从Java或Scala集合中创建的RDD就没有，从HDFS中读取文件生成的RDD就有prefer location
    ```scala
      /**
       * Get the preferred locations of a partition, taking into account whether the
       * RDD is checkpointed.
       * 获取分区的首先位置，同时考虑RDD是否为检查点
       */
      final def preferredLocations(split: Partition): Seq[String] = {
        checkpointRDD.map(_.getPreferredLocations(split)).getOrElse {
          getPreferredLocations(split)
        }
      }
    ```
## SparkConf
SparkConf是SparkContext初始化的必要前提，了解了SparkConf对于了解SparkContext会有很大的帮助。
### 1.主要构造方法
```scala
class SparkConf(loadDefaults: Boolean) extends Cloneable with Logging with Serializable {
    //从SparkConf的伴生对象导入，它们主要管理过期的、旧版本的配置项，以及日志输出
    import SparkConf._
    //采用ConcurrentHasMap来存放所有的配置项，保证多线程环境下的线程安全和效率问题
    private val settings = new ConcurrentHashMap[String, String]()
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
}
```
SparkConf中的主构造函数参数 `loadDefaults` 它指示是否要从Java的系统属性(即System.getProperties()和-Dkey=value指定的参数)中加载Spark相关的属性，默认是true即从Java的系统属性中获取Spark的属性。
### 2.设置配置项
* 如果要配置Spark的配置项，有以下三种方法
    1. 直接使用 `set` 方法，这是我们最常使用的方法。SparkConf中提供了多种set方法，最基础的set方法重载如下
    ```scala
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
    可见SparkConf中的key和value都不能为 `null` 。并且每次set之后都返回 `this` 对象，所以支持链式调用，另外还有一些快速设置配置项的方法，如 `setMaster()` 和 `setAppName()` 它们最终都会调用 `set` 方法。
    ```scala
      /**
       * The master URL to connect to, such as "local" to run locally with one thread, "local[4]" to
       * run locally with 4 cores, or "spark://master:7077" to run on a Spark standalone cluster.
       */
      def setMaster(master: String): SparkConf = {
        set("spark.master", master)
      }
    
      /** Set a name for your application. Shown in the Spark web UI. */
      def setAppName(name: String): SparkConf = {
        set("spark.app.name", name)
      }
    ```
    2. 通过系统属性加载Spark属性，通过主构造器我们知道SparkConf会默认从Java的系统属性中加载Spark属性
    ```scala
      private[spark] def loadFromSystemProperties(silent: Boolean): SparkConf = {
        // Load any spark.* system properties
        for ((key, value) <- Utils.getSystemProperties if key.startsWith("spark.")) {
          set(key, value, silent)
        }
        this
      }
    ```
    如果我们直接 `new SparkConf()` 的话，将会调用 `def this()=this(true)` 默认从Java的系统属性中加载以 `spark.` 开头的属性。
    3. clone SparkConf，SparkConf类继承了 `scala.Cloneable` 特质，并覆写了 `clone()` 方法，因此SparkConf是可以(深)克隆的。
    ```scala
      /** Copy this object */
      override def clone: SparkConf = {
        val cloned = new SparkConf(false)
        settings.entrySet().asScala.foreach { e =>
          cloned.set(e.getKey(), e.getValue(), true)
        }
        cloned
      }
    ```
    虽然ConcurrentHashMap保证的线程安全，不会影响SparkConfs实例共享，但在高并发的情况下，锁机制还是会带来性能的损耗，我们可以把SparkConf克隆到多个组件中，以便让他们获得相同的配享。
### 3.获取配置项
获得配置项只有一个途径，即调用 `get` 方法。
```scala
  /** Get a parameter; throws a NoSuchElementException if it's not set */
  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }
  /** Get a parameter, falling back to a default if not set */
  def get(key: String, defaultValue: String): String = {
    getOption(key).getOrElse(defaultValue)
  }
  /** Get a parameter as an Option */
  def getOption(key: String): Option[String] = {
    Option(settings.get(key)).orElse(getDeprecatedConfig(key, settings))
  }
```
在获取配置项时，同时会检查过期配置(getDeprecatedConfig方法是在伴生对象中定义的)，并使用Scala的Option包装返回的接口，对于有值(scala.Some)和无值(scala.None)的情况可以灵活处理。
### 4.校验配置项
SparkConf中有一个校验配置项的方法 `org.apache.spark.SparkConf.validateSettings` 主要对过期配置配置的告警，以及对非法设置或不兼容的配置项抛出异常(由于validateSettings方法源码过长但是逻辑比较简单，这里就不上代码了)
## Spark天堂之门--SparkContext
### 1.概述
SparkContext会伴随着整个Spark程序的生命周期，SparkContext是Spark程序通往集群的的唯一通道，是程序的起点也是程序的终点，SparkContext会创建三大核心对象TaskSchedulerImpl,DAGScheduler和SchedulerBackend。
### 2.什么是Spark的天堂之门
1. Spark程序在运行时分为Driver和Executor两部分
2. Spark编程是基于SparkContext的
    1. Spark的核心基础是RDD，整个程序的第一个RDD是由SparkContext来创建的，
    2. Spark的程序调度优化也是基于SparkContext的，首先进行调度优化
3. Spark程序的注册是通过SparkContext实例化时产生的对象来完成的(其实是SchedulerBackend来完成的)
4. Spark程序在运行时，需要通过ClusterManager获取具体的计算资源，计算资源也是通过SparkContext所产生的对象来申请的(其实时SchedulerBackend来获取计算资源的)
5. SparkContext崩溃或结束的时候整个Spark程序也就结束了
### 3.SparkContext解析
#### 1.SparkContext构造函数
SparkContext接收SparkConf作为类构造器参数，并且有多种辅助构造器方法的实现。
```scala
class SparkContext(config: SparkConf) extends Logging {
  /**
   * Create a SparkContext that loads settings from system properties (for instance, when
   * launching with ./bin/spark-submit).
   */
  def this() = this(new SparkConf())
  def this(master: String, appName: String, conf: SparkConf) = this(SparkContext.updatedConf(conf, master, appName))
  def this(
      master: String,
      appName: String,
      sparkHome: String = null,
      jars: Seq[String] = Nil,
      environment: Map[String, String] = Map()) = {
    this(SparkContext.updatedConf(new SparkConf(), master, appName, sparkHome, jars, environment))
  }
  private[spark] def this(master: String, appName: String) = this(master, appName, null, Nil, Map())
  private[spark] def this(master: String, appName: String, sparkHome: String) = this(master, appName, sparkHome, Nil, Map())
  private[spark] def this(master: String, appName: String, sparkHome: String, jars: Seq[String]) = this(master, appName, sparkHome, jars, Map())
}
```
主构造器在一个巨大的 `try-catch` 中，其中包含了很多组件的初始化逻辑.
#### 2.SparkContext初始化组件
在上述的 `try-catch` 代码块的上方，SparkContext预先声明了一批私有变量字段，且定义了获取该值的 `getter` 方法。它们用于维护SparkContext需要初始化的所有组件的内部状态。
```scala
private var _conf: SparkConf = _
private var _eventLogDir: Option[URI] = None
private var _eventLogCodec: Option[String] = None
private var _listenerBus: LiveListenerBus = _
private var _env: SparkEnv = _
private var _statusTracker: SparkStatusTracker = _
private var _progressBar: Option[ConsoleProgressBar] = None
private var _ui: Option[SparkUI] = None
private var _hadoopConfiguration: Configuration = _
private var _executorMemory: Int = _
private var _schedulerBackend: SchedulerBackend = _
private var _taskScheduler: TaskScheduler = _
private var _heartbeatReceiver: RpcEndpointRef = _
@volatile private var _dagScheduler: DAGScheduler = _
private var _applicationId: String = _
private var _applicationAttemptId: Option[String] = None
private var _eventLogger: Option[EventLoggingListener] = None
private var _executorAllocationManager: Option[ExecutorAllocationManager] = None
private var _cleaner: Option[ContextCleaner] = None
private var _listenerBusStarted: Boolean = false
private var _jars: Seq[String] = _
private var _files: Seq[String] = _
private var _shutdownHookRef: AnyRef = _
private var _statusStore: AppStatusStore = _
//初始化组件，即SparkContext的实际初始化顺序
private[spark] def conf: SparkConf = _conf
private[spark] def listenerBus: LiveListenerBus = _listenerBus
private[spark] def env: SparkEnv = _env
def statusTracker: SparkStatusTracker = _statusTracker
private[spark] def progressBar: Option[ConsoleProgressBar] = _progressBar
private[spark] def ui: Option[SparkUI] = _ui
def hadoopConfiguration: Configuration = _hadoopConfiguration
private[spark] def schedulerBackend: SchedulerBackend = _schedulerBackend
private[spark] def taskScheduler: TaskScheduler = _taskScheduler
private[spark] def taskScheduler_=(ts: TaskScheduler): Unit = {
  _taskScheduler = ts
}
private[spark] def dagScheduler: DAGScheduler = _dagScheduler
private[spark] def dagScheduler_=(ds: DAGScheduler): Unit = {
  _dagScheduler = ds
}
private[spark] def eventLogger: Option[EventLoggingListener] = _eventLogger
private[spark] def executorAllocationManager: Option[ExecutorAllocationManager] = _executorAllocationManager
private[spark] def cleaner: Option[ContextCleaner] = _cleaner
private[spark] def  : AppStatusStore = _statusStore
```
##### org.apache.spark.SparkConf
它其实算不上是SparkContext的一个初始化组件，因为它是构造SparkContext的时候传进来的。SparkContext会将传入的SparkConf克隆一份副本，
之后会在副本上做校验(主要是AppName和Master的校验)，及添加其他必要参数(Driver地址、应用ID等)。这样用户就不可以在修改配置项，
以保证SparkConf在运行期的不变性。
##### org.apache.spark.scheduler.LiveListenerBus
它是SparkContext中的事件总线，它异步的将事件源产生的事件( `org.apache.spark.scheduler.SparkListenerEvent` )投递给已经注册的监听器( `org.apache.spark.scheduler.SparkListener` ).Spark中广泛运用监听器模式，以适应集群状态下的分布式事件汇报。<br/>
除了LiveListenerBus之外，Spark中还有很多事件总线，它们都继承自 `org.apache.spark.util.ListenerBus` 特质，事件总线式Spark底层重要的支撑组件。<br/>
LiveListenerBus的初始化
```scala
_listenerBus = new LiveListenerBus(_conf)
```
##### org.apache.spark.status.AppStatusStore
它式提供Spark程序运行中各项监控指标的键值对化存储。Web UI中见到的数据指标基本都在存在这里。
对AppStatusStore的初始化
```scala
// Initialize the app status store and listener before SparkEnv is created so that it gets
// all events.
_statusStore = AppStatusStore.createLiveStore(conf)
listenerBus.addToStatusQueue(_statusStore.listener.get)

//org.apache.spark.status.AppStatusStore.createLiveStore方法
def createLiveStore(conf: SparkConf): AppStatusStore = {
    val store = new ElementTrackingStore(new InMemoryStore(), conf)
    val listener = new AppStatusListener(store, conf, true)
    new AppStatusStore(store, listener = Some(listener))
}
```
可见AppStatusStore底层使用了ElementTrackingStore，它是能够跟踪元素及其数量的键值对存储结构，因此适用于监控。<br/>
另外还会产生一个监听器AppStatusListener实例，并注册到LiveListenerBus上，用来收集监控数据。
##### org.apache.spark.SparkEnv
SparkEnv是Spark的执行环境，Driver和Executor的运行都需要SparkEnv提供的各类组件形成的环境来作为基础。其初始化代码如下：
```scala
// Create the Spark execution environment (cache, map output tracker, etc)
_env = createSparkEnv(_conf, isLocal, listenerBus)
//在创建Driver环境创建完毕之后，会调用SparkEnv伴生对象的set()方法保存它，这样就一处创建多处使用了。
SparkEnv.set(_env)
//org.apache.spark.SparkContext.createSparkEnv方法如下
// This function allows components created by SparkEnv to be mocked in unit tests:
private[spark] def createSparkEnv(
  conf: SparkConf,
  isLocal: Boolean,
  listenerBus: LiveListenerBus): SparkEnv = {
SparkEnv.createDriverEnv(conf, isLocal, listenerBus, SparkContext.numDriverCores(master, conf))
}
```
可见SparkEnv的创建依赖于LiveListenerBus，并且在SparkContext初始化时只会创建Driver的执行环境，Executor的执行环境创建就是后话了。
SparkEnv中包含了很多的组件，例如安全管理器SecurityManager,serializerManager、RpcEnv、块存储管理器BlockManager、mapOutputTracker，监控度量系统MetricsSystem等。<br/>
SparkContext构造方法的后方，就会藉由SparkEnv先初始化BlockManager与启动MetricSystem，代码如下：
```scala
_env.blockManager.initialize(_applicationId)
// The metrics system for Driver need to be set spark.app.id to app ID.
// So it should start after we get app ID from the task scheduler and set spark.app.id.
_env.metricsSystem.start()
// Attach the driver metrics servlet handler to the web ui after the metrics system is started.
_env.metricsSystem.getServletHandlers.foreach(handler => ui.foreach(_.attachHandler(handler)))
```
##### org.apache.spark.SparkStatusTracker
SparkStatusTracker提供报告最近作业执行情况的低级API。它的内部只有6个方法，从AppStatusStore中查询并返回诸如Job/Stage ID、活跃/完成/失败的Task数、Executor内存用量等基础数据。它只能保证非常弱的一致性语义，也就是说它报告的信息会有延迟或缺漏
##### org.apache.spark.ui.ConsoleProgressBar
ConsoleProgressBar按行打印Stage的计算进度。它周期性地从AppStatusStore中查询Stage对应的各状态的Task数，并格式化成字符串输出。它可以通过 `spark.ui.showConsoleProgress` 参数控制开关，默认值false。由于SparkStatusTrack存在一致性问题，所以ConsoleProgressBar的显示会有延时
##### org.apache.spark.ui.SparkUI
SparkUI维护监控数据在Spark Web UI界面的展示。SparkUI间接依赖于计算引擎、调度系统、存储体系、作业(Job)、阶段(Stage)、存储、执行器(Executor)等组件的监控数据都会以SparkListenerEvent的形式投递到LiveListenerBus中，SparkUI从各个SparkListener中读取数据并显示到web页面上
SparkUI的初始化代码如下：
```scala
_ui =
      if (conf.getBoolean("spark.ui.enabled", true)) {
        Some(SparkUI.create(Some(this), _statusStore, _conf, _env.securityManager, appName, "",
          startTime))
      } else {
        // For tests, do not enable the UI
        None
      }
// Bind the UI before starting the task scheduler to communicate
// the bound port to the cluster manager properly
_ui.foreach(_.bind())
```
可以通过 `spark.ui.enabled` 来控制是否启动SparkUI，其默认值为true，然后调用SparkUI的父类 `org.apache.spark.ui.WebUI.WebUI` 的 `bind()` 方法，将SparkUI绑定到特定的host:port上。
##### org.apache.hadoop.conf.Configuration
SparkContext会借助工具类SparkHadoopUtil初始化一些与Hadoop有关的配置，存放在Hadoop的Configuration实例中，如Amazon S3相关的配置，和以“spark.hadoop.”为前缀的Spark配置参数。
##### org.apache.spark.HeartbeatReceiver
所有执行的Executor都会向HeartbeatReceiver发送心跳通知，HeartbeatReceiver接收到Executor的心跳之后，首先更新Executor的最后可见时间，然后将此信息交由TaskScheduler做进一步处理，它本质上也是个监听器，继承了SparkListener。其初始化代码如下：
```scala
// We need to register "HeartbeatReceiver" before "createTaskScheduler" because Executor will
// retrieve "HeartbeatReceiver" in the constructor. (SPARK-6640)
_heartbeatReceiver = env.rpcEnv.setupEndpoint(HeartbeatReceiver.ENDPOINT_NAME, new HeartbeatReceiver(this))
```
HeartbeatReceiver通过RpcEnv最终包装成了一个RPC端点的引用即RpcEndpointRef，Spark集群的节点之间必然会有大量的网络通讯，心跳机制只是其中的一部分。因此Rpc框架同事件总线一样不可或缺。
##### org.apache.spark.scheduler.SchedulerBackend
SchedulerBackend负责向等待计算的Task分配计算资源，并在Executor上启动Task。它是一个Scala特征，有多种部署模式下的SchedulerBackend实现类。它在SparkContext中是和TaskScheduler一起初始化的，作为一个元组返回。其是初始化代码和TaskScheduler一并展现。
##### org.apache.spark.scheduler.TaskSchedulerImpl
TaskScheduler即任务调度器。它也是一个Scala特征，但只有一种实现，即TaskSchedulerImpl类。它负责提供Task的调度算法，并且会持有SchedulerBackend的实例，通过SchedulerBackend发挥作用。<br/>
TaskScheduler按照调度算法对集群管理器已经分配给应用的资源进行二次调度后分配给任务。TaskScheduler调度的Task是由DAGScheduler创建的，所以DAGScheduler是TaskScheduler的前置调度。
它们两个的初始化代码如下。
```scala
val (sched, ts) = SparkContext.createTaskScheduler(this, master, deployMode)
_schedulerBackend = sched
_taskScheduler = ts
/**
* 方法比较长，它包括有三种本地模式、本地集群模式、Standalone模式，以及第三方集群管理器（如YARN）提供的模式。
* Create a task scheduler based on a given master URL.
* Return a 2-tuple of the scheduler backend and the task scheduler.
*/
private def createTaskScheduler(
  sc: SparkContext,
  master: String,
  deployMode: String): (SchedulerBackend, TaskScheduler) = {
    import SparkMasterRegex._
    
    // When running locally, don't try to re-execute tasks on failure.
    val MAX_LOCAL_TASK_FAILURES = 1
    
    master match {
      case "local" =>
        val scheduler = new TaskSchedulerImpl(sc, MAX_LOCAL_TASK_FAILURES, isLocal = true)
        val backend = new LocalSchedulerBackend(sc.getConf, scheduler, 1)
        scheduler.initialize(backend)
        (backend, scheduler)
    
      case LOCAL_N_REGEX(threads) =>
        def localCpuCount: Int = Runtime.getRuntime.availableProcessors()
        // local[*] estimates the number of cores on the machine; local[N] uses exactly N threads.
        val threadCount = if (threads == "*") localCpuCount else threads.toInt
        if (threadCount <= 0) {
          throw new SparkException(s"Asked to run locally with $threadCount threads")
        }
        val scheduler = new TaskSchedulerImpl(sc, MAX_LOCAL_TASK_FAILURES, isLocal = true)
        val backend = new LocalSchedulerBackend(sc.getConf, scheduler, threadCount)
        scheduler.initialize(backend)
        (backend, scheduler)
    
      case LOCAL_N_FAILURES_REGEX(threads, maxFailures) =>
        def localCpuCount: Int = Runtime.getRuntime.availableProcessors()
        // local[*, M] means the number of cores on the computer with M failures
        // local[N, M] means exactly N threads with M failures
        val threadCount = if (threads == "*") localCpuCount else threads.toInt
        val scheduler = new TaskSchedulerImpl(sc, maxFailures.toInt, isLocal = true)
        val backend = new LocalSchedulerBackend(sc.getConf, scheduler, threadCount)
        scheduler.initialize(backend)
        (backend, scheduler)
    
      case SPARK_REGEX(sparkUrl) =>
        val scheduler = new TaskSchedulerImpl(sc)
        val masterUrls = sparkUrl.split(",").map("spark://" + _)
        val backend = new StandaloneSchedulerBackend(scheduler, sc, masterUrls)
        scheduler.initialize(backend)
        (backend, scheduler)
    
      case LOCAL_CLUSTER_REGEX(numSlaves, coresPerSlave, memoryPerSlave) =>
        // Check to make sure memory requested <= memoryPerSlave. Otherwise Spark will just hang.
        val memoryPerSlaveInt = memoryPerSlave.toInt
        if (sc.executorMemory > memoryPerSlaveInt) {
          throw new SparkException("Asked to launch cluster with %d MB RAM / worker but requested %d MB/worker".format(memoryPerSlaveInt, sc.executorMemory))
        }
    
        val scheduler = new TaskSchedulerImpl(sc)
        val localCluster = new LocalSparkCluster(numSlaves.toInt, coresPerSlave.toInt, memoryPerSlaveInt, sc.conf)
        val masterUrls = localCluster.start()
        val backend = new StandaloneSchedulerBackend(scheduler, sc, masterUrls)
        scheduler.initialize(backend)
        backend.shutdownCallback = (backend: StandaloneSchedulerBackend) => {
          localCluster.stop()
        }
        (backend, scheduler)
    
      case masterUrl =>
        val cm = getClusterManager(masterUrl) match {
          case Some(clusterMgr) => clusterMgr
          case None => throw new SparkException("Could not parse Master URL: '" + master + "'")
        }
        try {
          val scheduler = cm.createTaskScheduler(sc, masterUrl)
          val backend = cm.createSchedulerBackend(sc, masterUrl, scheduler)
          cm.initialize(scheduler, backend)
          (backend, scheduler)
        } catch {
          case se: SparkException => throw se
          case NonFatal(e) =>
            throw new SparkException("External scheduler cannot be instantiated", e)
        }
    }
}
```
##### org.apache.spark.scheduler.DAGScheduler
DAGScheduler即有向无环图（DAG）调度器。<br/>
用来表示RDD之间的血缘。<br/>
DAGScheduler负责生成并提交Job，以及按照DAG将RDD和算子划分并提交Stage(按照宽依赖来划分Stage)。<br/>
每个Stage都包含一组Task，称为TaskSet，它们被传递给TaskScheduler。也就是说DAGScheduler需要先于TaskScheduler进行调度。<br/>
DAGScheduler初始化是直接new出来的，但在其构造方法里也会将SparkContext中TaskScheduler的引用传进去。因此要等DAGScheduler创建后，再真正启动TaskScheduler。其初始化代码如下：
```scala
_dagScheduler = new DAGScheduler(this)
// start TaskScheduler after taskScheduler sets DAGScheduler reference in DAGScheduler's constructor
_taskScheduler.start()
```
SchedulerBackend、TaskScheduler与DAGScheduler是Spark调度逻辑的主要组成部分。
##### org.apache.spark.scheduler.EventLoggingListener
EventLoggingListener是用于事件持久化的监听器。它可以通过 `spark.eventLog.enabled` 参数控制开关，默认值false。如果开启，它也会注册到LiveListenerBus里，并将特定的一部分事件写到磁盘。
##### org.apache.spark.ExecutorAllocationManager
ExecutorAllocationManager即Executor动态分配管理器。顾名思义，可以根据工作负载动态调整Executor的数量。在配置 `spark.dynamicallocation.enable` 为true的前提下，在非local模式下或者当 `spark.dynanicAllocation.testing` 属性为true时启用。<br/>
如果开启，并且SchedulerBackend的实现类支持这种机制，Spark就会根据程序运行时的负载动态增减Executor的数量。
其初始化代码如下：
```scala
// Optionally scale number of executors dynamically based on workload. Exposed for testing.
val dynamicAllocationEnabled = Utils.isDynamicAllocationEnabled(_conf)
_executorAllocationManager =
  if (dynamicAllocationEnabled) {
    schedulerBackend match {
      case b: ExecutorAllocationClient =>
        Some(new ExecutorAllocationManager(schedulerBackend.asInstanceOf[ExecutorAllocationClient], listenerBus, _conf,env.blockManager.master))
      case _ =>
        None
    }
  } else {
    None
  }
_executorAllocationManager.foreach(_.start())
```
##### org.apache.spark.ContextCleaner
上下文清理器。ContextCleaner实际采用异步的方式清理那些超出俎作用域范围的RDD、ShuffleDependency和Broadcast等信息。<br/>
它可以通过spark.cleaner.referenceTracking参数控制开关，默认值true。它内部维护着对RDD、Shuffle依赖和广播变量（之后会提到）的弱引用，如果弱引用的对象超出程序的作用域，就异步地将它们清理掉。
#### 3.SparkContext的辅助属性
##### creationSite
creationSie用来描述了SparkContext在那里被创造了。它的数据类型是 `org.apache.spark.util.CallSite` 他的数据结构很简单，只有shirtForm和longForm这两个属性，用来描述代码的位置。其初始化代码如下：
```scala
// The call site where this SparkContext was constructed.
private val creationSite: CallSite = Utils.getCallSite()
```
其中 `Utils.getCallSite()` 方法会遍历线程栈中，找到最后一个(靠近栈顶的)Spark方法调用，与最先一个(靠近栈底的)用户用户方法调用，把它们的完整描述和简短描述包装在CallSite中返回。
##### allowMultipleContexts
它指示一个JVM(即一个Application)中是否允许运行多个SparkContext实例。
初始化代码如下：
```scala
private val allowMultipleContexts: Boolean = config.getBoolean("spark.driver.allowMultipleContexts", false)
```
它由 `spark.driver.allowMultipleContexts` 参数来控制，默认为 `false` 不允许一个JVM中有多个SparkContext实例。如果设置为 `true` 那么如果有多个SparkContext实例的话则会发出警告。
##### startTime & stopped
startTime指示SparkContext启动时的时间戳。stopped则指示SparkContext是否停止，它采用AtomicBoolean类型保证更新状态的原子性。其初始化代码如下：
```scala
val startTime = System.currentTimeMillis()
//使用AtomicBoolean保证原子性
private[spark] val stopped: AtomicBoolean = new AtomicBoolean(false)
```
##### addedFiles/addedJars & _files/_jars
Spark支持提交应用时，附带用户自定义的其他文件和jar包，addedFiles和addedJars是两个ConcurrentHashMap，用来维护用户自定义以及jar包的URL路径和它们被加入到ConcurrentHashMap的时间戳。其代码如下：
```scala
// Used to store a URL for each static file/jar together with the file's local timestamp
private[spark] val addedFiles = new ConcurrentHashMap[String, Long]().asScala
private[spark] val addedJars = new ConcurrentHashMap[String, Long]().asScala
```
_files和_jars则接收Spark配置中定义的文件或jar包路径，获取文件的逻辑相同。其代码如下：
```scala
_jars = Utils.getUserJars(_conf)
/**
* Return the jar files pointed by the "spark.jars" property. Spark internally will distribute
* these jars through file server. In the YARN mode, it will return an empty list, since YARN
* has its own mechanism to distribute jars.
*/
def getUserJars(conf: SparkConf): Seq[String] = {
    val sparkJars = conf.getOption("spark.jars")
    sparkJars.map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
}
_files = _conf.getOption("spark.files").map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
```
可以看出_files和_jars分别接收 `spark.files`( `--files` ) 和 `spark.jars`( `--jars` ) 配置的参数，首先使用 `Utils.getUserJars` 获取SparkConf中 `spark.jars` 和 `spark.jars` 配置信息，然后分别调用添加方法，对于jar包则是调用 `org.apache.spark.SparkContext.addJar` 方法。
```scala
/**
* Adds a JAR dependency for all tasks to be executed on this `SparkContext` in the future.
*
* If a jar is added during execution, it will not be available until the next TaskSet starts.
*
* @param path can be either a local file, a file in HDFS (or other Hadoop-supported filesystems),
* an HTTP, HTTPS or FTP URI, or local:/path for a file on every worker node.
*
* @note A path can be added only once. Subsequent additions of the same path are ignored.
*/
def addJar(path: String) {
    def addJarFile(file: File): String = {
      try {
        if (!file.exists()) {
          throw new FileNotFoundException(s"Jar ${file.getAbsolutePath} not found")
        }
        if (file.isDirectory) {
          throw new IllegalArgumentException(
            s"Directory ${file.getAbsoluteFile} is not allowed for addJar")
        }
        //添加到RPC环境中
        env.rpcEnv.fileServer.addJar(file)
      } catch {
        case NonFatal(e) =>
          logError(s"Failed to add $path to Spark environment", e)
          null
      }
    }
    if (path == null) {
      logWarning("null specified as parameter to addJar")
    } else {
      val key = if (path.contains("\\")) {
        // For local paths with backslashes on Windows, URI throws an exception
        addJarFile(new File(path))
      } else {
        val uri = new URI(path)
        // SPARK-17650: Make sure this is a valid URL before adding it to the list of dependencies
        Utils.validateURL(uri)
        uri.getScheme match {
          // A JAR file which exists only on the driver node
          case null =>
            // SPARK-22585 path without schema is not url encoded
            addJarFile(new File(uri.getRawPath))
          // A JAR file which exists only on the driver node
          case "file" => addJarFile(new File(uri.getPath))
          // A JAR file which exists locally on every worker node
          case "local" => "file:" + uri.getPath
          case _ => path
        }
      }
      if (key != null) {
        val timestamp = System.currentTimeMillis
        if (addedJars.putIfAbsent(key, timestamp).isEmpty) {
          logInfo(s"Added JAR $path at $key with timestamp $timestamp")
          postEnvironmentUpdate()
        } else {
          logWarning(s"The jar $path has been added already. Overwriting of added jars " +
            "is not supported in the current version.")
        }
      }
    }
}
```
addJar()方法检查JAR包路径的合法性和类型，然后调用RpcEnv中的RpcEnvFileServer.addJar()方法，将JAR包加进RPC环境中。在该方法的最后还调用了postEnvironmentUpdate()，用来更新执行环境。
##### persistentRdds
Spark支持RDD的持久化，可以持久化到磁盘或内存。persistentRdds维护的是持久化RDD的ID与其弱引用的映射关系。其初始化代码如下：
```scala
private[spark] val persistentRdds = {
    val map: ConcurrentMap[Int, RDD[_]] = new MapMaker().weakValues().makeMap[Int, RDD[_]]()
    map.asScala
}
```
通过RDD自带的 `cache()/persist()/unpersist()` 方法可以持久化与反持久化一个RDD，它们最终调用了 `org.apache.spark.SparkContext.persistRDD(rdd: RDD[_])` 和 `org.apache.spark.SparkContext.unpersistRDD(rddId: Int, blocking: Boolean = true)` 内部代码，其带啊吗如下：
```scala
  /**
   * Register an RDD to be persisted in memory and/or disk storage
   */
  private[spark] def persistRDD(rdd: RDD[_]) {
    persistentRdds(rdd.id) = rdd
  }

  /**
   * Unpersist an RDD from memory and/or disk storage
   */
  private[spark] def unpersistRDD(rddId: Int, blocking: Boolean = true) {
    env.blockManager.master.removeRdd(rddId, blocking)
    persistentRdds.remove(rddId)
    listenerBus.post(SparkListenerUnpersistRDD(rddId))
  }
```
##### executorEnvs & _executorMemory & _sparkUser
executorEnvs是一个HashMap，用来存储需要传递给Executor的环境变量，executorEnvs中存储的变量将传递给执行任务的Executor使用。_executorMemory与_sparkUser就是其中之二，分别代表Executor内存大小和当前启动SparkContext的用户名。其初始化代码如下：
```scala
private[spark] val executorEnvs = HashMap[String, String]()
//Executor内存可以通过spark.executor.memory配置项、SPARK_EXECUTOR_MEMORY环境变量、SPARK_MEM环境变量指定，优先级依次降低，且默认大小是1GB1024MB。
_executorMemory = _conf.getOption("spark.executor.memory")
                       .orElse(Option(System.getenv("SPARK_EXECUTOR_MEMORY")))
                       .orElse(Option(System.getenv("SPARK_MEM"))
                       .map(warnSparkMem))
                       .map(Utils.memoryStringToMb)
                       .getOrElse(1024)
val sparkUser = Utils.getCurrentUserName()
//executorEnvs也会存储_executorMemory和_sparkUser
executorEnvs("SPARK_EXECUTOR_MEMORY") = executorMemory + "m"
executorEnvs ++= _conf.getExecutorEnv
executorEnvs("SPARK_USER") = sparkUser
```
##### checkpointDir
checkpointDir指定集群状态下，RDD检查点在HDFS上的保存路径。检查点的存在是为了当计算过程中出错时，能够快速恢复，而不必从头计算。SparkContext提供 `setCheckpointDir()` 方法来设定检查点。代码如下：
```scala
  private[spark] var checkpointDir: Option[String] = None
  /**
   * Set the directory under which RDDs are going to be checkpointed.
   * @param directory path to the directory where checkpoint files will be stored
   * (must be HDFS path if running in cluster)
   */
  def setCheckpointDir(directory: String) {

    // If we are running on a cluster, log a warning if the directory is local.
    // Otherwise, the driver may attempt to reconstruct the checkpointed RDD from
    // its own local file system, which is incorrect because the checkpoint files
    // are actually on the executor machines.
    if (!isLocal && Utils.nonLocalPaths(directory).isEmpty) {
      logWarning("Spark is not running in local mode, therefore the checkpoint directory " +
        s"must not be on the local filesystem. Directory '$directory' " +
        "appears to be on the local filesystem.")
    }

    checkpointDir = Option(directory).map { dir =>
      val path = new Path(dir, UUID.randomUUID().toString)
      val fs = path.getFileSystem(hadoopConfiguration)
      fs.mkdirs(path)
      fs.getFileStatus(path).getPath.toString
    }
  }
```
##### localProperties
localProperties用于维护一个Properties数据类型的线程本地变量。它是InheritableThreadLocal类型，继承自ThreadLocal，在后者的基础上允许本地变量从父线程到子线程的继承，也就是该Properties会沿着线程栈传递下去。
##### _eventLogDir & _eventLogCodec
这两个属性与EventLoggingListener相关。<br/>
EventLoggingListener打开时，事件日志会写入_eventLogDir指定的目录，可以用 `spark.eventLog.dir` 参数设置。<br/>
_eventLogCodec指定事件日志的压缩算法，当通过 `spark.eventLog.compress` 参数启用压缩后，就根据 `spark.io.compression.codec` 参数配置压缩算法，目前支持lz4、lzf、snappy、zstd四种。
##### _applicationId & _applicationAttemptId
这两个ID都是TaskScheduler初始化完毕并启动之后才分配的。TaskScheduler启动之后，应用代码的逻辑才真正被执行，并且可能会进行多次尝试。在SparkUI、BlockManager和EventLoggingListener初始化时，也会用到它们。其初始化代码如下：
```scala
_applicationId = _taskScheduler.applicationId()
_applicationAttemptId = taskScheduler.applicationAttemptId()
```
##### _shutdownHookRef
它用来定义SparkContext的关闭钩子，主要是在JVM退出时，显式地执行SparkContext.stop()方法，以防止用户忘记而留下烂摊子。初始化代码如下：
```scala
_shutdownHookRef = ShutdownHookManager.addShutdownHook(
  ShutdownHookManager.SPARK_CONTEXT_SHUTDOWN_PRIORITY) { () =>
  logInfo("Invoking stop() from shutdown hook")
  try {
    stop()
  } catch {
    case e: Throwable =>
      logWarning("Ignoring Exception while stopping SparkContext from shutdown hook", e)
  }
}
```
##### nextShuffleId & nextRddId
这两个ID都是AtomicInteger类型保证原子性。Shuffle和RDD都需要唯一ID来进行标识，并且它们是递增的。初始化代码如下：
```scala
private val nextShuffleId = new AtomicInteger(0)
private val nextRddId = new AtomicInteger(0)
```
#### 4.SparkContext后置初始化
在SparkContext初始化中，在初始化的尾声阶段，在ContextCleaner初始化完毕之后，还有一小部分逻辑，其代码如下：
```scala
    setupAndStartListenerBus()
    postEnvironmentUpdate()
    postApplicationStart()

    // Post init
    //等待SchedulerBackend初始化完毕
    _taskScheduler.postStartHook()
    //在度量系统注册DAGScheduler、BlockManager、ExecutorAllocationManager的度量源，以收集它们的监控指标
    _env.metricsSystem.registerSource(_dagScheduler.metricsSource)
    _env.metricsSystem.registerSource(new BlockManagerSource(_env.blockManager))
    _executorAllocationManager.foreach { e =>
      _env.metricsSystem.registerSource(e.executorAllocationManagerSource)
    }

    // Make sure the context is stopped if the user forgets about it. This avoids leaving
    // unfinished event logs around after the JVM exits cleanly. It doesn't help if the JVM
    // is killed, though.
    logDebug("Adding shutdown hook") // force eager creation of logger
    //添加关闭钩子
    _shutdownHookRef = ShutdownHookManager.addShutdownHook(
      ShutdownHookManager.SPARK_CONTEXT_SHUTDOWN_PRIORITY) { () =>
      logInfo("Invoking stop() from shutdown hook")
      try {
        stop()
      } catch {
        case e: Throwable =>
          logWarning("Ignoring Exception while stopping SparkContext from shutdown hook", e)
      }
    }
  //标记SparkContext为活动状态
  SparkContext.setActiveContext(this, allowMultipleContexts)
```
主要的逻辑在开头的三个方法里面。<br/>
**_org.apache.spark.SparkContext#setupAndStartListenerBus_**代码如下：
```scala
  /**
   * Registers listeners specified in spark.extraListeners, then starts the listener bus.
   * This should be called after all internal listeners have been registered with the listener bus
   * (e.g. after the web UI and event logging listeners have been registered).
   */
  private def setupAndStartListenerBus(): Unit = {
    try {
      conf.get(EXTRA_LISTENERS).foreach { classNames =>
        //通过反射监听器
        val listeners = Utils.loadExtensions(classOf[SparkListenerInterface], classNames, conf)
        listeners.foreach { listener =>
          //注册到LiveListenerBus中
          listenerBus.addToSharedQueue(listener)
          logInfo(s"Registered listener ${listener.getClass().getName()}")
        }
      }
    } catch {
      case e: Exception =>
        try {
          stop()
        } finally {
          throw new SparkException(s"Exception when registering SparkListener", e)
        }
    }

    listenerBus.start(this, _env.metricsSystem)
    _listenerBusStarted = true
  }
```
这个方法用于注册自定义的监听器，并最终启动LiveListenerBus。<br/>
自定义监听器都实现了SparkListener特质，通过 `spark.extraListeners` 配置参数来指定。<br/>
然后调用Utils.loadExtensions()方法，通过反射来构建自定义监听器的实例，并将它们注册到LiveListenerBus。
**_org.apache.spark.SparkContext#postEnvironmentUpdate_**方法如下：
```scala
  /** Post the environment update event once the task scheduler is ready */
  private def postEnvironmentUpdate() {
    if (taskScheduler != null) {
      //获取调度方式
      val schedulingMode = getSchedulingMode.toString
      //获得jar列表
      val addedJarPaths = addedJars.keys.toSeq
      //获得文件列表
      val addedFilePaths = addedFiles.keys.toSeq
      //一些环境细节
      val environmentDetails = SparkEnv.environmentDetails(conf, schedulingMode, addedJarPaths, addedFilePaths)
      //封装成SparkListenerEnvironmentUpdate事件
      val environmentUpdate = SparkListenerEnvironmentUpdate(environmentDetails)
      //推送到事件总线LiveListenerBus中
      listenerBus.post(environmentUpdate)
    }
  }
```
该方法在添加自定义文件和JAR包时也都有调用，因为添加的资源会对程序的执行环境造成影响。<br/>
它会取得当前的自定义文件和JAR包列表，以及Spark配置、调度方式，然后通过SparkEnv.environmentDetails()方法再取得JVM参数、Java系统属性等，一同封装成SparkListenerEnvironmentUpdate事件，并投递给事件总线。
**_org.apache.spark.SparkContext#postApplicationStart_**方法代码如下：
```scala
  /** Post the application start event */
  private def postApplicationStart() {
    // Note: this code assumes that the task scheduler has been initialized and has contacted
    // the cluster manager to get an application ID (in case the cluster manager provides one).
    listenerBus.post(SparkListenerApplicationStart(appName, Some(applicationId),
      startTime, sparkUser, applicationAttemptId, schedulerBackend.getDriverLogUrls))
  }
```
向事件总线投递SparkListenerApplicationStart事件，表示Application已经启动。
#### 5.SparkContext中提供的其他功能
##### 1.生成RDD
对于生成RDD，有两种方式，一种是对在内存中的数据执行并行化(parallelize)操作，另一种是从外部存储系统中读取数据之后生成RDD。这两类方法都在SparkContext中。
###### 1.parallelize
对于SparkCotext中的parallelize方法代码如下：
```scala
  def parallelize[T: ClassTag](
      seq: Seq[T],
      numSlices: Int = defaultParallelism): RDD[T] = withScope {
    assertNotStopped()
    new ParallelCollectionRDD[T](this, seq, numSlices, Map[Int, Seq[String]]())
  }
```
该方法生成的RDD类型是ParallelCollectionRDD，numslices指定了RDD中有多少个分区。默认与TaskScheduler的Task的并行度相同(默认最小的分区是2)。
###### 2.从外部文件系统中读取
这里只看用的最多的方法textFile方法。textFile和hadoopFile代码如下：
```scala
  def textFile(
      path: String,
      minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
    assertNotStopped()
    //使用TextInputFormat来接收读到的文件中的数据，使用LongWritable来接收文件内容的偏移量
    hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], minPartitions).map(pair => pair._2.toString).setName(path)
  }
  def hadoopFile[K, V](
      path: String,
      inputFormatClass: Class[_ <: InputFormat[K, V]],
      keyClass: Class[K],
      valueClass: Class[V],
      minPartitions: Int = defaultMinPartitions): RDD[(K, V)] = withScope {
    assertNotStopped()
    // This is a hack to enforce loading hdfs-site.xml.
    // See SPARK-11227 for details.
    FileSystem.getLocal(hadoopConfiguration)
    // A Hadoop configuration can be about 10 KB, which is pretty big, so broadcast it.
    val confBroadcast = broadcast(new SerializableConfiguration(hadoopConfiguration))
    val setInputPathsFunc = (jobConf: JobConf) => FileInputFormat.setInputPaths(jobConf, path)
    new HadoopRDD(
      this,
      confBroadcast,
      Some(setInputPathsFunc),
      inputFormatClass,
      keyClass,
      valueClass,
      minPartitions).setName(path)
  }
```
textFile最终生成的是HadoopRDD，HadoopRDD是KV形式的pair RDD，K中存储着读入文件的偏移量，V中存储着一行一行的文件内容，然后textFile只提取出V中的文本值。
##### 2.广播变量
广播变量是Spark两种共享变量中的一种。所谓广播，就是Driver直接向每个Worker节点发送同一份数据的只读副本，而不像通常一样通过Task来计算。<br/>
广播变量适合处理多节点跨Stage的共享数据，特别是输入数据量较大的集合，可以提高效率。广播变量是Spark两种共享变量中的一种。<br/>
所谓广播，就是Driver直接向每个Worker节点发送同一份数据的只读副本，而不像通常一样通过Task来计算。广播变量适合处理多节点跨Stage的共享数据，特别是输入数据量较大的集合，可以提高效率。
```scala
  def broadcast[T: ClassTag](value: T): Broadcast[T] = {
    assertNotStopped()
    require(!classOf[RDD[_]].isAssignableFrom(classTag[T].runtimeClass),
      "Can not directly broadcast RDDs; instead, call collect() and broadcast the result.")
    val bc = env.broadcastManager.newBroadcast[T](value, isLocal)
    val callSite = getCallSite
    logInfo("Created broadcast " + bc.id + " from " + callSite.shortForm)
    cleaner.foreach(_.registerBroadcastForCleanup(bc))
    bc
  }
```
广播变量的产生依赖于Spark执行环境里面的广播变量管理器BroadcastManager。
##### 3.累加器
累加器与广播变量一样，也是Spark的共享变量。<br/>
顾名思义，累加器就是一个能够累积结果值的变量，最常见的用途是做计数。它在Driver端创建和读取，Executor端（也就是各个Task）只能做累加操作。SparkContext已经提供了数值型累加器的创建方法，如长整型的LongAccumulator。
```scala
  /**
   * Create and register a long accumulator, which starts with 0 and accumulates inputs by `add`.
   */
  def longAccumulator: LongAccumulator = {
    val acc = new LongAccumulator
    register(acc)
    acc
  }
  /**
   * Create and register a long accumulator, which starts with 0 and accumulates inputs by `add`.
   */
  def longAccumulator(name: String): LongAccumulator = {
    val acc = new LongAccumulator
    register(acc, name)
    acc
  }
```
所有累加器的基类都是AccumulatorV2抽象类，我们也可以自定义其他类型的累加器。特征AccumulatorParam则用于封装累加器对应的数据类型及累加操作。
##### 4.runJob
SparkContext提供很多种runJob()方法的重载来运行一个Job，也就是触发RDD行动算子的执行。归根结底，所有runJob()方法的重载都会调用如下逻辑：
```scala
  def runJob[T, U: ClassTag](
      rdd: RDD[T],
      func: (TaskContext, Iterator[T]) => U,
      partitions: Seq[Int],
      resultHandler: (Int, U) => Unit): Unit = {
    if (stopped.get()) {
      throw new IllegalStateException("SparkContext has been shutdown")
    }
    val callSite = getCallSite
    val cleanedFunc = clean(func)
    logInfo("Starting job: " + callSite.shortForm)
    if (conf.getBoolean("spark.logLineage", false)) {
      logInfo("RDD's recursive dependencies:\n" + rdd.toDebugString)
    }
    dagScheduler.runJob(rdd, cleanedFunc, partitions, callSite, resultHandler, localProperties.get)
    progressBar.foreach(_.finishAll())
    rdd.doCheckpoint()
  }
```
可见，它最终调用了DAGScheduler中的runJob方法来运行Job。它会将需要计算的RDD及其分区列表传入，在计算完成后，将结果传回给resultHandler回调方法。在运行Job的同时，还会对RDD本身保存检查点。
##### 5.SparkContext伴生对象
###### SparkContext伴生对象的属性
创建TaskScheduler的方法 `org.apache.spark.SparkContext#createTaskScheduler` 就在SparkContext的伴生对象中。除了它之外，伴生对象主要用来跟踪并维护SparkContext的创建与激活。SparkContext的伴生对象有如下属性：
```scala
private val SPARK_CONTEXT_CONSTRUCTOR_LOCK = new Object()
private val activeContext: AtomicReference[SparkContext] = new AtomicReference[SparkContext](null)
private var contextBeingConstructed: Option[SparkContext] = None
```
这三个对象都与SparkContext的创建有关。SPARK_CONTEXT_CONSTRUCTOR_LOCK是SparkContext构造过程中使用的锁对象，用来保证线程安全。<br/>
activeContext用来标记该SparkContext的活动状态。<br/>
contextBeingConstructed用来保存正在创建的SparkContext。
###### markPartiallyConstructed()方法
这个方法实际上在SparkContext主构造方法中就被调用了，它将当前的SparkContext标记为正在创建。其代码如下：
```scala
  private[spark] def markPartiallyConstructed(
      sc: SparkContext,
      allowMultipleContexts: Boolean): Unit = {
    SPARK_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      assertNoOtherContextIsRunning(sc, allowMultipleContexts)
      contextBeingConstructed = Some(sc)
    }
  }
```
可见最终调用了assertNoOtherContextIsRunning方法，这是一个私有方法，它检测当前是否有多个SparkContext实例在运行。并根据 `spark.driver.allowMultipleContexts` 参数的设置抛出异常或输出警告。
###### 6.setActiveContext()方法
这个方法在SparkContext的主构造方法的末尾处调用，将当前SparkContext标记为已激活。
```scala
  private[spark] def setActiveContext(
      sc: SparkContext,
      allowMultipleContexts: Boolean): Unit = {
    SPARK_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      assertNoOtherContextIsRunning(sc, allowMultipleContexts)
      contextBeingConstructed = None
      //激活SparkContext
      activeContext.set(sc)
    }
  }
```
###### 7.getOrCreate()方法
该方法是除 `new Spark()` 之外，另一种更好的创建SparkContext的途径。它会检查当前是否已经存在了一个已激活的SparkContext，如果有则则复用，没有则创建。
```scala
  def getOrCreate(config: SparkConf): SparkContext = {
    // Synchronize to ensure that multiple create requests don't trigger an exception
    // from assertNoOtherContextIsRunning within setActiveContext
    SPARK_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (activeContext.get() == null) {
        setActiveContext(new SparkContext(config), allowMultipleContexts = false)
      } else {
        if (config.getAll.nonEmpty) {
          logWarning("Using an existing SparkContext; some configuration may not take effect.")
        }
      }
      activeContext.get()
    }
  }
```
#### 6.SparkContext中核心组件
##### 1.事件总线以及ListenerBus
SparkContext在初始化的时候，第一个初始化的组件就是事件总线LiveListenerBus。后面的很多组件都会依赖它，它是SparkContext中很重要的组件。
###### 1.总线概览
监听器注册到ListenerBus事件总线上，事件被投递到ListenerBus中，ListenerBus找到处理该事件的对应的监听器上，然后监听器去处理相关事件。<br/>
所有监听器的基类都是ListenerBus特质，以下是ListenerBus的类继承关系：
```text
ListenerBus(org.apache.spark.util.ListenerBus)
    ExternalCatalogWithListener (org.apache.spark.sql.catalyst.catalog)
    StreamingListenerBus (org.apache.spark.streaming.scheduler)
    StreamingQueryListenerBus (org.apache.spark.sql.execution.streaming)
    SparkListenerBus (org.apache.spark.scheduler)
        AsyncEventQueue (org.apache.spark.scheduler)
        ReplayListenerBus (org.apache.spark.scheduler)
```
###### 2.ListenerBus特质
ListenerBus特质含有两个泛型L和E。L代表监听器的类型，并且它可以是任意类型。E则代表事件的类型。
```scala
private[spark] trait ListenerBus[L <: AnyRef, E] extends Logging {
  //维护了所有注册在事件总线上d的监听器以及它们对应的计时器的二元组，计时器是可选的，用来指示监听器处理事件的时间。
  //它采用了CopyOnWriteArrayList并发容器，以保证x线程安全和支持b并发修改。
  private[this] val listenersPlusTimers = new CopyOnWriteArrayList[(L, Option[Timer])]
  /**
   * Returns a CodaHale metrics Timer for measuring the listener's event processing time.
   * This method is intended to be overridden by subclasses.
   * 将listenersPlusTimers中的监听器单独取出来，转成java.util.List[L]类型。
   */
  private[spark] def listeners = listenersPlusTimers.asScala.map(_._1).asJava
}
```
###### 3.重要方法
1. addListener() & removeListener()
```scala
  /**
   * Add a listener to listen events. This method is thread-safe and can be called in any thread.
   * 往事件总线中添加监听器，这个方法是线程安全的(因为是直接在CopyOnWriteArrayList上操作)，可以在任何线程中去调用它
   */
  final def addListener(listener: L): Unit = {
    listenersPlusTimers.add((listener, getTimer(listener)))
  }
  /**
   * Remove a listener and it won't receive any events. This method is thread-safe and can be called
   * in any thread.
   * 从事件总线中移除监听器，这个方法也是线程安全的(因为是直接在CopyOnWriteArrayList上操作)，可以在任何线程中调用
   */
  final def removeListener(listener: L): Unit = {
    listenersPlusTimers.asScala.find(_._1 eq listener).foreach { listenerAndTimer =>
      listenersPlusTimers.remove(listenerAndTimer)
    }
  }
```
顾名思义，这两个方法分别向事件总线中注册监听器与移除监听器。它们都是直接在CopyOnWriteArrayList上操作，因此是线程安全的。
2. doPostEvent()
```scala
  /**
   * Post an event to the specified listener. `onPostEvent` is guaranteed to be called in the same
   * thread for all listeners.
   */
  protected def doPostEvent(listener: L, event: E): Unit
```
这个方法是将事件event投递给监听器listener进行处理，此处只提供定义，具体由子类去实现。
3. postToAll()
```scala
  /**
   * Post the event to all registered listeners. The `postToAll` caller should guarantee calling
   * `postToAll` in the same thread for all events.
   */
  def postToAll(event: E): Unit = {
    // JavaConverters can create a JIterableWrapper if we use asScala.
    // However, this method will be called frequently. To avoid the wrapper cost, here we use
    // Java Iterator directly.
    // 获得注册在事件总线上所有的监听器和处理时间的迭代器
    val iter = listenersPlusTimers.iterator
    while (iter.hasNext) {
      val listenerAndMaybeTimer = iter.next()
      // 取得监听器
      val listener = listenerAndMaybeTimer._1
      val maybeTimer = listenerAndMaybeTimer._2
      val maybeTimerContext = if (maybeTimer.isDefined) {
        maybeTimer.get.time()
      } else {
        null
      }
      try {
        //将事件投递给已经注册的监听器
        doPostEvent(listener, event)
        if (Thread.interrupted()) {
          // We want to throw the InterruptedException right away so we can associate the interrupt
          // with this listener, as opposed to waiting for a queue.take() etc. to detect it.
          throw new InterruptedException()
        }
      } catch {
        case ie: InterruptedException =>
          logError(s"Interrupted while posting to ${Utils.getFormattedClassName(listener)}.  " +
            s"Removing that listener.", ie)
          removeListenerOnError(listener)
        case NonFatal(e) if !isIgnorableException(e) =>
          logError(s"Listener ${Utils.getFormattedClassName(listener)} threw an exception", e)
      } finally {
        if (maybeTimerContext != null) {
          maybeTimerContext.stop()
        }
      }
    }
  }
```
这个方法通过调用doPostEvent()方法，将事件event投递给所有已注册的监听器。需要注意它是线程不安全的，因此调用方需要保证同时只有一个线程调用它。
###### 4.SparkListenerBus特质
SparkListenerBus特质是Spark Core内部事件总线的基类，其代码如下：
```scala
private[spark] trait SparkListenerBus
  extends ListenerBus[SparkListenerInterface, SparkListenerEvent] {
protected override def doPostEvent(
      listener: SparkListenerInterface,
      event: SparkListenerEvent): Unit = {
    event match {
      case stageSubmitted: SparkListenerStageSubmitted =>
        listener.onStageSubmitted(stageSubmitted)
      case stageCompleted: SparkListenerStageCompleted =>
        listener.onStageCompleted(stageCompleted)
      //......
      case speculativeTaskSubmitted: SparkListenerSpeculativeTaskSubmitted =>
        listener.onSpeculativeTaskSubmitted(speculativeTaskSubmitted)
      case _ => listener.onOtherEvent(event)
  }
}
```
SparkListenerBus继承了ListenerBus，实现了doPostEvent()方法，对事件进行匹配，并调用监听器的处理方法，如果匹配不到则调用onOtherEvent()方法。
SparkListener支持的监听器都是 `org.apache.spark.scheduler.SparkListenerInterface` 的子类，事件则是 `org.apache.spark.scheduler.SparkListenerEvent` 特质的子类。
###### 5.SparkListenerInterface & SparkListenerEvent特质
在SparkListenerInterface特征中，分别定义了处理每一个事件的处理方法，统一命名为“on+事件名称”，代码很简单，就不再贴出来了。<br/>
SparkListenerEvent是一个没有抽象方法的特征，类似于Java中的标记接口(marker interface)，它唯一的用途就是标记具体的事件类。事件类统一命名为“SparkListener+事件名称”，并且都是Scala样例类。我们可以简单看一下它们的部分代码。
```scala
@DeveloperApi
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "Event")
trait SparkListenerEvent {
  protected[spark] def logEvent: Boolean = true
}
@DeveloperApi
case class SparkListenerTaskStart(stageId: Int, stageAttemptId: Int, taskInfo: TaskInfo)
  extends SparkListenerEvent
@DeveloperApi
case class SparkListenerTaskGettingResult(taskInfo: TaskInfo) extends SparkListenerEvent
@DeveloperApi
case class SparkListenerSpeculativeTaskSubmitted(stageId: Int) extends SparkListenerEvent
@DeveloperApi
case class SparkListenerTaskEnd(
    stageId: Int,
    stageAttemptId: Int,
    taskType: String,
    reason: TaskEndReason,
    taskInfo: TaskInfo,
    @Nullable taskMetrics: TaskMetrics)
  extends SparkListenerEvent
@DeveloperApi
case class SparkListenerJobStart(
    jobId: Int,
    time: Long,
    stageInfos: Seq[StageInfo],
    properties: Properties = null)
  extends SparkListenerEvent {
  val stageIds: Seq[Int] = stageInfos.map(_.stageId)
}
// .
```
###### AsyncEventQueue & LiveListenerBus
SparkListenerBus默认采用的时间投递方式是同步调用。如果注册的监听器和产生的时间非常多，同步调用必然会时间的积压和处理的延时。因此Spark提供SparkListenerBus的实现类AsyncEventQueue中，提供异步事件队列机制，他也是SparkContext中的事件总线LiveListenerBus的基础。  
1. AsyncEventQueue(异步时间队列)  
在SparkListenerBus的实现类AsyncEventQueue中，提供了异步事件队列机制，它也是SparkContext中的事件总线LiveListenerBus的基础。  
其声明的源代码如下：
```scala
private class AsyncEventQueue(
    //队列名
    val name: String,
    //Spark配置参数
    conf: SparkConf,
    //LiveListenerBus的度量器
    metrics: LiveListenerBusMetrics,
    //事件总线LIveListenerBus
    bus: LiveListenerBus)
  extends SparkListenerBus
  with Logging {
  import AsyncEventQueue._
  private val eventQueue = new LinkedBlockingQueue[SparkListenerEvent](conf.get(LISTENER_BUS_EVENT_QUEUE_CAPACITY))
  private val eventCount = new AtomicLong()
  private val droppedEventsCounter = new AtomicLong(0L)
  //记录输出被丢弃事件值得时间戳
  @volatile private var lastReportTimestamp = 0L
  //用于标记是否发生了事件被丢弃得情况，使用AtomicBoolean来确保原子性
  private val logDroppedEvent = new AtomicBoolean(false)
  private var sc: SparkContext = null
  //用于标记阻塞队列得状态
  private val started = new AtomicBoolean(false)
  private val stopped = new AtomicBoolean(false)
  private val droppedEvents = metrics.metricRegistry.counter(s"queue.$name.numDroppedEvents")
  private val processingTime = metrics.metricRegistry.timer(s"queue.$name.listenerProcessingTime")
  //异步的实现主要是靠dispatchThread来完成
  private val dispatchThread = new Thread(s"spark-listener-group-$name") {
    setDaemon(true)//设置为守护线程
    override def run(): Unit = Utils.tryOrStopSparkContext(sc) {
      dispatch()
    }
  }
}
```
* eventQueue：用于存储SparkListenerEvent事件的阻塞队列LinkedBlockingQueue，它的大小是通过 `spark.scheduler.listenerbus.eventqueue.capacity` 来设定的，默认值为1000，如果不设置阻塞队列的大小的话，那么阻塞队列的默认大小为Integer.MAX_VALUE，那么会有OOM的风险
* eventCount：用于当前待处理事件的计数，因为事务从队列中取出并不代表事件处理完毕，所以不能用eventQueue的大小来表示，它使用AtomicLong类型来保证原子性
* droppedEventsCounter & lastReportTimestamp：用于被丢弃的事件的计数。当阻塞队列满了的时候，新产生的队列无法入队，就会被丢弃掉，日志中定期输出该计数器的值，用lastReportTimestamp记录下每次输出的时间戳，每当被输出到日志的时候，这个值将会被重置为0
* started & stopped：用于标记阻塞队列得状态
* dispatchThread：是将队列中的事件分发到各监听器的守护线程，实际上调用了dispatch()分发，而Utils.tryOrStopSparkContext()方法的作用在于执行代码块时如果抛出异常，就另外起一个线程关闭SparkContext。  
下面来看下 `org.apache.spark.scheduler.AsyncEventQueue#dispatch` 方法的源代码：
```scala
private def dispatch(): Unit = LiveListenerBus.withinListenerThread.withValue(true) {
  var next: SparkListenerEvent = eventQueue.take()
  //循环去队列中取事件
  while (next != POISON_PILL) {
    val ctx = processingTime.time()
    try {
      //调用父类ListenerBus特质中的postToAll()方法，将其投递给所有已经注册的监听器
      super.postToAll(next)
    } finally {
      ctx.stop()
    }
    //减少待处理事件的计数器的值
    eventCount.decrementAndGet()
    //获取下一个事件
    next = eventQueue.take()
  }
  eventCount.decrementAndGet()
}
private object AsyncEventQueue {
  val POISON_PILL = new SparkListenerEvent() { }
}
```
POISON_PILL(毒药丸)是AsyncEventQueue的伴生对象中定义的一个空的SparkListenerEvent，在队列停止时(即stop方法被调用的时候)会被放进队列中，当dispatchThread取得这个空对象的时候就会"中毒"退出  
上面是从队列中获取事件的方法，接下来看下往队列中加入事件的方法。
即 `org.apache.spark.scheduler.AsyncEventQueue.post` 方法的源码如下：
```scala
def post(event: SparkListenerEvent): Unit = {
  //检查队列是否已经停止了，若停止了则直接退出，没有停止则尝试往队列中放入事件
  if (stopped.get()) {
    return
  }
  //待处理事件的计数器+1
  eventCount.incrementAndGet()
  //offer方法是将一个元素插到队尾，如果插入成功则返回true，如果失败则返回false
  if (eventQueue.offer(event)) {
    //添加事件成功不再进行剩下的逻辑
    return
  }
  //添加事件失败，待处理事件计数器-1
  eventCount.decrementAndGet()
  //丢弃事件的度量器计数器+1
  droppedEvents.inc()
  //丢弃事件的计数器+1
  droppedEventsCounter.incrementAndGet()
  if (logDroppedEvent.compareAndSet(false, true)) {
    // Only log the following message once to avoid duplicated annoying logs.
    logError(s"Dropping event from queue $name. " +
      "This likely means one of the listeners is too slow and cannot keep up with " +
      "the rate at which tasks are being started by the scheduler.")
  }
  logTrace(s"Dropping event $event")

  val droppedCount = droppedEventsCounter.get
  if (droppedCount > 0) {
    // Don't log too frequently
    // 如果离上次打印丢弃事件数的时间间隔大于1分钟的话就再次打印一次
    if (System.currentTimeMillis() - lastReportTimestamp >= 60 * 1000) {
      // There may be multiple threads trying to decrease droppedEventsCounter.
      // Use "compareAndSet" to make sure only one thread can win.
      // And if another thread is increasing droppedEventsCounter, "compareAndSet" will fail and
      // then that thread will update it.
      if (droppedEventsCounter.compareAndSet(droppedCount, 0)) {
        val prevLastReportTimestamp = lastReportTimestamp
        lastReportTimestamp = System.currentTimeMillis()
        val previous = new java.util.Date(prevLastReportTimestamp)
        logWarning(s"Dropped $droppedCount events from $name since $previous.")
      }
    }
  }
}
```
2. LiveListenerBus(异步时间总线)   
AsyncEventQueue继承了SparkListenerBus特质，LiveListenerBus把AsyncEventQueue作为核心。以下是LiveListenerBus的源码  
```scala
private[spark] class LiveListenerBus(conf: SparkConf) {
  import LiveListenerBus._
  private var sparkContext: SparkContext = _
  private[spark] val metrics = new LiveListenerBusMetrics(conf)
  private val started = new AtomicBoolean(false)
  private val stopped = new AtomicBoolean(false)
  private val droppedEventsCounter = new AtomicLong(0L)
  @volatile private var lastReportTimestamp = 0L
  //一个维护着AsyncEventQueue的一个集合
  private val queues = new CopyOnWriteArrayList[AsyncEventQueue]()
  @volatile private[scheduler] var queuedEvents = new mutable.ListBuffer[SparkListenerEvent]()
  // ...
}
```
这个类中的大部分属性与AsyncEventQueue大同小异，多出来的属性主要是queue和queueEvent这两个属性
* queues属性：维护一个AsyncEventQueue列表，也就是说LiveLinstenerBus中有多少个时间队列，它采用CopyOnWriteArrayList来保证线程安全。
* queuedEvent：维护一个SparkListenerEvent列表，用途是在LiveLListenerBus启动成功之前，缓存可能y已经收到的时间，在启动之后，这些实现首先会被投递出去  
LiveListenerBus作为一个事件总线，必须监听器注册，事件投递等功能，这些都是在AsyncEventQueue基础上去做的。  
首先看下 `org.apache.spark.scheduler.LiveListenerBus.addToQueue` 的代码
```scala
private[spark] def addToQueue(
    listener: SparkListenerInterface,
    //监听器队列的名字
    queue: String): Unit = synchronized {
  if (stopped.get()) {
    throw new IllegalStateException("LiveListenerBus is stopped.")
  }
  queues.asScala.find(_.name == queue) match {
    //queue->AsyncEventQueue
    case Some(queue: AsyncEventQueue) =>
      //往AsyncEventQueue中注册监听器
      queue.addListener(listener)
    //没有匹配到相同名字的AsyncEventQueue
    case None = >
      //新建一个新的AsyncEventQueue
      val newQueue = new AsyncEventQueue(queue, conf, metrics, this)
      //往新建的AsyncEventQueue中注册监听器
      newQueue.addListener(listener)
      if (started.get()) {
        //启动新建的队列
        newQueue.start(sparkContext)
      }
      //把新建的队列加入到queues中
      queues.add(newQueue)
  }
}
```
该方法将监听器注册到队列中去。他会去成员变量queues中去寻找是否存在相同队列名的队列，如果相同名称的队列存在的话，那么就调用父类ListenerBus的addListener()方法注册监听器；  
如果没有名称相同的监听器，则生成一个新的AsyncEventQueue队列，然后把监听器注册到新的队列中去。  
投递事件的相关方法如下， `org.apache.spark.scheduler.LiveListenerBus.post` 和 `org.apache.spark.scheduler.LiveListenerBus#postToQueues` 方法如下：
```scala
/** Post an event to all queues. */
def post(event: SparkListenerEvent): Unit = {
  if (stopped.get()) {
    return
  }
  metrics.numEventsPosted.inc()
  // If the event buffer is null, it means the bus has been started and we can avoid
  // synchronization and post events directly to the queues. This should be the most
  // common case during the life of the bus.
  if (queuedEvents == null) {
    postToQueues(event)
    return
  }
  // Otherwise, need to synchronize to check whether the bus is started, to make sure the thread
  // calling start() picks up the new event.
  synchronized {
    if (!started.get()) {
      queuedEvents += event
      return
    }
  }
  // If the bus was already started when the check above was made, just post directly to the
  // queues.
  postToQueues(event)
}
private def postToQueues(event: SparkListenerEvent): Unit = {
  val it = queues.iterator()
  while (it.hasNext()) {
    it.next().post(event)
  }
}
```
post()方法会检查queuedEvents中有无缓存的事件，以及事件总线是否还没有启动。投递时会调用postToQueues()方法，将事件发送给所有队列，由AsyncEventQueue来完成投递到监听器的工作。
##### 2.SparkEnv(Spark运行时环境)
继事件总线初始化完毕之后，接下来需要初始化的组件就是SparkEnv，即Spark执行环境。Driver和Executor的运行都依赖于SparkEnv提供的环境作为支持。   
SparkEnv初始化之后，与Spark相关的计算、存储和度量系统才会真正的准备好，SparkEnv中也包含h很多的组件。
###### SparkEnv入口
Driver环境的创建是通过 `org.apache.spark.SparkEnv.createDriverEnv` 方法来创建的，代码如下：
```scala
private[spark] def createDriverEnv(
    conf: SparkConf,
    isLocal: Boolean,
    //数据总线
    listenerBus: LiveListenerBus,
    //申请的核心数
    numCores: Int,
    mockOutputCommitCoordinator: Option[OutputCommitCoordinator] = None): SparkEnv = {
  assert(conf.contains(DRIVER_HOST_ADDRESS),
    s"${DRIVER_HOST_ADDRESS.key} is not set on the driver!")
  assert(conf.contains("spark.driver.port"), "spark.driver.port is not set on the driver!")
  val bindAddress = conf.get(DRIVER_BIND_ADDRESS)
  val advertiseAddress = conf.get(DRIVER_HOST_ADDRESS)
  val port = conf.get("spark.driver.port").toInt
  val ioEncryptionKey = if (conf.get(IO_ENCRYPTION_ENABLED)) {
    Some(CryptoStreamUtils.createKey(conf))
  } else {
    None
  }
  create(
    conf,
    SparkContext.DRIVER_IDENTIFIER,
    bindAddress,
    advertiseAddress,
    Option(port),
    isLocal,
    numCores,
    ioEncryptionKey,
    listenerBus = listenerBus,
    mockOutputCommitCoordinator = mockOutputCommitCoordinator
  )
}
```
`org.apache.spark.SparkEnv.createExecutorEnv` 是创建Executor的运行环境，代码如下：
```scala
private[spark] def createExecutorEnv(
    conf: SparkConf,
    executorId: String,
    hostname: String,
    numCores: Int,
    ioEncryptionKey: Option[Array[Byte]],
    isLocal: Boolean): SparkEnv = {
  val env = create(
    conf,
    executorId,
    hostname,
    hostname,
    None,
    isLocal,
    numCores,
    ioEncryptionKey
  )
  SparkEnv.set(env)
  env
}
```
创建Driver和Executor环境的时候最终都是调用的 `org.apache.spark.SparkEnv$#create` 方法去创建。首先看下这个方法的签名，签名如下：
```scala
/**
 * Helper method to create a SparkEnv for a driver or an executor.
 */
private def create(
    conf: SparkConf,
    //executor的唯一标识，如果driver的话则是driver字符串
    executorId: String,
    //监听Socket的绑定端口
    bindAddress: String,
    //RPC端点地址
    advertiseAddress: String,
    //监听的端口号
    port: Option[Int],
    //是否是本地模式
    isLocal: Boolean, 
    //分配driver或executor的核心数
    numUsableCores: Int,
    //I/O加密的密钥，当spark.io.encryption.enable配置项启用时才有效
    ioEncryptionKey: Option[Array[Byte]],
    listenerBus: LiveListenerBus = null,
    mockOutputCommitCoordinator: Option[OutputCommitCoordinator] = None): SparkEnv
```
###### SparkEnv初始化
按照 `org.apache.spark.SparkEnv#create` 方法中的顺序依次查看SparkEnv中初始化的组件
1. SecurityManager  
即安全管理器，它通过共享密钥的方式进行认证，以及基于ACL(Access Control List，访问控制列表)来管理Spark内部的账号和权限，其初始化代码如下：
```scala
val securityManager = new SecurityManager(conf, ioEncryptionKey)
if (isDriver) {
  securityManager.initializeAuth()
}

ioEncryptionKey.foreach { _ =>
  if (!securityManager.isEncryptionEnabled()) {
    logWarning("I/O encryption enabled without RPC encryption: keys will be visible on the " +
      "wire.")
  }
}
```
2. RpcEnv
RpcEnv即RPC环境，Spark各个组件之间必然会存在大量的网络通讯，这些通信实体在Spark的RPC体系中会被抽象成RPC端点(即RpcEndpoint)及其引用(即RpcEndpointRef)，RpcEnv为RPC端点提供处理消息环境，并负责RPC端点注册，端点之间的消息路由，以及端点的销毁。其初始化代码如下：
```scala
val systemName = if (isDriver) driverSystemName else executorSystemName
val rpcEnv = RpcEnv.create(systemName, bindAddress, advertiseAddress, port.getOrElse(-1), conf, securityManager, numUsableCores, !isDriver)
// Figure out which port RpcEnv actually bound to in case the original port is 0 or occupied.
if (isDriver) {
  conf.set("spark.driver.port", rpcEnv.address.port.toString)
}
def create(
      name: String,
      bindAddress: String,
      advertiseAddress: String,
      port: Int,
      conf: SparkConf,
      securityManager: SecurityManager,
      numUsableCores: Int,
      clientMode: Boolean): RpcEnv = {
    val config = RpcEnvConfig(conf, name, bindAddress, advertiseAddress, port, securityManager,
      numUsableCores, clientMode)
    new NettyRpcEnvFactory().create(config)
}
```
Spark底层通讯使用的是Netty来实现的，NettyRpcEnv目前是RpcEnv的唯一的实现类，RPC内部细节很多，会在以后进行分析。  
3. SerializerManager  
即序列化器。Spark在存储或交换数据的时候，往往需要先将数据序列化之后再反序列化，为了节省空间还会对数据进行压缩，这就是SerializerManager组件的工作。其初始化代码如下：
```scala
val serializer = instantiateClassFromConf[Serializer]("spark.serializer", "org.apache.spark.serializer.JavaSerializer")
logDebug(s"Using serializer: ${serializer.getClass}")
//数据序列化器
val serializerManager = new SerializerManager(serializer, conf, ioEncryptionKey)
//闭包序列化器
val closureSerializer = new JavaSerializer(conf)
```
`instantiateClassFromConf()` 方法是在create()内部创建的，它会先去SparkConf中寻找 `spark.serializer` 相关配置，如果没有找到则默认使用 `org.apache.spark.util.Utils$.classForName` 方法反射生成一个 `org.apache.spark.util.Utils$.classForName` 序列化器。
除了 `org.apache.spark.serializer.JavaSerializer` 这个序列化器，我们还会使用到 `org.apache.spark.serializer.KryoSerializer` 序列化器。  
序列化器有两个，一个是serializerManager：用于对数据的序列化；另一个是closureSerializer序列化器：是对于闭包的序列化器，此序列化器常用在DAGScheduler、TaskSetManager，其固定类型是JavaSerializer，不能修改  
4. BroadcastManager
即广播管理器。它除了为用户提供广播共享数据的功能之外，再Spark Core内部也广泛使用。如共享通用配置项或通用数据结构等待。其初始化代码如下：
```scala
val broadcastManager = new BroadcastManager(isDriver, conf, securityManager)
```
5. MapOutputTracker  
即Map端输出的跟踪器。再Shuffle过程中，Map任务通过Shuffle Writer阶段中产生了中间数据，Reducer任务在进行Shuffle Read的时候需要知道哪些数据位于哪些节点之上，以及Map输出的状态等信息，MapOutputTracker就负责维护这些信息。其初始化代码如下：
 ```scala
//会去区分Dirver还是Executor端
val mapOutputTracker = if (isDriver) {
  new MapOutputTrackerMaster(conf, broadcastManager, isLocal)
} else {
  new MapOutputTrackerWorker(conf)
}
// Have to assign trackerEndpoint after initialization as MapOutputTrackerEndpoint
// requires the MapOutputTracker itself
mapOutputTracker.trackerEndpoint = registerOrLookupEndpoint(MapOutputTracker.ENDPOINT_NAME,
  new MapOutputTrackerMasterEndpoint(
    rpcEnv, mapOutputTracker.asInstanceOf[MapOutputTrackerMaster], conf))
```
可见Driver端的MapOutputTracker和Executor端的MapOutputTracker是不一样的，Executor端的MapOutputTrackerWorker会像Driver端的MapOutputTrackerMaster汇报自己的情况。  
创建完MapOutputTracker实例之后，还会调用registerOrLookupEndpoint()方法，注册（Driver情况）或查找（Executor情况）对应的RPC端点，并返回其引用。  
6. ShuffleManager  
即Shuffle管理器。顾名思义，它负责管理Shuffle阶段的机制。并提供Shuffle方法的具体实现。其初始化代码如下：
```scala
// Let the user specify short names for shuffle managers
val shortShuffleMgrNames = Map(
  "sort" -> classOf[org.apache.spark.shuffle.sort.SortShuffleManager].getName,
  "tungsten-sort" -> classOf[org.apache.spark.shuffle.sort.SortShuffleManager].getName)
val shuffleMgrName = conf.get("spark.shuffle.manager", "sort")
val shuffleMgrClass = shortShuffleMgrNames.getOrElse(shuffleMgrName.toLowerCase(Locale.ROOT), shuffleMgrName)
val shuffleManager = instantiateClass[ShuffleManager](shuffleMgrClass)
```
ShuffleManager的种类可以通过配置项spark.shuffle.manager设置，默认为sort，即SortShuffleManager。  
取得对应的ShuffleManager类名之后，通过反射构建其实例。Shuffle是Spark计算过程中非常重要的一环，之后会深入地研究它。
7. MemoryManager  
即内存管理器，它负责Spark个节点内存的分配、利用和回收。  
Spark作为一个内存优先的大数据处理框架，内存管理机制是非常细的，主要涉及存储和执行两大方面。其初始化代码如下：
```scala
val useLegacyMemoryManager = conf.getBoolean("spark.memory.useLegacyMode", false)
val memoryManager: MemoryManager =
  if (useLegacyMemoryManager) {
    //静态内存管理器(1.6.x之前使用)
    new StaticMemoryManager(conf, numUsableCores)
  } else {
    //统一内存管理器(1.6.x之后使用，默认)
    UnifiedMemoryManager(conf, numUsableCores)
  }
```
8. BlockManager  
即块管理器。块作为Spark内部数据的基本单位，与操作系统中的"块"和HDFS中的"块"都不太一样。它可以存在于堆内内存，也可以存在于堆外内存和外存（磁盘）中，是Spark数据的通用表示方式。BlockManager就负责管理块的存储、读写流程和状态信息，其初始化代码如下：
```scala
val blockManagerPort = if (isDriver) {
  conf.get(DRIVER_BLOCK_MANAGER_PORT)
} else {
  conf.get(BLOCK_MANAGER_PORT)
}
val blockTransferService =
  new NettyBlockTransferService(conf, securityManager, bindAddress, advertiseAddress, blockManagerPort, numUsableCores)
val blockManagerMaster = new BlockManagerMaster(registerOrLookupEndpoint(
  BlockManagerMaster.DRIVER_ENDPOINT_NAME,
  new BlockManagerMasterEndpoint(rpcEnv, isLocal, conf, listenerBus)),
  conf, isDriver)
```
在初始化BlockManager之前，还需要先初始化块传输服务BlockTransferService，以及BlockManager的主节点BlockManagerMaster。  
BlockManager也是采用主从结构设计的，Driver上存在主RPC端点BlockManagerMasterEndpoint，而各个Executor上都存在从RPC端点BlockManagerSlaveEndpoint。
9. MetricsSystem
MetricsSystem即度量系统。它是Spark监控体系的后端部分，负责收集与输出度量（也就是各类监控指标）数据。度量系统由系统实例Instance、度量数据源Source、度量输出目的地Sink三部分组成。其在SparkEnv里的初始化代码如下
```scala
val metricsSystem = if (isDriver) {
  MetricsSystem.createMetricsSystem("driver", conf, securityManager)
} else {
  conf.set("spark.executor.id", executorId)
  val ms = MetricsSystem.createMetricsSystem("executor", conf, securityManager)
  ms.start()
  ms
}
```
10. OutputCommitCoordinator
即输出提交协调器。如果需要将Spark作业的结果数据持久化到外部存储（最常见的就是HDFS），就需要用到它来判定作业的每个Stage是否有权限提交。其初始化代码如下。
```scala
val outputCommitCoordinator = mockOutputCommitCoordinator.getOrElse {
  new OutputCommitCoordinator(conf, isDriver)
}
val outputCommitCoordinatorRef = registerOrLookupEndpoint("OutputCommitCoordinator",
  new OutputCommitCoordinatorEndpoint(rpcEnv, outputCommitCoordinator))
outputCommitCoordinator.coordinatorRef = Some(outputCommitCoordinatorRef)
```
可见，在Driver上还注册了其RPC端点OutputCommitCoordinatorEndpoint，各个Executor会通过其引用来访问它。  
11. SparkEnv的创建与保存
在create()方法的最后，会构建SparkEnv类的实例，创建Driver端的临时文件夹，并返回该实例。
```scala
val envInstance = new SparkEnv(
  executorId,
  rpcEnv,
  serializer,
  closureSerializer,
  serializerManager,
  mapOutputTracker,
  shuffleManager,
  broadcastManager,
  blockManager,
  securityManager,
  metricsSystem,
  memoryManager,
  outputCommitCoordinator,
  conf)

// Add a reference to tmp dir created by driver, we will delete this tmp dir when stop() is
// called, and we only need to do it for driver. Because driver may run as a service, and if we
// don't delete this tmp dir when sc is stopped, then will create too many tmp dirs.
if (isDriver) {
  val sparkFilesDir = Utils.createTempDir(Utils.getLocalDir(conf), "userFiles").getAbsolutePath
  envInstance.driverTmpDir = Some(sparkFilesDir)
}

envInstance
}
```
###### SparkEnv中的重要组件-RpcEnv 
RpcEnv对于Spark的整体运行起着至关重要的作用，Spark的内部和外部的通讯全部依赖于RpcEnv  
1. RPC端点及其引用  
RpcEnv抽象类是Spark RPC环境的通用表示，它其中的 `org.apache.spark.rpc.RpcEnv.setupEndpoint` 方法是向RPC环境注册一个RPC端点(RpcEndpoint)，并返回其引用(RpcEndpointRef)。  
如果一个客户端想要对一个RpcEndpoint发送消息的话，那么必须先获取其对应的RpcEndpointRef。RpcEndpoint和RpcEndpointRef是Rpc中基础组件  
2. RpcEndpoint  
RpcEndpoint是一个特质，其源代码如下：
```scala
private[spark] trait RpcEndpoint {
  /**
   * The [[RpcEnv]] that this [[RpcEndpoint]] is registered to.
   */
  val rpcEnv: RpcEnv
  /**
   * The [[RpcEndpointRef]] of this [[RpcEndpoint]]. `self` will become valid when `onStart` is
   * called. And `self` will become `null` when `onStop` is called.
   *
   * Note: Because before `onStart`, [[RpcEndpoint]] has not yet been registered and there is not
   * valid [[RpcEndpointRef]] for it. So don't call `self` before `onStart` is called.
   * 取得当前RpcEndpoint对应的RpcEndpointRef，当onStart方法调用之前RpcEndpointRef将是null
   */
  final def self: RpcEndpointRef = {
    require(rpcEnv != null, "rpcEnv has not been initialized")
    rpcEnv.endpointRef(this)
  }
  /**
   * Process messages from `RpcEndpointRef.send` or `RpcCallContext.reply`. If receiving a
   * unmatched message, `SparkException` will be thrown and sent to `onError`.
   * 接收其他RpcEndpointRef发来的消息并进行处理。
   */
  def receive: PartialFunction[Any, Unit] = {
    case _ => throw new SparkException(self + " does not implement 'receive'")
  }
  /**
   * Process messages from `RpcEndpointRef.ask`. If receiving a unmatched message,
   * `SparkException` will be thrown and sent to `onError`.
   * 接收其他RpcEndpointRef发来的消息，并发送回复
   */
  def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    case _ => context.sendFailure(new SparkException(self + " won't reply anything"))
  }
  /**
   * Invoked when any exception is thrown during handling messages.
   * 消息处理出现异常时调用的方法
   */
  def onError(cause: Throwable): Unit = {
    // By default, throw e and let RpcEnv handle it
    throw cause
  }
  /**
   * Invoked when `remoteAddress` is connected to the current node.
   * 当一个RPC端点建立连接的时候调用的方法
   */
  def onConnected(remoteAddress: RpcAddress): Unit = {
    // By default, do nothing.
  }
  /**
   * Invoked when `remoteAddress` is lost.
   * 当一个RPC端点失去连接的时候调用的方法
   */
  def onDisconnected(remoteAddress: RpcAddress): Unit = {
    // By default, do nothing.
  }
  /**
   * Invoked when some network error happens in the connection between the current node and
   * `remoteAddress`.
   * 当RPC端点的链接出现网络错误时调用的方法
   */
  def onNetworkError(cause: Throwable, remoteAddress: RpcAddress): Unit = {
    // By default, do nothing.
  }
  /**
   * Invoked before [[RpcEndpoint]] starts to handle any message.
   * 当RPC端点初始化的时候调用的方法
   */
  def onStart(): Unit = {
    // By default, do nothing.
  }
  /**
   * Invoked when [[RpcEndpoint]] is stopping. `self` will be `null` in this method and you cannot
   * use it to send or ask messages.
   * 当RPC端点关闭的时候调用的方法，在调用这个方法之后RpcEndpointRef将被置为null
   */
  def onStop(): Unit = {
    // By default, do nothing.
  }
  /**
   * A convenient method to stop [[RpcEndpoint]].
   * 停止当前的RpcEndpoint
   */
  final def stop(): Unit = {
    val _self = self
    if (_self != null) {
      rpcEnv.stop(_self)
    }
  }
}
```
3. RpcEndpoint的继承关系
```text
RpcEndpoint (org.apache.spark.rpc.RpcEndpoint)
    OutputCommitCoordinatorEndpoint in OutputCommitCoordinator$ (org.apache.spark.scheduler)
    WorkerWatcher (org.apache.spark.deploy.worker)
    RpcEndpointVerifier (org.apache.spark.rpc.netty)
    MapOutputTrackerMasterEndpoint (org.apache.spark)
    ThreadSafeRpcEndpoint (org.apache.spark.rpc)
        CoarseGrainedExecutorBackend (org.apache.spark.executor)
        DriverEndpoint in CoarseGrainedSchedulerBackend (org.apache.spark.scheduler.cluster)
        BlockManagerMasterEndpoint (org.apache.spark.storage)
        HeartbeatReceiver (org.apache.spark)
        EpochCoordinator (org.apache.spark.sql.execution.streaming.continuous)
        StateStoreCoordinator (org.apache.spark.sql.execution.streaming.state)
        ClientEndpoint (org.apache.spark.deploy)
        Worker (org.apache.spark.deploy.worker)
        RPCContinuousShuffleReader (org.apache.spark.sql.execution.streaming.continuous.shuffle)
        BarrierCoordinator (org.apache.spark)
        ContinuousRecordEndpoint (org.apache.spark.sql.execution.streaming)
        LocalEndpoint (org.apache.spark.scheduler.local)
        Master (org.apache.spark.deploy.master)
        BlockManagerSlaveEndpoint (org.apache.spark.storage)
```
ThreadSafeRpcEndpoint是直接继承自RpcEndpoint的特质。顾名思义，他要求RPC端点对消息的处理必须是线程安全的，线程安全的RpcEndpoint在处理消息时必须满足happens-before原则。  
4. RpcEndpointRef  
RpcEndpointRef是一个抽象类，其源代码如下：
```scala
private[spark] abstract class RpcEndpointRef(conf: SparkConf) extends Serializable with Logging {
  //最大重连次数，用spark.rpc.numRetries来控制
  private[this] val maxRetries = RpcUtils.numRetries(conf)
  //每次重连的等待时间，用spark.rpc.retry.wait来控制
  private[this] val retryWaitMs = RpcUtils.retryWaitMs(conf)
  //对RPC端点进行ask()操作的默认超时时长，默认120秒，用spark.rpc.askTimeout(优先级较高)或spark.network.timeout去控制
  private[this] val defaultAskTimeout = RpcUtils.askRpcTimeout(conf)
  /**
   * return the address for the [[RpcEndpointRef]]
   */
  def address: RpcAddress
  def name: String
  /**
   * Sends a one-way asynchronous message. Fire-and-forget semantics.
   * 异步发送一条单向消息，并且"发送即忘记(fire-and-forget)"，不需要回复
   */
  def send(message: Any): Unit
  /**
   * Send a message to the corresponding [[RpcEndpoint.receiveAndReply)]] and return a [[Future]] to
   * receive the reply within the specified timeout.
   *
   * This method only sends the message once and never retries.
   * 异步发送一条消息，并在规定的超时时间内等待对端RPC端点的回复，RPC端点会调用receiveAndReply方法去处理回复的消息
   */
  def ask[T: ClassTag](message: Any, timeout: RpcTimeout): Future[T]
  /**
   * Send a message to the corresponding [[RpcEndpoint.receiveAndReply)]] and return a [[Future]] to
   * receive the reply within a default timeout.
   *
   * This method only sends the message once and never retries.
   */
  def ask[T: ClassTag](message: Any): Future[T] = ask(message, defaultAskTimeout)
  /**
   * Send a message to the corresponding [[RpcEndpoint.receiveAndReply]] and get its result within a
   * default timeout, throw an exception if this fails.
   *
   * Note: this is a blocking action which may cost a lot of time,  so don't call it in a message
   * loop of [[RpcEndpoint]].
   * 是ask方法的同步实现。由于它是阻塞的，有可能会消耗大量的时间因此必须慎用
   * @param message the message to send
   * @tparam T type of the reply message
   * @return the reply message from the corresponding [[RpcEndpoint]]
   */
  def askSync[T: ClassTag](message: Any): T = askSync(message, defaultAskTimeout)
  /**
   * Send a message to the corresponding [[RpcEndpoint.receiveAndReply]] and get its result within a
   * specified timeout, throw an exception if this fails.
   *
   * Note: this is a blocking action which may cost a lot of time, so don't call it in a message
   * loop of [[RpcEndpoint]].
   *
   * @param message the message to send
   * @param timeout the timeout duration
   * @tparam T type of the reply message
   * @return the reply message from the corresponding [[RpcEndpoint]]
   */
  def askSync[T: ClassTag](message: Any, timeout: RpcTimeout): T = {
    val future = ask[T](message, timeout)
    timeout.awaitResult(future)
  }
}
```
值得注意的是：maxRetries和retryWaitMs这两个属性在2.3.3版本之前是有用到的，在2.4.3版本中没有用到了，整明Spark官方取消了RPC重试机制，也就是统一为消息传输语义中的at most once予以了。我们可以自己实现带有重试机制的RpcEndpointRef  
RpcEndpointRef只有一个子类，那就是 `org.apache.spark.rpc.netty.NettyRpcEndpointRef` ，NettyRpcEndpointRef对send和ask方法的实现如下：
```scala
  override def send(message: Any): Unit = {
    require(message != null, "Message is null")
    nettyEnv.send(new RequestMessage(nettyEnv.address, this, message))
  }
  override def ask[T: ClassTag](message: Any, timeout: RpcTimeout): Future[T] = {
    nettyEnv.ask(new RequestMessage(nettyEnv.address, this, message), timeout)
  }
```
这两个方法都依赖于NettyRpcEnv类，来看下他是被如何构建出来的，它是调用了 `org.apache.spark.rpc.netty.NettyRpcEnvFactory.create` 方法创建的：
```scala
  def create(config: RpcEnvConfig): RpcEnv = {
    val sparkConf = config.conf
    // Use JavaSerializerInstance in multiple threads is safe. However, if we plan to support
    // KryoSerializer in future, we have to use ThreadLocal to store SerializerInstance
    //创建JavaSerializer序列化器，用于RPC传输中的序列化。
    val javaSerializerInstance = new JavaSerializer(sparkConf).newInstance().asInstanceOf[JavaSerializerInstance]
    //通过NettyRpcEnv的构造方法创建啊NettyRpcEnv，这其中会涉及到一些RPC基础组件的初始化。
    val nettyEnv = new NettyRpcEnv(sparkConf, javaSerializerInstance, config.advertiseAddress, config.securityManager, config.numUsableCores)
    if (!config.clientMode) {
      //偏离函数
      val startNettyRpcEnv: Int => (NettyRpcEnv, Int) = { actualPort =>
        nettyEnv.startServer(config.bindAddress, actualPort)
        (nettyEnv, nettyEnv.address.port)
      }
      try {
        //调用通用工具类Utils中的startServiceOnPort()方法来启动NettyRpcEnv。
        Utils.startServiceOnPort(config.port, startNettyRpcEnv, sparkConf, config.name)._1
      } catch {
        case NonFatal(e) =>
          nettyEnv.shutdown()
          throw e
      }
    }
    nettyEnv
  }
```
5. NettyRpcEnv中的属性成员  
我们先不去看NettyRpcEnv类的细节，而是先看他内部包含哪些属性  
```scala
//传输配置信息，作用类似于SparkConf，负责管理与RPC相关的所有配置
private[netty] val transportConf = SparkTransportConf.fromSparkConf(
  conf.clone.set("spark.rpc.io.numConnectionsPerPeer", "1"),
  "rpc",
  conf.getInt("spark.rpc.io.threads", 0))
//调度器，h或者叫分发器，负责将消息路由到正确的RPC端点
private val dispatcher: Dispatcher = new Dispatcher(this, numUsableCores)
//流式管理器，用于处理RPC环境中的w文件，如自定义的配置文件或jar包
private val streamManager = new NettyStreamManager(this)
//传输上下文，作用类似于SparkContext至于Spark.负责管理RPC的服务端(TransportServer)与客户端(TransportClient)与它们之间的Netty传输管道
private val transportContext = new TransportContext(transportConf, new NettyRpcHandler(dispatcher, this, streamManager))
//创建RPCk客户端的额工程类
private val clientFactory = transportContext.createClientFactory(createClientBootstraps())
@volatile private var fileDownloadFactory: TransportClientFactory = _
val timeoutScheduler = ThreadUtils.newDaemonSingleThreadScheduledExecutor("netty-rpc-env-timeout")
private[netty] val clientConnectionExecutor = ThreadUtils.newDaemonCachedThreadPool(
  "netty-rpc-connection",
  conf.getInt("spark.rpc.connect.threads", 64))
//RPC环境中的服务端，负责提供基础且高效的流式服务。
@volatile private var server: TransportServer = _
private val stopped = new AtomicBoolean(false)
private val outboxes = new ConcurrentHashMap[RpcAddress, Outbox]()
```
TransportConf和TransportContext提供底层的基于Netty的RPC机制，TransportClient和TransportServer则是RPC端点的最低级别抽象。
    1. NettyRpcEnv中重要的组件-Dispatcher类  
    Dispatcher类中的属性不是很多，但是都比较重要，其属性声明如下：
    ```scala
    private val endpoints: ConcurrentMap[String, EndpointData] = new ConcurrentHashMap[String, EndpointData]
    private val endpointRefs: ConcurrentMap[RpcEndpoint, RpcEndpointRef] =  new ConcurrentHashMap[RpcEndpoint, RpcEndpointRef]
    private val receivers = new LinkedBlockingQueue[EndpointData]
    private val threadpool: ThreadPoolExecutor = {
      val availableCores = if (numUsableCores > 0) numUsableCores else Runtime.getRuntime.availableProcessors()
      val numThreads = nettyEnv.conf.getInt("spark.rpc.netty.dispatcher.numThreads", math.max(2, availableCores))
      //守护线程池
      val pool = ThreadUtils.newDaemonFixedThreadPool(numThreads, "dispatcher-event-loop")
      for (i <- 0 until numThreads) {
        pool.execute(new MessageLoop)
      }
      pool
    }
    ```
    endpoints和endpointRefs这两个属性分别用ConcurentHashMap来维护RpcEndpointData的名称和RpcEdnpointData；RpcEndpoint和RpcEndpointRef端点引用。  
    receivers存储端点数据的阻塞队列，只有当RPC端点收到要处理的数据的时候，才会被放到阻塞队列中，空闲额RPC端点是不会放进去的。  
    threadPool一个用来调度消息的固定大小的守护线程池，该线程池中的线程的数量由 `spark.rpc.netty.dispatcher.numThreads` 决定，默认值1或2(取决于服务器是否只有一个可用线程)。这个线程池内跑的线程都是MessageLoop类型。
        1. EndpointData  
        EndpointData是Dispatcher的私有内部类。它的实现也很简单，其代码如下：
        ```scala
        private class EndpointData(
            val name: String,
            val endpoint: RpcEndpoint,
            val ref: NettyRpcEndpointRef) {
          val inbox = new Inbox(ref, endpoint)
        }
        ```
        EndpointData接收三个参数，RPC端点名称，RPC端点实例和RPC端点引用，然后构建出一个Inbox对象，什么是Inbox?可以理解为"收件箱"，每个RPC端点都有对应的收件箱，里面采用链表维护着它们收到并且要处理的消息，这些消息均继承自InboxMessage特质。
        2. Dispatcher的调度逻辑  
            1. MessageLoop的实现  
            从上面的描述中可以知道，Dispatcher线程池中执行的都是MessageLoop，它是一个内部类，来看他的代码：
            ```scala
            private class MessageLoop extends Runnable {
              override def run(): Unit = {
                try {
                  while (true) {
                    try {
                      //从receivers阻塞队列中获取需要处理的数据
                      val data = receivers.take()
                      if (data == PoisonPill) {
                        // Put PoisonPill back so that other MessageLoops can see it.
                        receivers.offer(PoisonPill)
                        return
                      }
                      data.inbox.process(Dispatcher.this)
                    } catch {
                      case NonFatal(e) => logError(e.getMessage, e)
                    }
                  }
                } catch {
                  case _: InterruptedException => // exit
                  case t: Throwable =>
                    try {
                      // Re-submit a MessageLoop so that Dispatcher will still work if
                      // UncaughtExceptionHandler decides to not kill JVM.
                      threadpool.execute(new MessageLoop)
                    } finally {
                      throw t
                    }
                }
              }
            }
            ```
            可以看出MessageLoop本质上是一个不断循环处理消息的线程，它每次从阻塞队列中，
>由于在RDD的一系操作中，**如果一些连续的操作都是窄依赖操作的话，那么它们的执行是可以并行的，这一系列操作会形成pipeline的形式去处理数据**，而宽依赖则不行。  
>Spark中的Stage的划分就是以宽依赖来划分的，将一个Job(一个Action操作会生成一个Job，有多少个Action就有多少个Job)划分成多个Stage，一个Stage里面的任务，被抽象成TaskSet，一个TaskSet中包含很多Task(一个Partition对应一个Task)，同一个Stage中的Task的操作逻辑是相同的，只是要处理的数据不同
1. TaskScheduler
    1. 创建TaskScheduler
    ```scala
    // Create and start the scheduler
    //createTaskScheduler的具体事现参见org.apache.spark.SparkContext#createTaskScheduler
    val (sched, ts) = SparkContext.createTaskScheduler(this, master, deployMode)
    _schedulerBackend = sched
    //创建TaskScheduler和DAGScheduler的顺序不能颠倒，DAGScheduler管理着TaskScheduler，DAGScheduler的创建依赖着TaskScheduler
    _taskScheduler = ts
    _dagScheduler = new DAGScheduler(this)
    _heartbeatReceiver.ask[Boolean](TaskSchedulerIsSet)
    ```
2. TaskScheduler初始化机制：
    1. 首相在SparkContext中调用createTaskScheduler方法，这个方法比较关键，createTaskScheduler这个方法会创建几个比较重要的对象： `org.apache.spark.scheduler.TaskSchedulerImpl`,`org.apache.spark.scheduler.SchedulerBackend`
        1. `org.apache.spark.scheduler.TaskSchedulerImpl` 就是TaskScheduler，TaskSchedulerImpl底层依赖SchedulerBackend来工作
        2. `org.apache.spark.scheduler.SchedulerBackend` ，在standalone模式下是具体的 `org.apache.spark.scheduler.cluster.StandaloneSchedulerBackend` 类，它接收TaskSchedulerImpl的控制，实际上负责与Master的注册、Executor的反向注册，把Task发送到Executor等操作
        3. SchedulerPool
2. DAGScheduler
    1. 创建DAGScheduler
    ```scala
    _dagScheduler = new DAGScheduler(this)
    //DAGScheduler类
    private[spark] class DAGScheduler(
        private[scheduler] val sc: SparkContext,
        private[scheduler] val taskScheduler: TaskScheduler,
        listenerBus: LiveListenerBus,
        mapOutputTracker: MapOutputTrackerMaster,
        blockManagerMaster: BlockManagerMaster,
        env: SparkEnv,
        clock: Clock = new SystemClock())
      extends Logging {
        //DAGScheduler的创建依赖taskScheduler
        //需要事先创建好taskScheduler
        def this(sc: SparkContext) = this(sc, sc.taskScheduler)
        def this(sc: SparkContext, taskScheduler: TaskScheduler) = {
          this(
            sc,
            taskScheduler,
            sc.listenerBus,
            sc.env.mapOutputTracker.asInstanceOf[MapOutputTrackerMaster],
            sc.env.blockManager.master,
            sc.env)
        }
    }
    ```
3. SchedulerBackend
    1. 创建SchedulerBackend
    ```scala
    _schedulerBackend = sched
    ```
## 算子
预先输出的RDD，注意RDD中是不存数据的，只存运算的逻辑，RDD生成的Task来执行真正的计算
```java
    rdd1= jsc.parallelize(Arrays.asList("zhengwei","zhangsan","lisi","wangwu","maliu"));
    rdd2=jsc.parallelize(Arrays.asList("lisi","wanwu","maliu","zhengwei1","zhengwei2"));
    rdd3=jsc.parallelize(Arrays.asList(1,2,3,4,5,6,7,8,9));
    rdd4=jsc.parallelize(Arrays.asList(5,6,7,8,9,10,11,12,5,6,12));
    rdd5=jsc.parallelize(Arrays.asList("a b c","d e f","g h i"));
    rdd6=jsc.parallelizePairs(Arrays.asList(
                new Tuple2<>("zhengwei1",18),
                new Tuple2<>("zhengwei2",20),
                new Tuple2<>("zhengwei3",22),
                new Tuple2<>("zhengwei1",20),
                new Tuple2<>("zhengwei2",25),
                new Tuple2<>("zhengwei3",30)
        ));
```
1. 转换(Transformation)算子：懒加载，需要Action算子去触发执行
    1. map算子：对RDD中的每条记录进行映射操作，输入与输出的数据是一一对应的关系
        ```java
        JavaRDD<String> map = rdd1.map(x -> x + "_map");
        ```
        输出的结果为
        ```text
        zhengwei_map
        zhangsan_map
        lisi_map
        wangwu_map
        maliu_map
        ```
    2. filter算子：对RDD中的每条记录进行过滤操作
        ```java
        JavaRDD<Integer> filter = rdd3.filter(x -> x > 5);
        ```
        输出的结果为
        ```text
        9
        8
        7
        6
        ```
    3. flatMap(func)算子：类似于map，但是输入与输出的数据并不一定是一一对应的关系，可能对于多个记录也可能是少的记录，所以flatMap要求返回一个集合
        ```java
        JavaRDD<String> flatMap = rdd5.flatMap(x -> Arrays.asList(x.split("[ ]")).iterator());
        ```
        输出结果
        ```text
        g
        d
        a
        b
        c
        e
        f
        h
        i
        ```
    4. mapPartitions(func)算子：类似于map算子，输入数据与输出数据是一一对应的，但是独立的运行于每一个RDD的分区上，效率比map的效率要高
        ```java
        JavaRDD<String> mapPartitions = rdd2.mapPartitions(iter -> {
                    List<String> result = new ArrayList<>();
                    iter.forEachRemaining(x->result.add(x+"_mapPartitions"));
                    return result.iterator();
        });
        ```
       输出结果
       ```text
       lisi_mapPartitions
       zhengwei2_mapPartitions
       wanwu_mapPartitions
       zhengwei1_mapPartitions
       maliu_mapPartitions
       ```
    5. mapPartitionsWithIndex(func)算子：类似于mapPartitions算子，但func带有一个整数参数表示分区索引
        ```java
        JavaRDD<String> mapPartitionsWithIndex = rdd2.mapPartitionsWithIndex((index, iter) -> {
                    List<String> result=new ArrayList<>();
                    //拿到一个分区，然后通过分区获取里面的数据
                    iter.forEachRemaining(x -> result.add("partition index->" + index + ",record->" + x));
                    return result.iterator();
        },true);
        ```
        输出的结果
        ```text
        partition index->1,record->maliu
        partition index->0,record->lisi
        partition index->0,record->wanwu
        partition index->1,record->zhengwei1
        partition index->1,record->zhengwei2
        ```
    6. union(otherRDD)算子：融合两个RDD,不会去重，只是单纯的把两个RDD融合在一起，取全集
        ```java
        JavaRDD<String> union = rdd1.union(rdd2);
        ```
        输出的结果
        ```text
        lisi
        wangwu
        maliu
        zhengwei
        zhangsanlisi
        wanwu
        maliu
        zhengwei1
        zhengwei2
        ```
    7. intersection(otherRDD)算子：取交集
        ```java
        JavaRDD<String> intersection = rdd1.intersection(rdd2);
        ```
        输出结果
        ```text
        lisi
        maliu
        ```
    8. distinct([numTasks])算子：对RDD进行去重操作
        ```java
        JavaRDD<Integer> distinct = rdd4.distinct();
        ```
        输出结果
        ```text
        6
        11
        8
        7
        12
        9
        10
        5
        ```
    9. groupByKey([numTasks])算子：对K-V RDD进行key相同的进行汇聚，效率较低，尽量避免使用
        ```java
        JavaPairRDD<String, Iterable<Integer>> groupByKey = rdd6.groupByKey();
        ```
        输出结果
        ```text
        zhengwei3->[22, 30]
        zhengwei1->[18, 20]
        zhengwei2->[20, 25]
        ```
    10. reduceByKey([numTasks])算子：对K-V RDD进行key相同的进行汇聚，效率比 `groupByKey` 高，它会现在每个节点上先进行汇聚一次之后，再进行shuffle
        reduceByKey的源代码
        ```scala
        def reduceByKey(partitioner: Partitioner, func: (V, V) => V): RDD[(K, V)] = self.withScope {
            combineByKeyWithClassTag[V]((v: V) => v, func, func, partitioner)
        }
        ```
        * createCombiner：与foldByKey相比，reduceByKey没有初始值，createCombiner也没有调用函数，而是直接将参数作为返回值返回了
        * mergeValue，mergeCombiners：func函数同时是mergeValue和mergeCombiners
        * 当不需要createCombiner，且mergeValue和mergeCombiners相同时适用
        ```java
        JavaPairRDD<String, Integer> reduceByKey = rdd6.reduceByKey(Integer::sum);
        ```
        输出结果
        ```text
        zhengwei3->52
        zhengwei1->38
        zhengwei2->45
        ```
    11. aggregateByKey(zeroValue,func1,func2)算子：对K-V RDD进行key相同的汇聚，**初始值只会在分区中汇聚时(第一个lambda表达式)进行使用，在全局汇聚时(第二个lambda表达式)不使用**
        aggregateByKey源码
        ```scala
        def aggregateByKey[U: ClassTag](zeroValue: U, partitioner: Partitioner)(seqOp: (U, V) => U,
          combOp: (U, U) => U): RDD[(K, U)] = self.withScope {
        // 中间代码省略，主要看最后一个，调用combineByKey
        val cleanedSeqOp = self.context.clean(seqOp)
        // seqOp，同时是，createCombiner，mergeValue。而combOp是mergeCombiners
        combineByKeyWithClassTag[U]((v: V) => cleanedSeqOp(createZero(), v),
          cleanedSeqOp, combOp, partitioner)
        }
        ```
        * createCombiner：cleanedSeqOp(createZero(),v)是createCombiner，也就是传入的seqOpz，只不过其中一个值是传入的zeroValue
        * mergeValue：seqOp函数同样是mergeValue，createCombine和mergeValue函数相同是aggregateByKey的关键
        * mergeCombiners：combOp函数
        * 当createCombiner和mergeValue函数的操作相同，aggregate更合适
        实验：
        ```java
        JavaPairRDD<String, Integer> aggregateByKey = rdd6.aggregateByKey(2, Integer::sum, Integer::sum);
        ```
        输出结果
        ```text
        特别说明：rdd6是有两个分区(代码中没有指定，默认两个)，
        那么第一个分区中有key->zhengwei1,zhengwei2,zhengwei3
        第二个分区中有key->zhengwei1,zhengwei2,zhengwei3
        所以进行汇聚的时候第一个分区中的zhengwei1的value+zeroValue+第二个分区中的zhengwei1的value+zeroValue
        以此类推，得到最终结果
        zhengwei2->49 -> 20+2+25+2
        zhengwei3->56 -> 22+2+30+2
        zhengwei1->42 -> 18+2+30+2
        ```
    11. filterByRange(str,str)算子：按给的范围进行过滤(好像被移除了)
    12. flatMapValues(func)算子：对KVRDD进行value压平操作
        ```java
        JavaPairRDD<String, String> flatMapValues = rdd1.mapToPair(x -> new Tuple2<>(x, x + "_map")).flatMapValues(x -> Arrays.asList(x.split("[_]")));
        ```
        输出结果
        ```text
        lisi->lisi
        lisi->map
        zhengwei->zhengwei
        wangwu->wangwu
        wangwu->map
        zhengwei->map
        zhangsan->zhangsan
        zhangsan->map
        maliu->maliu
        maliu->map
        ```
    13. foldByKey()算子：
    14. combineByKey(func,func,func)算子：按Key进行汇聚
        combineByKey的源码
        ```scala
        def combineByKey[C](
          createCombiner: V => C,
          mergeValue: (C, V) => C,
          mergeCombiners: (C, C) => C): RDD[(K, C)] = self.withScope {
        combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners)(null)
        }
        ```
        * createCombiner：当combineByKey第一次遇到值为Key时，调用createCombiner函数，将V=>C
        * mergeValue：combineByKey不是第一次遇到k为Key的值时，调用mergeValue函数，将V累加到C中(分区中局部汇聚)
        * mergeCombine：将两个C合并起来(全局汇聚)
        实验：
        ```java
        JavaPairRDD<String, Integer> combineByKey = rdd6.combineByKey(x -> x, Integer::sum, Integer::sum);
        JavaPairRDD<String, List<Integer>> combineByKey1 = rdd6.combineByKey(
                        c -> {
                            List<Integer> list = new ArrayList<>();
                            list.add(c);
                            return list;
                        }, 
                        (c, v) -> {
                            c.add(v);
                            return c;
                        }, 
                        (c1, c2) -> {
                            c1.addAll(c2);
                            return c1;
                        }
                );
        ```
        输出结果
        ```text
        zhengwei1--38
        zhengwei3--52
        zhengwei2--45
        
        zhengwei1--[18, 20]
        zhengwei3--[22, 30]
        zhengwei2--[20, 25]
        ```
    15. cache()算子：缓存RDD算子，懒加载，不会生成 "新的" RDD，所生成的RDD只是对原RDD的引用，如果要**缓存的数据大于实际的内存的话，
    Spark只是会缓存一部分到内存，缓存一部分到磁盘中，cache是不会切断RDD的血统关系的**
        unpersist()算子，取消缓存
        ```java 
        JavaRDD<String> cache = rdd1.cache();
        JavaRDD<String> unpersist = cache.unpersist();
        ```
        cache实际调用的是 `persist()` 方法，`persist()` 方法默认的缓存级别是全存在内存中，不仅如此， `persist()` 还提供不通的存储等级来满足需求，具体的存储等级如下：
        ```scala
        //第一个参数：是否缓存到磁盘
        //第二个参数：是否缓存到内存
        //第三个参数：缓存到磁盘中，是否要进行序列化，true->不进行序列化，按Java对象缓存，false->进行序列化，一序列化之后的对象缓存
        //第四个参数：缓存到内存中，是否要进行序列化，true->不进行序列化，按Java对象缓存，false->进行序列化，一序列化之后的对象缓存
        //第五个参数，要缓存的数据的副本数
        val NONE = new StorageLevel(false, false, false, false)
        /*
        虽然 `persist(DISK_ONLY)` 会将RDD中的partition持久化到磁盘中，
        但是partition是由blockManager管理的，一旦在Driver Program执行结束之后，
        即Executor所在的进程CoarseGrainedExecutorBackend被stop，被持久化到磁盘的数据也会被删除(整个blockManager使用的local文件被删除)
        */
        val DISK_ONLY = new StorageLevel(true, false, false, false)
        val DISK_ONLY_2 = new StorageLevel(true, false, false, false, 2)
        val MEMORY_ONLY = new StorageLevel(false, true, false, true)
        val MEMORY_ONLY_2 = new StorageLevel(false, true, false, true, 2)
        val MEMORY_ONLY_SER = new StorageLevel(false, true, false, false)
        val MEMORY_ONLY_SER_2 = new StorageLevel(false, true, false, false, 2)
        val MEMORY_AND_DISK = new StorageLevel(true, true, false, true)
        val MEMORY_AND_DISK_2 = new StorageLevel(true, true, false, true, 2)
        val MEMORY_AND_DISK_SER = new StorageLevel(true, true, false, false)
        val MEMORY_AND_DISK_SER_2 = new StorageLevel(true, true, false, false, 2)
        val OFF_HEAP = new StorageLevel(true, true, true, false, 1)
        ```
        **cache缓存的数据是存放在Executor所在的机器上的内存或者磁盘的，若缓存在内存中的话，内存中是数据的地址引用**
    16. checkpoint()算子：将需要花费很大代价的RDD缓存到可靠的文件系统中，在使用checkpoint之前，需要设置checkpoint的目录，即 `JavaSparkContext jsc.setChackPoint("hdfs://ns:9000/checkpoint")` ，设置完了之后，下次进行checkpoint的时候，spark会把缓存下来的文件存入到对应的hdfs目录中。<br/>
        **特别注意的是：checkpoint会切断RDD的血统关系，因为hdfs的高容错性，保证了缓存的副本是可靠的，可以丢弃掉之前的依赖关系；被checkpoint到hdfs的缓存文件是持久化到磁盘的，即使整个Driver Program执行结束了之后，缓存的数据也不会消失；checkpoint会等到job结束之后，会单独再启动一个job去完成checkpoint，即需要被checkpoint的RDD会被计算两次**<br>
        **Spark官方建议把需要checkpoint的RDD先缓存到内存中，再进行checkpoint，这样就不会把RDD计算两次了**
        ```scala
        /**
         * Mark this RDD for checkpointing. It will be saved to a file inside the checkpoint
         * directory set with `SparkContext#setCheckpointDir` and all references to its parent
         * RDDs will be removed. This function must be called before any job has been
         * executed on this RDD. It is strongly recommended that this RDD is persisted in
         * memory, otherwise saving it on a file will require recomputation.
         */
        def checkpoint(): Unit = RDDCheckpointData.synchronized {
          // NOTE: we use a global lock here due to complexities downstream with ensuring
          // children RDD partitions point to the correct parent partitions. In the future
          // we should revisit this consideration.
          if (context.checkpointDir.isEmpty) {
            throw new SparkException("Checkpoint directory has not been set in the SparkContext")
          } else if (checkpointData.isEmpty) {
            checkpointData = Some(new ReliableRDDCheckpointData(this))
          }
        }
        ```
2. 行动算子(Action)：触发转换(Transformation)算子的执行，决定了一个Application中有多少个Job，有几个Action就会有几个Job
    1. aggregate(zeroValue,func1,func2)算子：先分区内部聚合，再分区之间聚合，**初始值会在每次分区中局部汇聚时(第一个lambda表达式)使用一次，全局汇聚时(第二个lambda表达式)使用一次**，取决于分区的数量，应用的次数=partition size+1
        ```java
        Integer aggregate = rdd4.aggregate(0, Integer::sum, Integer::sum);
        String aggregate1 = rdd1.aggregate("|", (x1, y1) -> x1 + y1, (x2, y2) -> x2 + y2);
        ```
        输出结果
        ```text
        91
        ||zhengweizhangsan|lisiwangwumaliu
        ```
    2. collectAsMap算子：将结果收集成Map集合，**结果会对key进行去重**
        ```java
        Map<String, Integer> collectAsMap = rdd6.mapValues(x -> x * 10).collectAsMap();
        ```
        输出结果
        ```text
        zhengwei1->200
        zhengwei3->300
        zhengwei2->250
        ```
    3. countByKey()算子：对key出现的次数进行统计
    4. countByValue()算子：对value出现的次数进行统计
    5. foreach(func)算子：每次仅取出一条数据进行处理
    6. foreachPartition(func)算子：每次取出一整个partition中的数据，比foreach要高效，saveAsTextFile底层调用的就是foreachPartition，所以是有几个分区就会有几个文件
## Spark杂散知识(平时积累或看到的)
* 一个partition对应一个task(在同一个Stage中)，一个partition对应的task只能在一台机器里面(Executor)，一台机器上可以有多个分区，即对应多个task，一个task对应一个输出文件
* Executor中包含Task
* Driver默认只收集1G的数据，超过1G就不再收集了
* 最好不要将结果通过collect收集到Driver端，可以直接在算子中直接写入到需要的存储系统中(redis,hbase,mysql...)，这样可以提高效率，不会对Driver造成冲击
* Spark是边读边计算，
* Spark的WordCount的执行流程
    1. 会生成6个RDD，特别注意的是textFile会生成两个RDD->(HadoopRDD,MapPartitionsRDD)，saveAsTextFile会生成一个RDD->(MapPartitionsRDD)
    2. 刚开始textFile读文件进来的时候是会有`HadoopRDD<偏移量,一行数据>`，然后回调用map将偏移量给去掉即`MapPartitionsRDD<一行文本>`，只保留下一行的文本数据
    具体源码为
    ```scala
    hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
          minPartitions).map(pair => pair._2.toString).setName(path)
    ```
    3. saveAsTextFile会生成一个 `MapPartitions<String>` 用来存储文件到hdfs
    ```scala
    def saveAsTextFile(path: String): Unit = withScope {
        // https://issues.apache.org/jira/browse/SPARK-2075
        //
        // NullWritable is a `Comparable` in Hadoop 1.+, so the compiler cannot find an implicit
        // Ordering for it and will use the default `null`. However, it's a `Comparable[NullWritable]`
        // in Hadoop 2.+, so the compiler will call the implicit `Ordering.ordered` method to create an
        // Ordering for `NullWritable`. That's why the compiler will generate different anonymous
        // classes for `saveAsTextFile` in Hadoop 1.+ and Hadoop 2.+.
        //
        // Therefore, here we provide an explicit Ordering `null` to make sure the compiler generate
        // same bytecodes for `saveAsTextFile`.
        val nullWritableClassTag = implicitly[ClassTag[NullWritable]]
        val textClassTag = implicitly[ClassTag[Text]]
        //因为保存到hdfs需要KV形式的RDD，把K设置成null，然后把value.toString()输出到文件中
        val r = this.mapPartitions { iter =>
          val text = new Text()
          iter.map { x =>
            text.set(x.toString)
            (NullWritable.get(), text)
          }
        }
        RDD.rddToPairRDDFunctions(r)(nullWritableClassTag, textClassTag, null)
          .saveAsHadoopFile[TextOutputFormat[NullWritable, Text]](path)
      }
    ```
    3. Spark会根据shuffle来划分Stage，同一个Stage中的Task的类型是相同的，MapShuffleTask或者是ResultTask
    4. 一共有多少个Task由Stage和Partition共同决定，读取hdfs中文件时，有多少个文件切片就会有多少个Partition，即就会有多少个Task，即这个Stage中一共有Task，如果后续的Stage中没有自己指定分区数的话，那么后续的Stage的分区将和之前的Stage中的分区保持一致，
       那一共有多少个`一个Job中Task总数=Stage的数量*一个Stage中Task的总数`.
    5. Shuffle分为两个步骤，分别是MapTask和ResultTask，MapTask会把根据分区器的规则把结果溢写到磁盘中然后把最后的结果反馈给Driver，ResultTask会去和Driver通信，获取到MapTask溢写的文件的位置，然后去拉取再做聚合
## 广播变量和累加器
1. 广播变量
    1. **不能将一个RDD广播出去**，因为RDD中是不存储数据的，只存储计算逻辑，不过可以把RDD的极计算结果广播出去
    2. 广播变量只能在Driver进行定义，**不能再Executor端进行定义**
    3. 在Driver端可以修改广播变量的值，但是在Executor中不能修改广播变量的值
    4. 如果Executor端用到了Driver端的变量，如果没有使用广播变量的话那么有多少个Task就会有多少个变量的副本；如果使用了广播变量的话，那么不论有多少个Task，变量的副本始终是一份
    5. **广播变量是存储在Executor中的MemoryStore中的**
2. 累加器
## 广播管理器( `org.apache.spark.broadcast.BroadcastManager` )
1. BroadcastManager用于将配置信息和序列化后的RDD、Job以及ShuffleDependency等信息本地存储。如果为了容灾，也会复制到其他节点上，BroadcastManager的类签名如下
```scala
private[spark] class BroadcastManager(
    //是否是Driver
    val isDriver: Boolean,
    //SparkConf
    conf: SparkConf,
    //安全管理器
    securityManager: SecurityManager)
  extends Logging {
  //是否以及被初始化
  private var initialized = false
  //广播工厂实例
  private var broadcastFactory: BroadcastFactory = null
  //下一个广播对象的的广播ID，使用AtomicLong保证原子性
  private val nextBroadcastId = new AtomicLong(0)
  initialize()

  // Called by SparkContext or Executor before using Broadcast
  // BroadcastManager在初始化过程中就会调用自己的initialize方法，当initialize执行完毕之后，BroadcastManager就正式生效了。
  private def initialize() {
    //同步代码块，确保BroadcastManager只初始化一次
    synchronized {
      //首先判断BroadcastManager是否已经被初始化过
      if (!initialized) {
        //新建TorrentBroadcastFactory(`org.apache.spark.broadcast.TorrentBroadcastFactory`)作为BroadcastFactory的实例
        //从Spark2.x开始，Spark的BroadcastManager的实例固定为TorrentBroadcastManager
        broadcastFactory = new TorrentBroadcastFactory
        //调用TorrentBroadcastFactory的initialize方法对TorrentBroadcastFactory进行初始化
        broadcastFactory.initialize(isDriver, conf, securityManager)
        //更新初始化状态，标记为true，表示初始化完毕
        initialized = true
      }
    }
  }
  /**
   * BroadcastManager的三个方法都分别代理了TorrentBroadcastManager的三个方法
   */
  def stop() {
    broadcastFactory.stop()
  }

  private[broadcast] val cachedValues = {
    new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK)
  }

  def newBroadcast[T: ClassTag](value_ : T, isLocal: Boolean): Broadcast[T] = {
    broadcastFactory.newBroadcast[T](value_, isLocal, nextBroadcastId.getAndIncrement())
  }

  def unbroadcast(id: Long, removeFromDriver: Boolean, blocking: Boolean) {
    broadcastFactory.unbroadcast(id, removeFromDriver, blocking)
  }
}
```

## 宽依赖和窄依赖
1. 宽依赖：父RDD中的一个partition给了子RDD的**多个**partition，父RDD中的一个partition对应子RDD中的多个partition
    * 现阶段需要依赖上一个Stage的全部内容运算结果，上一个Stage需要全部完成，然后经过shuffle，才能开始这个Stage。
    * 对于数据恢复：对于一个宽依赖的lineage，单个节点的数据丢失的话可能会导致这个RDD的所有父RDD丢失部分数据，因而导致整个lineage的重新计算，效率低下
    * Spark中的宽依赖实现是 `org.apache.spark.ShuffleDependency`
        1. `org.apache.spark.ShuffleDependency` 需要进行shuffle
        ```scala
        /**
         * Represents a dependency on the output of a shuffle stage. Note that in the case of shuffle,
         * the RDD is transient since we don't need it on the executor side.
         *
         * @param _rdd the parent RDD
         * @param partitioner partitioner used to partition the shuffle output
         * @param serializer [[org.apache.spark.serializer.Serializer Serializer]] to use. If not set
         *                   explicitly then the default serializer, as specified by `spark.serializer`
         *                   config option, will be used.
         * @param keyOrdering key ordering for RDD's shuffles
         * @param aggregator map/reduce-side aggregator for RDD's shuffle
         * @param mapSideCombine whether to perform partial aggregation (also known as map-side combine)
         */
        @DeveloperApi
        class ShuffleDependency[K: ClassTag, V: ClassTag, C: ClassTag](
            @transient private val _rdd: RDD[_ <: Product2[K, V]],
            //分区器Partitioner
            val partitioner: Partitioner,
            //SparkEnv中创建serializer，即org.apache.spark.serializer.JavaSerializer
            val serializer: Serializer = SparkEnv.get.serializer,
            //按key进行排序的scala.math.Ordering的实现类
            val keyOrdering: Option[Ordering[K]] = None,
            //对map任务的输出数据进行聚合的聚合器
            val aggregator: Option[Aggregator[K, V, C]] = None,
            //是否在map端进行聚合
            val mapSideCombine: Boolean = false)
          extends Dependency[Product2[K, V]] {
        
          if (mapSideCombine) {
            require(aggregator.isDefined, "Map-side combine without Aggregator specified!")
          }
          override def rdd: RDD[Product2[K, V]] = _rdd.asInstanceOf[RDD[Product2[K, V]]]
        
          private[spark] val keyClassName: String = reflect.classTag[K].runtimeClass.getName
          private[spark] val valueClassName: String = reflect.classTag[V].runtimeClass.getName
          // Note: It's possible that the combiner class tag is null, if the combineByKey
          // methods in PairRDDFunctions are used instead of combineByKeyWithClassTag.
          private[spark] val combinerClassName: Option[String] =
            Option(reflect.classTag[C]).map(_.runtimeClass.getName)
          //获取Shuffle Id
          val shuffleId: Int = _rdd.context.newShuffleId()
          //当前ShuffleDependency的处理器
          val shuffleHandle: ShuffleHandle = _rdd.context.env.shuffleManager.registerShuffle(
            shuffleId, _rdd.partitions.length, this)
        
          _rdd.sparkContext.cleaner.foreach(_.registerShuffleForCleanup(this))
        }
        ```
2. 窄依赖：父RDD中的一个partition给了子RDD的**一个**partition，父RDD中的partition与子RDD中的partition是一一对应的
    * 窄依赖中每个子RDD的partition中的操作都是可以并行执行的，每条记录依次通过pipeline(流水线)的形式在内存中进行迭代。
    * 对于数据恢复：在窄依赖中，如果RDD的一个分区的数据丢失，只需要计算丢失的分区的数据即可，而且不同节点之间可以并行执行，恢复容易高效。
    * Spark中的窄依赖的实现是 `org.apache.spark.NarrowDependency` , `org.apache.spark.NarrowDependency` 是一个抽象类，它有三个子类 `org.apache.spark.OneToOneDependency` 和 `org.apache.spark.RangeDependency` 和 `org.apache.spark.rdd.PruneDependency`
        1. `org.apache.spark.OneToOneDependency` 这个类重写了getParents方法
        ```scala
        /**
         * Represents a one-to-one dependency between partitions of the parent and child RDDs.
         * 表示父RDD和子RDD的分区之间的一对一依赖关系。
         */
        @DeveloperApi
        class OneToOneDependency[T](rdd: RDD[T]) extends NarrowDependency[T](rdd) {
          override def getParents(partitionId: Int): List[Int] = List(partitionId)
        }
        ```
        2. `org.apache.spark.RangeDependency` 用于union操作，合并两个RDD
        ```scala
        /**
         * :: DeveloperApi ::
         * Represents a one-to-one dependency between ranges of partitions in the parent and child RDDs.
         * @param rdd the parent RDD
         * @param inStart the start of the range in the parent RDD
         * @param outStart the start of the range in the child RDD
         * @param length the length of the range
         */
        @DeveloperApi
        class RangeDependency[T](rdd: RDD[T], inStart: Int, outStart: Int, length: Int)
          extends NarrowDependency[T](rdd) {
        
          override def getParents(partitionId: Int): List[Int] = {
            if (partitionId >= outStart && partitionId < outStart + length) {
              List(partitionId - outStart + inStart)
            } else {
              Nil
            }
          }
        }
        ```
## 检查点
尽管RDD中的Lineage信息可以用来恢复丢失的数据，但是对于Lineage较长的RDD来说，这种恢复也将会是比较耗时的。
1. 一般来说，我们可以对Lineage较长的、宽依赖较多的RDD采取检查点操作，集群中的节点故障可能会导致每个父RDD的数据丢失，这样会导致RDD的重新计算，我们可以把窄依赖的RDD的计算结果缓存起来以提高数据恢复的效率。
2. Spark提供 `cache()` 和 `checkpoint()` 算子来支持检查点操作。
3. 值得注意的是：RDD是只读的，所以不需要一致性维护而带来额外的开销，我们可以设置后台来执行检查点操作。
4. 子RDD时如何确定父RDD是否被checkpoint的呢，那是通过RDD中的 `dependencies: Seq[Dependency[_]]` 方法来确定，如果 `dependencies_` 这个属性不为null则代表父RDD被checkpoint过，为null则代表父RDD没有被checkpoint过。
```scala
  /**
   * Get the list of dependencies of this RDD, taking into account whether the
   * RDD is checkpointed or not.
   */
  final def dependencies: Seq[Dependency[_]] = {
    checkpointRDD.map(r => List(new OneToOneDependency(r))).getOrElse {
      if (dependencies_ == null) {
        dependencies_ = getDependencies
      }
      dependencies_
    }
  }
```
5. 当我们通过 `org.apache.spark.api.java.JavaSparkContext.checkpointFile[T](path: String): JavaRDD[T]` 方法从一个checkpoint文件中恢复一个RDD的时候，其实返回的是一个 `org.apache.spark.rdd.ReliableCheckpointRDD` 对象，这个RDD可以继续往checkpoint文件夹中继续缓存数据
```scala
  /**
   * Write an RDD partition's data to a checkpoint file.
   */
  def writePartitionToCheckpointFile[T: ClassTag](
      path: String,
      broadcastedConf: Broadcast[SerializableConfiguration],
      blockSize: Int = -1)(ctx: TaskContext, iterator: Iterator[T]) {
    val env = SparkEnv.get
    val outputDir = new Path(path)
    val fs = outputDir.getFileSystem(broadcastedConf.value.value)

    val finalOutputName = ReliableCheckpointRDD.checkpointFileName(ctx.partitionId())
    val finalOutputPath = new Path(outputDir, finalOutputName)
    val tempOutputPath =
      new Path(outputDir, s".$finalOutputName-attempt-${ctx.attemptNumber()}")

    val bufferSize = env.conf.getInt("spark.buffer.size", 65536)

    val fileOutputStream = if (blockSize < 0) {
      val fileStream = fs.create(tempOutputPath, false, bufferSize)
      if (env.conf.get(CHECKPOINT_COMPRESS)) {
        CompressionCodec.createCodec(env.conf).compressedOutputStream(fileStream)
      } else {
        fileStream
      }
    } else {
      // This is mainly for testing purpose
      fs.create(tempOutputPath, false, bufferSize,
        fs.getDefaultReplication(fs.getWorkingDirectory), blockSize)
    }
    val serializer = env.serializer.newInstance()
    val serializeStream = serializer.serializeStream(fileOutputStream)
    Utils.tryWithSafeFinally {
      serializeStream.writeAll(iterator)
    } {
      serializeStream.close()
    }

    if (!fs.rename(tempOutputPath, finalOutputPath)) {
      if (!fs.exists(finalOutputPath)) {
        logInfo(s"Deleting tempOutputPath $tempOutputPath")
        fs.delete(tempOutputPath, false)
        throw new IOException("Checkpoint failed: failed to save output of task: " +
          s"${ctx.attemptNumber()} and final output path does not exist: $finalOutputPath")
      } else {
        // Some other copy of this task must've finished before us and renamed it
        logInfo(s"Final output path $finalOutputPath already exists; not overwriting it")
        if (!fs.delete(tempOutputPath, false)) {
          logWarning(s"Error deleting ${tempOutputPath}")
        }
      }
    }
  }
```
5. 检查点会切断此RDD的检查点之前的所有Lineage和迭代关系，所以在 `org.apache.spark.rdd.ReliableCheckpointRDD` 中的compute方法只会返回对应HDFS的文件反序列化流的一个迭代器即可。
```scala
  /**
   * Read the content of the checkpoint file associated with the given partition.
   */
  override def compute(split: Partition, context: TaskContext): Iterator[T] = {
    val file = new Path(checkpointPath, ReliableCheckpointRDD.checkpointFileName(split.index))
    ReliableCheckpointRDD.readCheckpointFile(file, broadcastedConf, context)
  }
```
## Spark运行过程(Standalone)
1. 集群启动，Master节点和Worker节点启动，Worker节点向Master节点注册汇报自己的资源情况，并定期向Master发送心跳
2. 当Application运行到 `JavaSparkContext jsc = new JavaSparkContext(conf);` 时，Driver端就会向Master建立连接并申请资源
3. Master收到Driver的申请资源的请求后，开始调度资源
4. Master与Worker进行RPC通信，让Worker启动Executor
5. Worker去启动Executor，Executor启动线程池以用来执行Task，同时实例化BlockManager，Executor启动完毕之后，Executor跟Driver通信
6. 程序中触发Action，开始构建DAG，即有向无环图(调用Spark中的算子)
    1. DAG描述多个RDD的转换过程，任务执行时，可以按照DAG的描述，执行真正的计算(数据被操作的一个过程)
    2. DAG是有边界的，有开始(通过SparkContext或者JavaSparkContext创建)，有结束(触发Action，执行run job，一个完整的DAG就形成了)
    3. 一个Spark Application有多少个DAG，取决于触发了多少次Action，即一个DAG对应一个Job，每个Job之间的Stage划分是独立的
    4. 一个RDD只是描述了数据计算过程中的一个环节，而DAG由一个或多个RDD组成，描述了数据计算过程中的所有环节
    5. 一个DAG可能产生多种不同类型的Task，会有不同的阶段
2. DAGScheduler将DAG切分Stage，将Stage中生成的Task以**TaskSet**的形式组成**TaskScheduler**(切分的依据是Shuffle)
    1. DAGScheduler将一个DAG切分成一个或多个Stage，**切分的唯一依据就是是否产生了Shuffle(宽依赖)**，在切分完Stage之后，先提交之前的Stage，执行完之后再提交后面的Stage，Stage会产生Task，一个Stage中会包含很多业务逻辑相同的Task
    2. 为什么要进行Stage划分？
        一个复杂的业务逻辑(将多台机器上具有相同属性的数据聚合到同一台机器上，这个过程我们称之为Shuffle)需要分多个阶段，
        如果DAG中含有Shuffle，那就意味着前面阶段产生的结果后，才能去执行下一阶段，下一个阶段的计算是依赖上一个阶段的，
        在同一个Stage中会有多个算子合并在一起组成一条计算逻辑，这就是pipeline(即流水线：严格按照流程、顺序执行)，
        **在同一个Stage中也会有多个分区，有几个分区就会有几个Task，这些Task的业务逻辑都是相同的，只是要去计算的数据不相同，一个Task里面就会对应一个一条流水线，Task会并行的去执行流水线的中得业务逻辑**
    3. Shuffle的定义
        Shuffle的含义是洗牌,将数据打散，父RDD中的一个partition中的数据如果给了子RDD的多个partition这就会产生Shuffle，
        Shuffle的过程中会产生网络传输，但是有网络传输并不一定是Shuffle
    4. **注意：Spark会根据最后一个RDD从后往前进行依赖关系的推断，因为每个RDD中都会记录父RDD的信息，直到没有父RDD为止**
3. Stage中的Task会形成TaskSet，然后传递给TaskScheduler，TaskScheduler调度Task(根据资源情况将Task调度到相应的Executor中去执行)，由Dirver把具体的Task发送到Executor中去执行
4. Executor接受Task，首先将Task进行反序列化，然后将Task用一个实现了Runnable接口的实现类进行包装，然后将Task放入ThreadPool中去执行
## Spark中的对象实例化和序列化的一些总结
1. 序列化问题
    1. 什么时候对象需要实现序列化接口？
        >一个对象的在Driver端进行了初始化，并且Executor使用到了这个对象，Driver通过网络发送Task给Executor的时候，是通过网络发送给Task的，通过网络势必就要对对象的序列化和反序列化，那么这时对象就要实现序列化接口，以确保Driver可以正常序列化对象，把对象发送给Executor
        >如果对象的初始化是在Executor中完成的，那么在Driver在发送Task时将不会携带这个对象的信息的，那么这时这个对象是不会通过网络发送给Executor的，而是Executor自己初始化对象的，那么这时对象就不用实现序列化接口
    2. 如果一个对象在Driver端进行了初始化，在Driver把对象通过网络发送给Executor的时候，一个Executor中可能会有多个Task，发送到Executor的**象在每个Task中会有一份实例**，这样就是有几个Task就会有多少个对象实例
    3. 如果一个对象是在Executor端进行初始化的，那么在Executor在允许Task的时候，每允许一次算子，就会实例化一个对象实例，有多少条数据就有多少个实例对象，这样会浪费资源降低性能
    4. 如果是单例对象，在Driver端实例化和在Executor端进行实例化的唯一区别就是该单例对象是否需要实现序列化接口，单例对象在一个JVM进程中只有一份实例存在，不论是从Driver端发送到Executor还是Executor端自己初始化，在Executor的JVM进程中就只会有一份对象实例在内存中，但是在Executor进行初始化的效率应该比在Driver实例化然后发送到Executor端的效率要高，因为在Executor端实例化不需要走网络
    5. **其实一个对象或者一个变量是通过闭包的方式被调用的话，那么这些对象或变量就会通过网络发送给其他节点上进行装载，通过网络传输势必会涉及到序列化**
## Worker、Executor、Job、Stage、Task和Partition的关系
* 一个物理节点上可以有一个或多个Worker，Worker其实是一个JVM进程

* 一个Executor可以并行执行多个Task，**实际上Executor是一个计算对象**，**ExecutorBackend是真正的JVM进程**，在Spark@Yarn的模式下，
其进程名为CoarseGrainedExecutorBackend，一个CoarseGrainedExecutorBackend中有且仅有一个Executor对象，
Task是Executor进程中的一个线程，一个Task至少要独占用Executor中的一个Vcore

* Executor的个数是由 `--num-executors` 来指定，Executor中有多少个核心是有 `--executor-cores` 来指定的，
一个Task要占几个核心是有 `--conf spark.task.cores=1` 来配置，默认一个Task占用一个core

* 一个Application中的**`最大并行度 = Executor数目 * (每个Executor核心数 / 每个Task要占用的核心数)`**，
注意：如果Spark读取的是HDFS上的文件时，Spark 会按照一个 block 来划分分区，比如一个文件的大小时1345MB，
一个block块的大小是128MB，那么Spark会生成1345MB/128M=11个Partition

* 一个Job中会有一个或多个Stage，一个Stage中会有一个或多个Task，如果一次提交的Task过多，
超出了 **最大并行度=Executor数目 * (每个Executor核心数 / 每个Task要占用的核心数)** 的话，那么Task会被分批次执行，
每次执行总 cores 个任务，等有 cores 空闲下来的时候再去执行剩余的Task

* 如果一个Executor调用了一个工具类的静态方法，在Executor端调用，那就是在Executor端对这个类首次使用，
那么Executor所在的JVM进程就会去加载该类并进行初始化操作(该类所在jar应该是提前通过--jars参数来指定，并发送到了各个Executor中)，
因为是静态方法，是属于类的，所以会被加载到JVM的方法区中(对于HotSpot虚拟机来说)，也就只存在一份。

## Spark SQL笔记
1. 在Spark1.x中，使用SQLContext是SparkSQL的操作入口，在Spark2.x中，改用了SparkSession作为SparkSQL的操作入口
2. UDF(user define function):
    1. UDF：输入一行，返回一个结果，输入与输出是一对一的关系，
    2. UDTF：输入一行，返回多行数据，输入与输出是一对多的关系
    3. UDAF：输入多行，经过聚合(aggregate)之后返回一行，输入与输出是多对一的关系，count,sum等聚合函数是Spark自带的聚合函数，但是遇到复杂的业务逻辑时，需要我们自定义聚合函数
    4. UDF和UDAF的实例详情见代码zhengwei.spark.sparksql.SparkSQL中
3. 注意：**在创建Dataset时，如果使用的是Java的实体类进行Schema关联的时候，这个实体类的访问权限必须是 `public` 的否则会报Caused by: java.util.concurrent.ExecutionException: org.codehaus.commons.compiler.CompileException: File 'generated.java'错**
4. SparkSQL中的join操作是会产生大量shuffle的，最好避免join操作，**推荐使用广播变量+自定义函数的方式进行对数据的聚合**，这样会减少shuffle提高效率
5. Spark SQL中所产生的DataFrame和Dataset都是基于RDD的，DataFrame里面存放了结构化数据的描述信息，DataFrame有表头(表的描述信息)，描述了有多少列，每一列叫什么名字、什么类型和能不能为空即 `RDD+Schema=DataFrame`
6. DataFrame和Dataset既然都是基于RDD的，那么这两者都是RDD所具有的基本属性，transformation是懒加载的，action触发transformation的执行
7. Spark SQL可以读取不同的数据源，包括text,json,csv,parquet格式，也可以保存到不同的数据格式，包括text,json,csv以及parquet
    1. 在将Dataset结果保存为text格式的时候， `Dataset<Row>` 中的Row的Schema信息不能有多列，如果Schema中有多列的话，写入text文件的时候会报错，保存成text文件时，Schema只能由一列数据信息
    2. 对于保存为json、csv和parquet格式的文件，都是可以保存为多列的，都是可以记录简单的Schema信息的
    3. 对于读取parquet格式的文件，Spark SQL可以选择要读取的列，对于不用的列就不用读取了，可以减少数据量的读入，提高效率
8. 开窗函数
    * row_number()函数：就是按照某个字段分组，然后取另一个字段的前几个值，相当于分组取topN
    * 开窗函数的格式 `row_number() over (partition by xxx order by xxx)`
        1. 
    * 常用的开窗函数 `over` 有如下：
        1. 为每一条数据显示聚合信息：(聚合函数() over())
        2. 为每条数据提供分组的聚合函数结果(聚合函数() over(partition by 字段) as 别名)
        3. 与排名函数一起使用(row number() over(order by 字段) as 别名)
            * `ROW_NUMBER() OVER()` 
            * `RANK() OVER()`
            * `DENSE_RANK() OVER()`
            * `NTILE(n) OVER()`
9. RDD、DataFrame和Dataset
    1. RDD
        1. RDD时懒执行的不可变的，可以支持lambda表达式的并行"数据集合"
        2. RDD最大的好处是简单，API易于使用
        3. RDD的劣势是性能限制，它是一个JVM驻内存对象，这就决定了它存在GC的限制，和数据量增加时Java序列化成本的增加
    2. DataFrame
        1. 与RDD类似，DataFrame也是一个分布式数据容器。然而DataFrame更像传统数据库的二维表，除了数据之外，还记录了结构信息，即schema
        2. 性能要比RDD要高，减少数据读取以及执行计划优化
        3. 数据以二进制的方式存在于非堆内存中，节省了大量空间外，还摆脱了GC控制
        4. 在编译器不会进行类型检查，会导致允许出错
    3. Dataset
        1. 是DataFrame API的一个扩展，是Spark的最新的数据抽象
        2. Dataset是强类型的，会进行类型检查，具有和DataFrame一样的查询优化
        3. Dataset支持编解码器，当需要访问非堆上的数据时可以避免反序列化整个对象，提高了效率。
        4. Dataframe是Dataset的特列，DataFrame=Dataset[Row] ，所以可以通过as方法将Dataframe转换为Dataset。Row是一个类型，跟Car、Person这些的类型一样，所有的表结构信息我都用Row来表示。
10. SparkSQL之join总结
    1. Shuffle Hash Join
        * 在数据库常见模型中(例如星型模型或雪花模型)，表一般分为两种：事实表和维度表。维度表一般是固定的，变大较少的表，一般数据有限。而事实表一般记记录流水，通常随时间的增长而不断膨胀
        * 因为join操作是对两个表中key值相同的记录进行连接，在SparkSQL中，对两个表做join最直接的方式就是先根据key分区，再在每个分区中把key相同的记录拿出来做连接操作，这样就不可避免的会涉及到shuffle
        * 由于Spark是一个分布式的计算引擎，可以通过分区的形式将大批量的数据划分成n份较小的数据集进行并行计算，这种思想应用到Spark SQL join上便是Shuffle Hash Join，利用key相同必然被分到相同的分区，SparkSQL将较大的表分而治之，先将表划分成n个分区
        * 在一定大小的表中，SparkSQL从时空结合的角度来看，将两个表进行重新分区，并且对小表中的分区hash化，从而完成join，尽量减少driver和executor的内存压力，提升计算时的性能。
        * Shuffle Hash Join主要分两步：
            1. 对两张表按照join keys进行重新分区，即shuffle，目的是为了让具有相同join keys值的记录分到对应的分区中
            2. 对对应分区中的数据进行join，此处先将小表分区构造成一张hash表，然后根据大表中记录的join keys值拿出来进行匹配
    2. Broadcast Join
        * shuffle在spark中是比较耗时的操作，我们应该尽量避免shuffle操作
        * 当维度表和事实表进行join的时候，为了避免shuffle操作，我们可以将大小有限的维度表的全部数据广播到各个节点上，供事实表去使用，executor存储了全部的维度表数据，会牺牲一定的executor的存储空间，换取避免shuffle操作
        * Broadcast Join的条件如下
            1. 被广播的表要小于 `spark.sql.autoBroadcastJoinThreshold` 所配置的值，默认是10M
            2. 基表不能被广播，比如 `left join` 时，只能广播右表
            3. 一侧的表要明显小于另一侧表，小的一侧将被广播(明显小于定义的3倍)
        * 缺点：这个方案只适用于广播较小的表，因为倍广播的表首先要被collect到driver端，然后倍冗余分发到各个executor上，所以当表较大时，采用Broadcast Join这种方式会对driver和executor端造成较大压力，数据的冗余传输会远大于shuffle的开销；另外，频繁的广播，对driver的内存也是一种考验
    3. Sort Merge Join
        * 上面两种join方式对于一个大表一个小表的情形比较适用，但当两个表都非常大的时候，显然无论使用哪种方式都对内存造成巨大压力，这是因为两者采用的都是hash join，是将另一侧的数据完全加载到内存中，使用hash code取join keys值相等的记录进行连接
        * 当两张表都特别大时，Spark SQL采用一种全新的方案，即Sort Merge Join。这种方式不用将一侧数据全部加载后再进行hash join，但需要在join前对数据进行排序，因为两个序列是有序的，从头遍历，遇到相同的key就输出，如果不同，左边小就继续取左边，反之取右边。可以看出无论分区多大，Sort Merge Join都不用把某一侧的数据全部加载到内存中，而是即用即取即丢，从而大大提高了大数据量下sql的join操作。
## Spark内存模型
### Spark 1.6.x的时候通过**org.apache.spark.memory.StaticMemoryManager**，数据处理以及类的实体对象都存在JVM Heap中(具体的额JVM内存结构参见JVM.md)
1. JVM Heap的默认值是512M，这取决于 `spark.executor.memory` 参数决定，不论你定义了多大的 `spark.executor.memory` .Spark都必然会定义一个安全空间，在默认情况下只会使用Java Heap上的90%作为安全空间，在单个Executor的角度来看，就是 `Java Heap * 90%`
    * 场景一：假设在一个Executor，它可用的大小是10G，实际上Spark只能使用90%，这个安全空间的比例是由 `spark.storage.safetyFaction` 来控制的(如果内存非常大的话，可以考虑把这个比例调整为95%)，
    **在安全空间中也会被划分为三个不同的空间，一个是storage区，一个是unroll区和一个shuffle区**
    1. 安全空间(Safe)：计算公式是-> `spark.executor.memory * spark.storage.safetyFraction` 。也就是Heap Size * 90%，在本例中为10G * 09%=9G
    2. 缓存空间(Storage)：计算公式是-> `spark.executor.memory * spark.storage.safetyFraction * spark.storage.memoryFraction` 。也就是 `Heap Size * 90% * 60% -> Head Size * 54%`，在场景一中是10Gx54%=5.4G，一个应用程序能够缓存多少数据由safetyFraction和memoryFraction这两个参数共同决定
    ```scala
      /**
       * Return the total amount of memory available for the storage region, in bytes.
       * 获得缓存空间的内存
       */
      private def getMaxStorageMemory(conf: SparkConf): Long = {
        val systemMaxMemory = conf.getLong("spark.testing.memory", Runtime.getRuntime.maxMemory)
        val memoryFraction = conf.getDouble("spark.storage.memoryFraction", 0.6)
        val safetyFraction = conf.getDouble("spark.storage.safetyFraction", 0.9)
        (systemMaxMemory * memoryFraction * safetyFraction).toLong
      }
    ```
    3. Unroll空间：计算公式是-> `spark.executor.memory * spark.sotrage.safetyFraction * spark.storage.memoryFraction * spark.storage.unrollFraction` 也就是 `Heap Size * 90% * 60% * 20% = 1.8G` ，你可以把序列化的数据放入内存中，当你需要使用数据时，需要对数据进行反序列化
    ```scala
      // Max number of bytes worth of blocks to evict when unrolling
      //存储序列化的数据，Spark从此区域取出数据进行反序列化
      private val maxUnrollMemory: Long = {
        (maxOnHeapStorageMemory * conf.getDouble("spark.storage.unrollFraction", 0.2)).toLong
      }
    ```
    4. Shuffle空间(Execution空间)：计算公式是-> `spark.executor.memory * spark.shuffle.memoryFraction * spark.shuffle.safetyFraction` 。在shuffle空间中也会有一个默认80%的安全空间比例，所以应该是 `Heap Size * 20% * 80%` ，在本例中是 `10G * 20% * 80% = 1.6G` 这是shuffle的可用内存
    ```scala
    /**
     * Return the totSal amount of memory available for the execution region, in bytes.
     */
     private def getMaxExecutionMemory(conf: SparkConf): Long = {
        val systemMaxMemory = conf.getLong("spark.testing.memory", Runtime.getRuntime.maxMemory)
    
        if (systemMaxMemory < MIN_MEMORY_BYTES) {
          throw new IllegalArgumentException(s"System memory $systemMaxMemory must " +
            s"be at least $MIN_MEMORY_BYTES. Please increase heap size using the --driver-memory " +
            s"option or spark.driver.memory in Spark configuration.")
        }
        if (conf.contains("spark.executor.memory")) {
          val executorMemory = conf.getSizeAsBytes("spark.executor.memory")
          if (executorMemory < MIN_MEMORY_BYTES) {
            throw new IllegalArgumentException(s"Executor memory $executorMemory must be at least " +
              s"$MIN_MEMORY_BYTES. Please increase executor memory using the " +
              s"--executor-memory option or spark.executor.memory in Spark configuration.")
          }
        }
        val memoryFraction = conf.getDouble("spark.shuffle.memoryFraction", 0.2)
        val safetyFraction = conf.getDouble("spark.shuffle.safetyFraction", 0.8)
        (systemMaxMemory * memoryFraction * safetyFraction).toLong
     }
    ``` 
### Spark2.x的内存模型叫联合内存(**org.apache.spark.memory.UnifiedMemoryManager**)，数据缓存与数据执行之间的内存可以相互移动，这是一种更弹性的方式，数据处理以及类的实例存放在JVM中，Spark2.x中把内存划分成了3块：Reserved Memory,User Memory和Spark Memory
1. Reserved Memory：默认是300M，这个数字一般是固定不变的，在系统运行时，Java Heap Size的大小至少为 `Heap Reserved Memory * 1.5` 
> e.g. 300M * 1.5 = 450M的JVM配置，一般本地开发需要2G内存
```scala
  // Set aside a fixed amount of memory for non-storage, non-execution purposes.
  // This serves a function similar to `spark.memory.fraction`, but guarantees that we reserve
  // sufficient memory for the system even for small heaps. E.g. if we have a 1GB JVM, then
  // the memory used for execution and storage will be (1024 - 300) * 0.6 = 434MB by default.
  private val RESERVED_SYSTEM_MEMORY_BYTES = 300 * 1024 * 1024
```
2. User　Memory：写Spark应用程序中产生的临时变量或者是自己维护的一些数据结构也要给予他们一定的存储空间，我们可以把这部分空间认为是我们程序员自己主导内存口空间，叫用户操作空间，占用的空间是 `(Java Heap Size - Reserved Memory) * 25%` ，
                 25%是默认参数，可以通过参数进行调优，**这样设计的好处是可以让用户操作时需要的空间与系统框架运行时所需要的空间分开。**
> 假设Executor的大小有4G，那么在默认情况下User Memory的大小为(4G - 300M) * 25% = 949MB，也就是说一个Stage内部展开后Task的算子在运行时最大占用内存不能超过949MB。
> 例如工程师使用 mapPartition 等，一个 Task 内部所有算子使用的数据空间的大小如果大于 949MB 的话，那么就会出现 OOM。
> 思考题：有 100个 Executors 每个 4G 大小，现在要处理 100G 的数据，假设这 100G 分配给 100个 Executors，每个 Executor 分配 1G 的数据，这 1G 的数据远远少于 4G Executor 内存的大小，为什么还会出现 OOM 的情况呢？那是因为在你的代码中(e.g.你写的应用程序算子）超过用户空间的限制 (e.g. 949MB)，而不是 RDD 本身的数据超过了限制。
3. Spark Memory：系统框架在运行时需要的内存，这部分内存由两部分构成，分别是Storage Memory和Execution Memory。现在Storage和Execution(Shuffle)采用了Unified的方式共同使用了 `(Java Heap Size -300M) * 75%` ，默认情况下Storage和Execution各占50%，但是Storage和Execution的空间是可以互相移动的
> 定义：所谓 Unified 的意思是 Storage 和 Execution 在适当时候可以借用彼此的 Memory，需要注意的是，当 Execution 空间不足而且 Storage 空间也不足的情况下，
> Storage 空间如果曾经使用了超过 Unified 默认的 50% 空间的话则超过部份会被强制 drop 掉一部份数据来解决 Execution 空间不足的问题 (注意：drop 后数据会不会丢失主要是看你在程序设置的 storage_level 来决定你是 Drop 到那里，可能 Drop 到磁盘上)，这是因为执行(Execution) 比缓存 (Storage) 是更重要的事情。
```scala
  private def getMaxMemory(conf: SparkConf): Long = {
    val systemMemory = conf.getLong("spark.testing.memory", Runtime.getRuntime.maxMemory)
    val reservedMemory = conf.getLong("spark.testing.reservedMemory",
      if (conf.contains("spark.testing")) 0 else RESERVED_SYSTEM_MEMORY_BYTES)
    val minSystemMemory = (reservedMemory * 1.5).ceil.toLong
    if (systemMemory < minSystemMemory) {
      throw new IllegalArgumentException(s"System memory $systemMemory must " +
        s"be at least $minSystemMemory. Please increase heap size using the --driver-memory " +
        s"option or spark.driver.memory in Spark configuration.")
    }
    // SPARK-12759 Check executor memory to fail fast if memory is insufficient
    if (conf.contains("spark.executor.memory")) {
      val executorMemory = conf.getSizeAsBytes("spark.executor.memory")
      if (executorMemory < minSystemMemory) {
        throw new IllegalArgumentException(s"Executor memory $executorMemory must be at least " +
          s"$minSystemMemory. Please increase executor memory using the " +
          s"--executor-memory option or spark.executor.memory in Spark configuration.")
      }
    }
    val usableMemory = systemMemory - reservedMemory
    val memoryFraction = conf.getDouble("spark.memory.fraction", 0.6)
    (usableMemory * memoryFraction).toLong
  }
```
**SparkMemory空间默认是占可用 HeapSize 的 60%，与上图显示的75％有点不同，当然这个参数是可配置的！！**
## Spark Streaming
### kafka：分布式消息队列。具有高性能、持久化、多副本备份和横向扩展能力
1. kafka特性
    * 高吞吐、低延迟：kafka每秒可以处理几十万数据，每条消息的延迟只有几毫秒，每个topic可以分成多个partition，consumer group进行consumer操作
    * 可扩展性：kafka集群支持热扩展，支持横向扩展
    * 持久性、可靠性：消息被持久化到本地磁盘，并且支持数据备份防止数据丢失
    * 容错性：允许集群中的节点失效(若副本数量为n，最多容忍n-1个节点失效)
    * 高并发：支持数千个客户端同时读写
2. kafka设计思想
    1. kafka broker leader选举：kafka broker leader集群受zookeeper管理，
    2. consumer group：各个consumer(consumer线程)可以组成一个组(consumer group)，partition中的每个message只能被consumer group中的consumer(consumer线程)消费，如果一个message可以被多个consumer消费的话，那么这些consumer必须在不同的组。kafak不支持一个partition中的message由两个或两个以上的在同一个consumer group下的consumer thread来处理，除非在启动一个新的consumer group。**所以想要对同一个topic做消费的话，启动多个consumer group就行了，但是要注意的是，这里的多个consumer的消费必须是顺序读取partition中的message，新启动的consumer默认从partition队列的最开始进行读取message，同一个partition中的一条message只能被同一个consumer group中的一个consumer消费，不能够同一个consumer group的多个consumer同时消费一个partition。**一个consumer group中无论有多少个consumer，这consumer group一定会去把这个topic下的所有partition全部消费，如果consumer比partition多的话，那么会有consumer会在空闲状态；如果consumer的数量比partition少的话，就会出现一个consumer消费多个partition的情况，最好的情况是指定和partition数量一样的consumer的数量，这样的效率最高，一个consumer消费一个partition。我们在设定consumer group的时候，只需指明consumer group中有几个consumer即可，无需指定对应的消费partition序号，consumer会自动进行rebalance
    3. consumer rebalance的触发条件
        1. consumer增加或删除会触发consumer group
        2. broker的增加或减少也会触发consumer rebalance
    4. consumer：consumer处理partition中的message。顺序读取。所以必须维护着上一次读到哪里的offset信息。high level api将offset保存在zookeeper中，low level api将offset由自己维护
    5. Delivery Mode：Kafka producer 发送message不用维护message的offste信息，因为这个时候，offsite就相当于一个自增id，producer就尽管发送message就好了。
    6. Topic & Partition：Topic相当于传统消息系统MQ中的一个队列queue，producer端发送的message必须指定是发送到哪个topic，但是不需要指定topic下的哪个partition，因为kafka会把收到的message进行load balance，均匀的分布在这个topic下的不同的partition上(hash(message) % [broker数量])。物理上存储上，这个topic会分成一个或多个partition，每个partiton相当于是一个子queue。在物理结构上，每个partition对应一个物理的目录（文件夹），文件夹命名是[topicname]_[partition]_[序号]，一个topic可以有无数多的partition，根据业务需求和数据量来设置。在kafka配置文件中可随时更高num.partitions参数来配置更改topic的partition数量，在创建Topic时通过参数指定parittion数量。Topic创建之后通过Kafka提供的工具也可以修改partiton数量。
        1. 一个Topic的Partition数量大于等于Broker的数量，可以提高吞吐率。
        2. 同一个Partition的Replica尽量分散到不同的机器，高可用。
    7. 当add a new partition的时候：partition里面的message不会重新进行分配，原来的partition里面的message数据不会变，新加的这个partition刚开始是空的，随后进入这个topic的message就会重新参与所有partition的load balance
    8. Partition Replica：每个partition可以在其他的kafka broker节点上存副本，以便某个kafka broker节点宕机不会影响这个kafka集群。存replica副本的方式是按照kafka broker的顺序存。例如有5个kafka broker节点，某个topic有3个partition，每个partition存2个副本，那么partition1存broker1,broker2，partition2存broker2,broker3。。。以此类推**(replica副本数目不能大于kafka broker节点的数目，否则报错。这里的replica数其实就是partition的副本总数，其中包括一个leader，其他的就是copy副本)**。这样如果某个broker宕机，其实整个kafka内数据依然是完整的。但是，replica副本数越高，系统虽然越稳定，但是回来带资源和性能上的下降；replica副本少的话，也会造成系统丢数据的风险。
        1. 怎样传送消息：producer先把message发送到partition leader，再由leader发送给其他partition follower。（如果让producer发送给每个replica那就太慢了）
        2. 在向Producer发送ACK前需要保证有多少个Replica已经收到该消息：根据ack配的个数而定
        3. 怎样处理某个Replica不工作的情况：如果这个部工作的partition replica不在ack列表中，就是producer在发送消息到partition leader上，partition leader向partition follower发送message没有响应而已，这个不会影响整个系统，也不会有什么问题。如果这个不工作的partition replica在ack列表中的话，producer发送的message的时候会等待这个不工作的partition replca写message成功，但是会等到time out，然后返回失败因为某个ack列表中的partition replica没有响应，此时kafka会自动的把这个部工作的partition replica从ack列表中移除，以后的producer发送message的时候就不会有这个ack列表下的这个部工作的partition replica了。
        4. 怎样处理Failed Replica恢复回来的情况：如果这个partition replica之前不在ack列表中，那么启动后重新受Zookeeper管理即可，之后producer发送message的时候，partition leader会继续发送message到这个partition follower上。如果这个partition replica之前在ack列表中，此时重启后，需要把这个partition replica再手动加到ack列表中。（ack列表是手动添加的，出现某个部工作的partition replica的时候自动从ack列表中移除的）
    9.  Partition leader与follower：partition也有leader和follower之分。leader是主partition，producer写kafka的时候先写partition leader，再由partition leader push给其他的partition follower。partition leader与follower的信息受Zookeeper控制，一旦partition leader所在的broker节点宕机，zookeeper会冲其他的broker的partition follower上选择follower变为parition leader。**注意：broker是没有主从之分的，每个broker的地位是平等的**
    10. Topic分配partition和partition replica的算法
        1. 将Broker(size=n)和待分配的Partition排序。
        2. 将第i个Partition分配到第（i%n）个Broker上。
        3. 将第i个Partition的第j个Replica分配到第((i + j) % n)个Broker上
    11. 消息投递可靠性
        1. 啥都不管，发送出去就当作成功，这种情况当然不能保证消息成功投递到broker；
        2. Master-Slave模型，只有当Master和所有Slave都接收到消息时，才算投递成功，这种模型提供了最高的投递可靠性，但是损伤了性能；
        3. 只要Master确认收到消息就算投递成功；实际使用时，根据应用特性选择，绝大多数情况下都会中和可靠性和性能选择第三种模型
    12. 消息在broker上的可靠性：因为消息会持久化到磁盘上，所以如果正常stop一个broker，其上的数据不会丢失；但是如果不正常stop，可能会使存在页面缓存来不及写入磁盘的消息丢失，这可以通过配置flush页面缓存的周期、阈值缓解，但是同样会频繁的写磁盘会影响性能，又是一个选择题，根据实际情况配置。
    13. 消息消费可靠性：Kafka提供的是“At least once”模型，因为消息的读取进度由offset提供，offset可以由消费者自己维护也可以维护在zookeeper里，但是当消息消费后consumer挂掉，offset没有即时写回，就有可能发生重复读的情况，这种情况同样可以通过调整commit offset周期、阈值缓解，甚至消费者自己把消费和commit offset做成一个事务解决，但是如果你的应用不在乎重复消费，那就干脆不要解决，以换取最大的性能。
    14. Partition ack
        1. 当ack=1，表示producer写partition leader成功后，broker就返回成功，无论其他的partition follower是否写成功。
        2. 当ack=2，表示producer写partition leader和其他一个follower成功的时候，broker就返回成功，无论其他的partition follower是否写成功。
        3. 当ack=-1[parition的数量]的时候，表示只有producer全部写成功的时候，才算成功，kafka broker才返回成功信息。<br/>
        **这里需要注意的是，如果ack=1的时候，一旦有个broker宕机导致partition的follower和leader切换，会导致丢数据。**
    15. message状态：在Kafka中，消息的状态被保存在consumer中，broker不会关心哪个消息被消费了被谁消费了，只记录一个offset值（指向partition中下一个要被消费的消息位置），这就意味着如果consumer处理不好的话，broker上的一个消息可能会被消费多次
    16. message持久化：Kafka中会把消息持久化到本地文件系统中，并且保持O(1)极高的效率。由于message的写入持久化是顺序写入的，因此message在被消费的时候也是按顺序被消费的，保证partition的message是顺序消费的。
    17. message有效期：Kafka会长久保留其中的消息，以便consumer可以多次消费，当然其中很多细节是可配置的。
    18. Produer：Producer向Topic发送message，不需要指定partition，直接发送就好了。kafka通过partition ack来控制是否发送成功并把信息返回给producer，producer可以有任意多的thread，这些kafka服务器端是不care的。Producer端的delivery guarantee默认是At least once的。也可以设置Producer异步发送实现At most once。Producer可以用主键幂等性实现Exactly once
    19. Kafka高吞吐量：Kafka的高吞吐量体现在读写上，分布式并发的读和写都非常快，写的性能体现在以o(1)的时间复杂度进行顺序写入。读的性能体现在以o(1)的时间复杂度进行顺序读取， 对topic进行partition分区，consume group中的consume线程可以以很高能性能进行顺序读。
    20. Kafka delivery guarantee(message传送保证)：（1）At most once消息可能会丢，绝对不会重复传输；（2）At least once 消息绝对不会丢，但是可能会重复传输；（3）Exactly once每条信息肯定会被传输一次且仅传输一次，这是用户想要的。
    21. 批量发送：Kafka支持以消息集合为单位进行批量发送，以提高push效率。
    22. push-and-pull : Kafka中的Producer和consumer采用的是push-and-pull模式，即Producer只管向broker push消息，consumer只管从broker pull消息，两者对消息的生产和消费是异步的。
    23. Kafka集群中broker之间的关系：不是主从关系，各个broker在集群中地位一样，我们可以随意的增加或删除任何一个broker节点。
    24. 负载均衡方面： Kafka提供了一个 metadata API来管理broker之间的负载（对Kafka0.8.x而言，对于0.7.x主要靠zookeeper来实现负载均衡）。
    25. 同步异步：Producer采用异步push方式，极大提高Kafka系统的吞吐率（可以通过参数控制是采用同步还是异步方式）。
    26. 分区机制partition：Kafka的broker端支持消息分区partition，Producer可以决定把消息发到哪个partition，在一个partition 中message的顺序就是Producer发送消息的顺序，一个topic中可以有多个partition，具体partition的数量是可配置的。partition的概念使得kafka作为MQ可以横向扩展，吞吐量巨大。partition可以设置replica副本，replica副本存在不同的kafka broker节点上，第一个partition是leader,其他的是follower，message先写到partition leader上，再由partition leader push到parition follower上。所以说kafka可以水平扩展，也就是扩展partition。
    27. 离线数据装载：Kafka由于对可拓展的数据持久化的支持，它也非常适合向Hadoop或者数据仓库中进行数据装载。
    28. 实时数据与离线数据：kafka既支持离线数据也支持实时数据，因为kafka的message持久化到文件，并可以设置有效期，因此可以把kafka作为一个高效的存储来使用，可以作为离线数据供后面的分析。当然作为分布式实时消息系统，大多数情况下还是用于实时的数据处理的，但是当cosumer消费能力下降的时候可以通过message的持久化在淤积数据在kafka。
    29. 插件支持：现在不少活跃的社区已经开发出不少插件来拓展Kafka的功能，如用来配合Storm、Hadoop、flume相关的插件。
    30. 解耦:  相当于一个MQ，使得Producer和Consumer之间异步的操作，系统之间解耦
    31. 冗余:  replica有多个副本，保证一个broker node宕机后不会影响整个服务
    32. 扩展性:  broker节点可以水平扩展，partition也可以水平增加，partition replica也可以水平增加
    33. 峰值:  在访问量剧增的情况下，kafka水平扩展, 应用仍然需要继续发挥作用
    34. 可恢复性:  系统的一部分组件失效时，由于有partition的replica副本，不会影响到整个系统。
    35. 顺序保证性：由于kafka的producer的写message与consumer去读message都是顺序的读写，保证了高效的性能。
    36. 缓冲：由于producer那面可能业务很简单，而后端consumer业务会很复杂并有数据库的操作，因此肯定是producer会比consumer处理速度快，如果没有kafka，producer直接调用consumer，那么就会造成整个系统的处理速度慢，加一层kafka作为MQ，可以起到缓冲的作用。
    37. 异步通信：作为MQ，Producer与Consumer异步通信
3. Kafka文件存储机制
    1. kafka部分名词解释如下：
        1. 
1. topic & partition：生产者往topic中写入数据，消费者从topic中读取数据，为了做到水平扩展，一个topic实际是由多个partition组成的，遇到瓶颈时可以通过增加partition的数量来进行横向扩容，**单个partition确保消息有序**，topic相当于传统消息系统MQ中的一个队列queue，
2. partition：
3. 
### Spark Streaming:一种构建在Spark Core上的实时计算框架，它扩展了Spark处理大规模的流式数据的能力
1. 首先，Spark Streaming把实时输入的数据以时间片Δt(比如5秒)为单位切分成块，Spark Streaming会把每一块当成一个DStream(即离散的数据流)，我们可以把这个DStream看成是一系列连续的RDD集合，对DStream操作其实就是对DStream中的小RDD操作，每一个块都会生成一个DAG提交到Spark集群中去执行。
