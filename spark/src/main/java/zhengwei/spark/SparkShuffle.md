# SparkShuffle
对于spark来说，shuffle是很重要的，shuffle是两个stage的桥梁
## 对比Hadoop MapReduce和Spark的shuffle过程
如果熟悉Hadoop MapReduce 中的shuffle 过程，可能会按照MapReduce 的思路去想象Spark 的shuffle 过程。然而，它们之间有一些区别和联系。  
**high-level**：两者并无太大区别，都是将mapper(Spark中是ShuffleMapTask)的输出数据进行partition，不同的partition会被送到不同的
reducer(Spark中reducer可能是下一个stage里面的ShuffleMapTask，也可能是ResultTask)。reducer以内存作为缓冲区，边shuffler边
aggregate数据，等数据aggregate好以后进行reduce()(Spark可能是后续一些操作)
**low-level**：Hadoop的MapReduce的sort-based，进入combine()和reduce()的records必须先sort。这样的好处在于combine/reduce可以
处理大规模数据，因为其输入的数据可以通过**外排**(mapper先对每段数据先做排序，reducer的shuffle对每段排好序的每段数据做归并)。目前
的spark使用ShuffleManager来管理shuffle  
如果我们将map端划分数据、持久化数据的过程称为shuffle write，而将reducer读入数据、aggregate数据的过程称为shuffle read。
那么在Spark中，**问题就变为怎么在job的逻辑或者物理执行图中加入shuffle write和shuffle read的处理逻辑？
以及两个处理逻辑应该怎么高效实现？**
## HashShuffle
shuffle write的任务很简单：将数据partition好，并持久化，之所以要持久化，一方面减少内存的压力，另一方面是为了fault-tolerance。
### Shuffle Write
shuffle write的任务很简单，那么实现也很简单：该stage的finalRDD每输出一个record就将其partition并持久化，如下图所示：
![shuffle-write-no-consolidation](src\main\resources\mdimage\shuffle-write-no-consolidation.png)
上图有4个ShuffleMapTask要在同一个work node上运行，CPU core为2，可以同事运行两个task，每个task的执行结果(该stage的finalRDD中某个
partition包含的records)被逐一写到磁盘上，每个task包含R个缓冲区，R=reducer的个数(也就是下一个stage中task的个数)，
**缓冲区被成为bucket**，其大小为 `spark.shuffle.file.buffer` ，默认大小为32kb。
>bucket是一个广义的概念，代表ShuffleMapTask输出结果经过partition后要存放的地方，这里为了细化存放位置和数据名称，仅仅用bucket表示
>缓冲区

