# Spark专栏
* 学习和实验spark一些功能的时候写的一些代码
* RDD
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
            4. RDD的行动：行动算子用来触发转换算子的执行，一个application中有几个job取决于有几个行动算子
            5. RDD的输出：Spark可以将得到的结果输出到hdfs上，也可以时本地文件系统，还可以收集最终结果到一个集合中以供后续操作
* 算子
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
        15. cache()算子：缓存RDD算子，懒加载，不会生成 "新的" RDD，所生成的RDD只是对原RDD的引用，如果要**缓存的数据大于实际的内存的话，Spark只是会缓存一部分到内存，缓存一部分到磁盘中，cache是不会切断RDD的血统关系的**
            unpersist()算子，取消缓存
            ```java 
            JavaRDD<String> cache = rdd1.cache();
            JavaRDD<String> unpersist = cache.unpersist();
            ```
            cache实际调用的是 `persist()` 方法，`persist()` 方法默认的缓存级别是全存在内存中，不经如此， `persist()` 还提供不通的存储等级来满足需求，具体的存储等级如下：
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
    3. Spark会根据shuffle来划分Stage，同一个Stage中的Task的类型是相同的，MapTask或者是ResultTask
    4. 一共有多少个Task由Stage和Partition共同决定，读取hdfs中文件时，有多少个文件切片就会有多少个Partition，即就会有多少个Task，即这个Stage中一共有Task，如果后续的Stage中没有自己指定分区数的话，那么后续的Stage的分区将和之前的Stage中的分区保持一致，
       那一共有多少个`一个Job中Task总数=Stage的数量*一个Stage中Task的总数`.
    5. Shuffle分为两个步骤，分别是MapTask和ResultTask，MapTask会把根据分区器的规则把结果溢写到磁盘中然后把最后的结果反馈给Driver，ResultTask会去和Driver通信，获取到MapTask溢写的文件的位置，然后去拉取再做聚合
* 广播变量和累加器
    1. 广播变量
        1. **不能将一个RDD广播出去**，因为RDD中是不存储数据的，只存储计算逻辑，不过可以把RDD的极计算结果广播出去
        2. 广播变量只能在Driver进行定义，**不能再Executor端进行定义**
        3. 在Driver端可以修改广播变量的值，但是在Executor中不能修改广播变量的值
        4. 如果Executor端用到了Driver端的变量，如果没有使用广播变量的话那么有多少个Task就会有多少个变量的副本；如果使用了广播变量的话，那么不论有多少个Task，变量的副本始终是一份
        5. **广播变量是存储在Executor中的MemoryStore中的**
    2. 累加器  
        
* Spark运行过程
    1. 集群启动，Master节点和Worker节点启动，Worker节点向Master节点注册汇报自己的资源情况，并定期向Master发送心跳
    2. 当Application运行到 `JavaSparkContext jsc = new JavaSparkContext(conf);` 时，Driver端就会向Master建立连接并申请资源
    3. Master收到Driver的申请资源的请求后，开始调度资源
    4. Master与Worker进行RPC通信，让Worker启动Executor
    5. Worker去启动Executor，Executor启动线程池以用来执行Task，同时实例化BlockManager，Executor启动完毕之后，Executor跟Driver通信
    6. 程序中触发Action，开始构建DAG，即有向无环图(调用Spark中的算子)
        1. DAG描述多个RDD的转换过程，任务执行时，可以按照DAG的描述，执行真正的计算(数据被操作的一个过程)
        2. DAG是有边界的，有开始(通过SparkContext或者JavaSparkContext创建)，有结束(触发Action，执行run job，一个完整的DAG就形成了)
        3. 一个Spark Application有多少个DAG，取决于触发了多少次Action，即一个DAG对应一个Job
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
        4. 宽依赖与窄依赖
            1. 宽依赖：父RDD中的一个partition给了子RDD的**多个**partition，父RDD中的一个partition对应子RDD中的多个partition
            2. 窄依赖：父RDD中的一个partition给了子RDD的**一个**partition，父RDD中的partition与子RDD中的partition是一一对应的
        5. 注意：Spark会根据最后一个RDD从后往前进行依赖关系的推断，因为每个RDD中都会记录父RDD的信息，知道没有父RDD为止
    3. Stage中的Task会形成TaskSet，然后传递给TaskScheduler，TaskScheduler调度Task(根据资源情况将Task调度到相应的Executor中去执行)，由Dirver把具体的Task发送到Executor中去执行
    4. Executor接受Task，首先将Task进行反序列化，然后将Task用一个实现了Runnable接口的实现类进行包装，然后将Task放入ThreadPool中去执行
* Spark中的对象实例化和序列化的一些总结
    1. 序列化问题
        1. 什么时候对象需要实现序列化接口？
            >一个对象的在Driver端进行了初始化，并且Executor使用到了这个对象，Driver通过网络发送Task给Executor的时候，是通过网络发送给Task的，通过网络势必就要对对象的序列化和反序列化，那么这时对象就要实现序列化接口，以确保Driver可以正常序列化对象，把对象发送给Executor
            >如果对象的初始化是在Executor中完成的，那么在Driver在发送Task时将不会携带这个对象的信息的，那么这时这个对象是不会通过网络发送给Executor的，而是Executor自己初始化对象的，那么这时对象就不用实现序列化接口
        2. 如果一个对象在Driver端进行了初始化，在Driver把对象通过网络发送给Executor的时候，一个Executor中可能会有多个Task，发送到Executor的**象在每个Task中会有一份实例**，这样就是有几个Task就会有多少个对象实例
        3. 如果一个对象是在Executor端进行初始化的，那么在Executor在允许Task的时候，每允许一次算子，就会实例化一个对象实例，有多少条数据就有多少个实例对象，这样会浪费资源降低性能
        4. 如果是单例对象，在Driver端实例化和在Executor端进行实例化的唯一区别就是该单例对象是否需要实现序列化接口，单例对象在一个JVM进程中只有一份实例存在，不论是从Driver端发送到Executor还是Executor端自己初始化，在Executor的JVM进程中就只会有一份对象实例在内存中，但是在Executor进行初始化的效率应该比在Driver实例化然后发送到Executor端的效率要高，因为在Executor端实例化不需要走网络
* Worker、Executor、Job、Stage、Task和Partition的关系
    * 一个物理节点上可以有一个或多个Worker，Worker起始是一个JVM进程
    * 一个Executor可以并行执行多个Task，实际上Executor是一个JVM进程，Task是Executor进程中的一个线程，一个Task至少要独占用Executor中的一个vcore
    * Executor的个数是由 `--num-executors` 来指定，Executor中有多少个核心是有 `--executor-cores` 来指定的，一个Task要占几个核心是有 `--conf spark.task.cores=1` 来配置，默认一个Task占用一个core
    * 一个Application中的**`最大并行度=Executor数目*(每个Executor核心数/每个Task要占用的核心数)`**，注意：如果Spark读取的是HDFS上的文件时，Spark会按照一个block来划分分区，比如一个文件的大小时1345MB，一个block块的大小是128MB，那么Spark会生成1345MB/128M=11个Partition
    * 一个Job中会有一个或多个Stage，一个Stage中会有一个或多个Task，如果一次提交的Task过多，超出了**`最大并行度=Executor数目*(每个Executor核心数/每个Task要占用的核心数)`**的话，那么Task会被分批次执行，每次执行总cores个任务，等有cores空闲下来的时候再去执行剩余的Task