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
            A list of partitions (一系列分区，分区有编号，有顺序)
            A function for computing each split (每一个切片都会有一个函数作业在上面用于对数据进行处理)
            A list of dependencies on other RDDs (RDD与RDD之间存在依赖关系，形成linkage血统)
            Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned) (可选，key-value形式RDD才会有RDD[(K,V)])，如果是KV形式的RDD，会有一个分区器，默认是hash-partition)
            Optionally, a list of preferred locations to compute each split on (e.g. block locations foran HDFS file) (可以师从hdfs中获取数据，会得到数据的最优位置(向Name Node请求元数据))
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
            19/06/30 13:51:11 INFO DAGScheduler: Job 0 finished: collectAsMap at SparkTransformationAndActionOperator.java:128, took 0.270322 s
            zhengwei1->200
            zhengwei3->300
            zhengwei2->250
            ```
        3. countByKey()算子：对key出现的次数进行统计
        4. countByValue()算子：对value出现的次数进行统计
        5. foreach(func)
        6. foreachPartition(func)算子：每次取出一整个partition中的数据，比foreach要高效，saveAsTextFile底层调用的就是foreachPartition，所以是有几个分区就会有几个文件
* 一个partition对应一个task，一个task对应一个输出文件
* Executor中包含Task
* Driver默认只收集1G的数据，超过1G就不再收集了
* 最好不要将结果通过collect收集到Driver端，可以直接在算子中直接写入到需要的存储系统中(redis,hbase,mysql...)，这样可以提高效率，不会对Driver造成冲击
* saveAsTestFile底层