ShuffleMapTask的执行过程很简单：先利用pipeline计算得到finalRDD中对应partition的record。每得到一个record就送往对应的bucket，具体
是哪个bucket由 `partitioner.partition(record.getKey())` 决定，每个bucket里面的数据会不断的写入到本地磁盘上，
形成一个ShuffleBlockFile，或者简称**FileSegment**，之后的reducer会区fetch属于自己的FileSegment，进入shuffle read阶段。
#### 几个问题
1. **产生的FileSegment数量过多**。每个ShuffleMapTask产生R个(reducer个数)FileSegment个数，M个ShuffleMapTask就会产生 `M * R` 
个文件，一般SparkJob的M和R都很大，因此磁盘上会生成大量的数据文件
2. **缓冲区占用内存大**。每个ShuffleMapTask创建R个bucket，M个ShuffleMapTask就会产生 `M * R` 个bucket，虽然一个ShuffleMapTask
结束后，对应的bucket就可以被回收了，但一个work node上同时存在的bucket个数可以达到 `cores * R` (一般work同时运行cores个ShuffleMapTask)
占用的内存也就达到了 `cores * R * 32kb` 内存。对于8核1000个reducer的任务来说需要256mb的内存大小
#### 改进
对于第二个问题还没有好的解决方法，因为写磁盘终究是要开缓冲区的，缓冲区太小会影响IO性能。但第一个问题有一些方法可以解决，即解决产生过多
的FileSegment问题，下面介绍Spark的FileConsolidation方法，如下图所示：
![shuffle-write-consolidation](src\main\resources\mdimage\shuffle-write-consolidation.png)
可以明显看出，在一个core上连续执行的ShuffleMapTask可以公用一个输出文件ShuffleFile，先执行完的ShuffleMapTask形成ShuffleBlock i，
后面执行的ShuffleMapTask可以输出追加到ShuffleBlock i后面，形成ShuffleBlock i'，每个ShuffleBlock被成为**FileSegment**，下一个
stage的reducer只需要fetch整个FileSegment即可。这样每个core上的文件数量就从 `M * R` 个降低到 `cores * R` 个，FileConsolidation
可以通过 `spark.shuffle.consolidation = true` 来开启
### Shuffle Read
要计算ShuffleRDD中的数据，必须把MapPartitionsRDD中的数据fetch过来，那么问题来了：
1. 什么时候去fetch数据，parent RDD中的一个ShuffleMapTask执行完还是等全部的ShuffleMapTask执行完？
2. 边fetch边处理，还是一次性全部fetch过来再处理？
3. fetch过来的数据要存放到哪里？
4. 怎么获取要fetch的数据的存放位置？
#### 问题解决
1. **什么时候fetch？**，当parent stage的所有ShuffleMapTask全部结束之后再fetch。理论上讲，一个ShuffleMapTask结束后就可以fetch，
但为了迎合stage的概念(即一个stage如果parent stage没有执行，自己市不能被提交执行的)，还是选择全部ShuffleMapTask执行完再去fetch。因
为fetch过来的FileSegment要在内存中做缓冲，所以一次fetch过来的FileSegment总大小不能太大。spark规定这个缓冲区界限不能超过
`spark.reducer.maxMbInFlight` 这里用**softBuffer**表示，默认48M。一个sortBuffer里一般包含多个FileSegment，但如果某个
FileSegment特别大的话，就会填满甚至超出softBuffer界限
2. **边fetch边处理还是一次性fetch完再处理？** 边fetch边处理。本质上，MapReduce的Shuffle节点就是边fetch边使用combine()处理的，
只是combine处理的是部分数据。MapReduce为了让进入reduce()的records有序，必须等到全部数据都shuffle-sort之后再reduce()。因为Spark
不要求Shuffle后的数据全局有序，因此没必要等到全部数据完成后再处理。**那么如何实现边shuffle边处理，而且流入的是无序的records？**
答案是使用可以aggregate的数据结构，比如HashMap，每shuffle得到(从缓冲的FileSegment中deserialize出来)一个record，直接放进其HashMap
里面，如果该HashMap已经存在了key，那么直接aggregate也就是 `func(hashMap.get(key), value)`，比如WordCount例子，func就是
`hashMap.get(key) + value`，并将func的结果重新put回HashMap中去，这个func功能上相当于reduce()，但实际处理数据的方式与MapReduce 
reduce()有差别，差别相当于下面两段程序
```java
// MapReduce
reduce(K key, Iterable<V> values) { 
  result = process(key, values)
  return result    
}

// Spark
reduce(K key, Iterable<V> values) {
  result = null 
  for (V value : values) 
      result  = func(result, value)
  return result
}
```
MapReduce可以在process函数中定义任意的数据结构，也可以将部分或全部values都cache到缓存中后再进行处理，很灵活。而Spark的func的入参是
固定的，一个是上一个record的处理结果，另一个是当前要读入的record，他们经过func处理过后的结果将被下一个record处理时使用。因此一个算法
比如求平均数，在process中就很好实现，直接 `sum(values) / values.length` ，而在Spark中func可以实现 `sum(values)` ，但不好实现
`/ values.length`。
3. **fetch过来的数据要存在哪里？** 刚fetch过来的FileSegment存放在softBuffer中，经过处理后的文件存放在内存+磁盘中，
这里我们主要讨论处理后的数据，可以灵活设置这些数据是“只用内存”还是“内存＋磁盘”。如果`spark.shuffle.spill = false`就只用内存。
内存使用的是`AppendOnlyMap`，类似Java的HashMap，内存+磁盘使用的是`ExternalAppendOnlyMap`，如果内存空间不足时，
`ExternalAppendOnlyMap`可以将records进行sort后spill到磁盘上，等到需要它们的时候再进行归并。
4. **怎么获得要fetch的数据存放位置？** 在划分stage时，一个ShuffleMapStage形成后，会将该stage最后一个final RDD注册到
`MapOutputTrackerMaster.registerShuffle(shuffleId, rdd.partitions.size)`，这一步很重要，
因为shuffle过程需要MapOutputTrackerMaster来指示ShuffleMapTask输出数据的位置。因此，
reducer在shuffle的时候是要去driver里面的MapOutputTrackerMaster询问ShuffleMapTask输出的数据位置的。
每个ShuffleMapTask完成时会将FileSegment的存储位置信息汇报给MapOutputTrackerMaster。
## SortShuffleManager
SortShuffleManager主要有两种机制，一种是普通机制，另一种是bypass机制，当Shuffle Read Task数量小于
`spark.shuffle.sort.bypassMergeThreshold` 参数时(默认200)，就会启用bypass
### 普通机制
在该模式下，数据会先写入一个内存数据结构中，此时根据不同的shuffle算子，可能选用不同的数据结构。如果reduceByKey这种聚合类的算子，那么
会选择Map数据结构，一边通过Map进行聚合，一边写入内存；如果是join这种普通的shuffle算子，那么会选用Array数据结构，直接写入内存，接着，
每写入一条数据进入内存数据结构后，就会判断一下，是否达到了某个临界阈值，如果达到了阈值，那么就会尝试将内存数据结构溢写入到磁盘，然后清
空数据结构。  
在溢写到磁盘文件之前，会先根据key对内存数据结构中已有的数据结构进行排序。排序之后，会分批将数据写入磁盘文件，默认的batch数量为10_000条，
也就是说，排好序之后，每次会以没批1w条数据的形式写入磁盘文件。写入磁盘文件是通过Java的BufferedOutputStream实现的。
BufferedOutputStream是Java的缓冲输出流，首先会将数据缓冲到内存，当内存缓冲溢出之后一次性写出到磁盘文件，这样可以减少磁盘IO提升性能。   
一个task将所有数据写入内存数据结构的过程中，会发生多次磁盘溢写操作，也就会产生多个临时文件。最后会将所有的临时磁盘文件都进行合并，这就
是merge过程，此时会将之前所有临时磁盘文件中的数据读出来，然后依次写入到最终的磁盘文件中。此外，一个task只会对应一个磁盘文件，也就意味着
该task为下游stage中的task准备的数据都在这一个文件中，因此还需要一个索引文件，其中标识了下游各个task的数据在文件的start offset和end 
offset。  
SortShuffleManager由于有一个磁盘文件merge过程，因此大大减少了文件数量。比如第一个stage有50个task，总共有10个Executor，每个Executor
上运行5个task，而第二个stage有100个task。由于每个task最终生成一个磁盘文件，因此此时每个Executor上有5个磁盘文件，10个Executor有50个
磁盘文件。
![common_sort_shuffle_manager](src\main\resources\mdimage\common_sort_shuffle_manager.png)
### bypass机制
bypass触发的条件如下：
* shuffle map task数量小于`spark.shuffle.sort.bypassMergeThreshold`参数的值
* 不是聚合类的shuffle算子(如reduceByKey)
此时task会为每个下游task都创建一个临时磁盘文件，并将key按hash写入到不同的磁盘文件中。当然，写入磁盘文件时也是先写入内存缓冲中，缓冲写
慢之后再溢写到磁盘文件，最后，同样会将所有临时磁盘文件合并成功一个磁盘文件，并生成一个索引文件。  
该过程和未经优化的HashShuffleManager时一模一样的，因为都要创建数量惊人的磁盘文件，只是在最后会将所有临时文件合并成一个磁盘文件，因此
少量的最终文件，也让该机制相比未经优化的HashShuffleManager来说shuffle read性能要好。  
该机制与普通机制的不同之处在于：
1. 磁盘写机制不同
2. 不会排序，也就是说，在启用此机制最大的好处就是shuffle read过程中，不需要进行数据的排序，也就节省了这部分的性能开销
![bypass_sort_shuffle](src\main\resources\mdimage\bypass_sort_shuffle.png)
## shuffle调优相关参数
### spark.shuffle.file.buffer
* 默认值：32k
* 参数说明：该参数用于设置shuffle write task的BufferedOutputStream的buffer缓冲大小。将数据写到磁盘文件之前，会先写入buffer缓冲中，
待缓冲写满之后，才会溢写到磁盘。
* 调优建议：如果作业可用的内存资源较为充足的话，可以适当增加这个参数的大小（比如64k），从而减少shuffle write过程中溢写磁盘文件的次数，
也就可以减少磁盘IO次数，进而提升性能。在实践中发现，合理调节该参数，性能会有1%~5%的提升。
### spark.reducer.maxSizeInFlight
* 默认值：48m
* 参数说明：该参数用于设置shuffle read task的buffer缓冲大小，而这个buffer缓冲决定了每次能够拉取多少数据。
* 调优建议：如果作业可用的内存资源较为充足的话，可以适当增加这个参数的大小（比如96m），从而减少拉取数据的次数，
也就可以减少网络传输的次数，进而提升性能。在实践中发现，合理调节该参数，性能会有1%~5%的提升。
### spark.shuffle.io.maxRetries
* 默认值：3
* 参数说明：shuffle read task从shuffle write task所在节点拉取属于自己的数据时，如果因为网络异常导致拉取失败，是会自动进行重试的。
该参数就代表了可以重试的最大次数。如果在指定次数之内拉取还是没有成功，就可能会导致作业执行失败。
* 调优建议：对于那些包含了特别耗时的shuffle操作的作业，建议增加重试最大次数（比如60次），以避免由于JVM的full gc或者网络不稳定等因素
导致的数据拉取失败。在实践中发现，对于针对超大数据量（数十亿~上百亿）的shuffle过程，调节该参数可以大幅度提升稳定性。
### spark.shuffle.io.retryWait
* 默认值：5s
* 参数说明：具体解释同上，该参数代表了每次重试拉取数据的等待间隔，默认是5s。
* 调优建议：建议加大间隔时长（比如60s），以增加shuffle操作的稳定性。
### spark.shuffle.memoryFraction
* 默认值：0.2
* 参数说明：该参数代表了Executor内存中，分配给shuffle read task进行聚合操作的内存比例，默认是20%。
* 调优建议：在资源参数调优中讲解过这个参数。如果内存充足，而且很少使用持久化操作，建议调高这个比例，给shuffle read的聚合操作更多内存，
以避免由于内存不足导致聚合过程中频繁读写磁盘。在实践中发现，合理调节该参数可以将性能提升10%左右。
### spark.shuffle.manager
* 默认值：sort
* 参数说明：该参数用于设置ShuffleManager的类型。Spark 1.5以后，有三个可选项：hash、sort和tungsten-sort。
HashShuffleManager是Spark 1.2以前的默认选项，但是Spark 1.2以及之后的版本默认都是SortShuffleManager了。tungsten-sort与sort类似，
但是使用了tungsten计划中的堆外内存管理机制，内存使用效率更高。
* 调优建议：由于SortShuffleManager默认会对数据进行排序，因此如果你的业务逻辑中需要该排序机制的话，
则使用默认的SortShuffleManager就可以；而如果你的业务逻辑不需要对数据进行排序，那么建议参考后面的几个参数调优，
通过bypass机制或优化的HashShuffleManager来避免排序操作，同时提供较好的磁盘读写性能。这里要注意的是，
tungsten-sort要慎用，因为之前发现了一些相应的bug。
### spark.shuffle.sort.bypassMergeThreshold
* 默认值：200
* 参数说明：当ShuffleManager为SortShuffleManager时，如果shuffle read task的数量小于这个阈值（默认是200），
则shuffle write过程中不会进行排序操作，而是直接按照未经优化的HashShuffleManager的方式去写数据，
但是最后会将每个task产生的所有临时磁盘文件都合并成一个文件，并会创建单独的索引文件。
* 调优建议：当你使用SortShuffleManager时，如果的确不需要排序操作，那么建议将这个参数调大一些，大于shuffle read task的数量。
那么此时就会自动启用bypass机制，map-side就不会进行排序了，减少了排序的性能开销。
但是这种方式下，依然会产生大量的磁盘文件，因此shuffle write性能有待提高。
### spark.shuffle.consolidateFiles
* 默认值：false
* 参数说明：如果使用HashShuffleManager，该参数有效。如果设置为true，那么就会开启consolidate机制，
会大幅度合并shuffle write的输出文件，对于shuffle read task数量特别多的情况下，这种方法可以极大地减少磁盘IO开销，提升性能。
* 调优建议：如果的确不需要SortShuffleManager的排序机制，那么除了使用bypass机制，
还可以尝试将spark.shuffle.manager参数手动指定为hash，使用HashShuffleManager，同时开启consolidate机制。在实践中尝试过，
发现其性能比开启了bypass机制的SortShuffleManager要高出10%~30%。
## Shuffle中的HashMap
HashMap是Spark shuffle read过程中频繁使用的、用于aggregate的数据结构。Spark设计了两种：一种是全内存的AppendOnlyMap，
另一种是内存＋磁盘的ExternalAppendOnlyMap。下面我们来分析一下两者特性及内存使用情况。
### AppendOnlyMap
AppendOnlyMap 的官方介绍是 A simple open hash table optimized for the append-only use case, where keys are never removed,
but the value for each key may be changed。意思是类似 HashMap，但没有remove(key)方法。其实现原理很简单，开一个大 Object 数组，
蓝色部分存储 Key，白色部分存储 Value。  
当要 put(K, V) 时，先 hash(K) 找存放位置，如果存放位置已经被占用，就使用 Quadratic probing 探测方法来找下一个空闲位置。
对于图中的 K6 来说，第三次查找找到 K4 后面的空闲位置，放进去即可。get(K6)的时候类似，找三次找到 K6，取出紧挨着的V6，
与先来的value做func，结果重新放到V6的位置。  
迭代 AppendOnlyMap 中的元素的时候，从前到后扫描输出。  
如果 Array 的利用率达到**70%**，那么就**扩张一倍**，并对所有 key 进行 rehash 后，重新排列每个key的位置。  
AppendOnlyMap 还有一个 `destructiveSortedIterator(): Iterator[(K, V)]` 方法，可以返回Array中排序后的(K, V)pairs。
实现方法很简单：先将所有(K, V)pairs compact到Array的前端，并使得每个(K, V)占一个位置(原来占两个)，
之后直接调用Array.sort()排序，不过这样做会破坏数组(key的位置变化了)。
### ExternalAppendOnlyMap
相比AppendOnlyMap，ExternalAppendOnlyMap的实现略复杂，但逻辑其实很简单，类似Hadoop MapReduce中的shuffle-merge-combine-sort过程：
ExternalAppendOnlyMap持有一个AppendOnlyMap，shuffle来的一个个(K, V)record先insert到AppendOnlyMap中，
insert过程与原始的AppendOnlyMap一模一样。**如果AppendOnlyMap快被装满时检查一下内存剩余空间是否可以够扩展，够就直接在内存中扩展，
不够就sort一下AppendOnlyMap，将其内部所有records 都spill到磁盘上**。图中spill了4次，每次spill完在磁盘上生成一个spilledMap文件，
然后重新new出来一个AppendOnlyMap。最后一个(K, V)record insert到AppendOnlyMap后，
表示所有shuffle来的records都被放到了ExternalAppendOnlyMap中，但不表示records已经被处理完，因为每次insert的时候，
新来的record只与AppendOnlyMap中的records进行aggregate，并不是与所有的records进行aggregate(一些records 已经被spill到磁盘上了)。
因此当需要 aggregate 的最终结果时，需要对 AppendOnlyMap 和所有的 spilledMaps 进行全局 merge-aggregate。  
**全局merge-aggregate的流程也很简单**：先将AppendOnlyMap中的records进行sort，形成sortedMap。然后利用DestructiveSortedIterator
和DiskMapIterator分别从sortedMap和各个spilledMap读出一部分数据(StreamBuffer)放到mergeHeap里面。
StreamBuffer里面包含的records需要具有相同的hash(key)，所以图中第一个spilledMap只读出前三个records进入StreamBuffer。
mergeHeap顾名思义就是使用堆排序不断提取出hash(firstRecord.Key)相同的StreamBuffer，并将其一个个放入mergeBuffers中，
放入的时候与已经存在于mergeBuffers中的StreamBuffer进行merge-combine，第一个被放入mergeBuffers的StreamBuffer被称为minBuffer，
那么minKey就是minBuffer中第一个record的key。当merge-combine的时候，与minKey相同的records被aggregate一起，然后输出。
整个merge-combine在mergeBuffers中结束后，StreamBuffer剩余的records随着StreamBuffer重新进入mergeHeap。
一旦某个StreamBuffer在merge-combine后变为空(里面的records都被输出了)，
那么会使用 DestructiveSortedIterator或DiskMapIterator重新装填hash(key)相同的records，然后再重新进入mergeHeap。

