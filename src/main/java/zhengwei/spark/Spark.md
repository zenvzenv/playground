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
            4. RDD的行动：行动算子用来触发转换算子的执行，一个application中有几个job取决于有几个行动算子，**有几个Action算子就会有几个Job，Job与Job之间的Stage是不共用的，每个Job的Stage划分是独立划分的。**
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
* 如果一个Executor调用了一个工具类的静态方法，在Executor端调用，那就是在Executor端对这个类首次使用，那么Executor所在的JVM进程就会去加载该类并进行初始化操作(该类所在jar应该是提前通过--jars参数来指定，并发送到了各个Executor中)，
  因为是静态方法，是属于类的，所以会被加载到JVM的方法区中(对于Hotspot虚拟机来说)，也就只存在一份。
* Spark SQL笔记
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
* Spark内存模型
    1. Spark 1.6.x的时候通过**StaticMemoryManager**，数据处理以及类的实体对象都存在JVM Heap中(具体的额JVM内存结构参见JVM.md)
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
            3. Unroll空间：计算公式是-> `spark.executor.memory * saprk.sotrage.safetyFraction * spark.storage.memoryFraction * spark.storage.unrollFraction` 也就是 `Heap Size * 90% * 60% * 20% = 1.8G` ，你可以把序列化的数据放入内存中，当你需要使用数据时，需要对数据进行反序列化
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
        2. Spark2.x的内存模型叫联合内存(Spark Unified Memory)，数据缓存与数据执行之间的内存可以相互移动，这是一种更弹性的方式，数据处理以及类的实例存放在JVM中，Spark2.x中把内存划分成了3块：Reserved Memory,User Memory和Spark Memory
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
10. Spark Streaming
    1. kafka：分布式消息队列。具有高性能、持久化、多副本备份和横向扩展能力
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
    2. Spark Streaming
        1. 