# SparkSqlJoin
数据分析中将两个数据集进行 Join 操作是很常见的场景。在 Spark 的物理计划（physical plan）阶段，Spark 的 JoinSelection 类会根据 
Join hints 策略、Join 表的大小、 Join 是等值 Join（equal-join） 还是不等值（non-equal-joins）以及参与 Join 的 key 是否可以排序
等条件来选择最终的 Join 策略（join strategies），最后 Spark 会利用选择好的 Join 策略执行最终的计算。
当前 Spark（Apache Spark 3.0）一共支持五种 Join 策略：
1. Broadcast hash join(BHJ)
2. Shuffle hash join(SHJ)
3. Shuffle sorted merge join(SMJ)
4. Shuffle and replicate nested loop join(笛卡尔积，Cartesian product join)
5. Broadcast nested loop join(BNLJ)
其中BHJ和SMJ这两种join策略是我们运行spark最常用的。JoinSelection会先根据join的key为等值join来选择Broadcast hash join、Shuffle 
hash join以及Shuffle sorted merge join中的一个；如果join的key为不等值join或没有指定join条件，则会选择Broadcast nested loop join
或Shuffle and replicate nested loop join。

不同的join策略在执行上效率相差较大，作为一个大数据分析师，了解每种 Join 策略的执行过程和适用条件是很有必要的。

## Broadcast Hash Join(BHJ)
BHJ又叫map-side-only join，从名字可以看出，join操作是在map端进行的。这种join一般要求一张表很小，小到足以讲表的数据全部放到Driver端
和Executor端的内存中，而另外一张表很大。

Broadcast Hash Join的实现是将小表的数据广播(broadcast)到Spark的所有Executor上，这个广播过程和我们自己去广播数据没什么区别，先利用
collect算子将小表的数据从Executor端拉取到Driver端，然后在Driver端利用SparkContext.broadcast广播到所有Executor上，然后在Executor
上对广播的数据和大表进行join操作，这种join操作避免了shuffle操作。一般而言，broadcast hash join会比其他join策略执行的要快，但这个
也不是绝对的，
### 适用条件
使用BHJ需要满足以下条件：
* 小表的数据必须要足够的小，可以通过 `spark.sql.autoBroadcastJoinThreshold` 参数来控制，默认是10MB，如果你的内存比较打，可以将这个
阈值适当的调大，如果将 `spark.sql.autoBroadcastJoinThreshold` 设置为-1时可以关闭BHJ
* 只能使用等值join，不要参与join的key可排序
* 除了full outer join，支持所有的join类型

## Shuffle Hash Join(SHJ)
前面介绍的Broadcast Hash Join要求参与join的一张表大小小于 `spark.sql.autoBroadcastJoinThreshold` 参数大小，但是当我们的表的大小
大于这个值，并且我们的表又不适合使用广播时，这个时候可以考虑使用Shuffle Hash Join。

Shuffle Hash Join同样是在大表和小表进行join的时候选择的一种策略，它的计算思想是：把大表和小标按照同样的分区算法和分区数进行分区(根据
参与join的key进行分区)，这样就保证hash相同的数据被分发到同一个分区中，然后在同一个Executor中两张表hash值相同的分区就可以在本地进行
hash join。在join前，还会对小标hash完的分区构建hash map。Shuffle Hash Join充分利用了分治思想，将大问题分解成小问题去解决。

### 使用条件
要启用 Shuffle Hash Join 必须满足以下几个条件：
* 仅支持等值join，不要求参与的keys可排序
* `spark.sql.join.preferSortMergeJoin` 参数必须设置成false，该参数从Spark2.0.0引入，默认为true，也就是默认情况下选择
Sorted Merge Join
* 小表的大小（plan.stats.sizeInBytes）必须小于 `spark.sql.autoBroadcastJoinThreshold * spark.sql.shuffle.partitions`；
而且小表大小（stats.sizeInBytes）的三倍必须小于等于大表的大小（stats.sizeInBytes），
也就是 `a.stats.sizeInBytes * 3 < = b.stats.sizeInBytes`