整个 insert-merge-aggregate 的过程有三点需要进一步探讨一下：
* 内存剩余空间检测
为此，executor专门持有一ShuffleMemoryMap:HashMap[threadId, occupiedMemory]来监控每个reducer中ExternalOnlyAppendMap占用的内存量。
每当 AppendOnlyMap 要扩展时，都会计算ShuffleMemroyMap持有的所有reducer 中的AppendOnlyMap已占用的内存+扩展后的内存是会否会大于内存限制，
大于就会将AppendOnlyMap spill到磁盘。有一点需要注意的是前1000个records进入AppendOnlyMap的时候不会启动是否要spill的检查，
需要扩展时就直接在内存中扩展。
* AppendOnlyMap 大小估计
为了获知AppendOnlyMap占用的内存空间，可以在每次扩展时都将AppendOnlyMap reference的所有objects大小都算一遍，然后加和，
但这样做非常耗时。所以Spark设计了粗略的估算算法，算法时间复杂度是 O(1)，核心思想是利用AppendOnlyMap中每次insert-aggregate record后
result的大小变化及一共insert的records的个数来估算大小，具体见SizeTrackingAppendOnlyMap和SizeEstimator。
* Spill 过程
与shuffle write一样，在spill records到磁盘上的时候，会建立一个buffer缓冲区，大小仍为`spark.shuffle.file.buffer`，默认是32KB。
另外，由于serializer也会分配缓冲区用于序列化和反序列化，所以如果一次serialize的records过多的话缓冲区会变得很大。
spark限制每次serialize的records个数为`spark.shuffle.spill.batchSize`，默认是10000。