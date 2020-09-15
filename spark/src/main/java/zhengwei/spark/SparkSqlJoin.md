# SparkSQLJoin
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

### 例子
```text
scala> val iteblogDF = Seq(
     |   (0, "https://www.iteblog.com"),
     |   (1, "iteblog_hadoop"),
     |   (2, "iteblog")
     | ).toDF("id", "info")
iteblogDF: org.apache.spark.sql.DataFrame = [id: int, info: string]
scala> val r = iteblogDF.join(iteblogDF, Seq("id"), "inner")
r: org.apache.spark.sql.DataFrame = [id: int, info: string ... 1 more field]
scala> r.explain
== Physical Plan ==
*(1) Project [id#7, info#8, info#12]
+- *(1) BroadcastHashJoin [id#7], [id#11], Inner, BuildRight
   :- *(1) LocalTableScan [id#7, info#8]
   +- BroadcastExchange HashedRelationBroadcastMode(List(cast(input[0, int, false] as bigint))), [id=#15]
      +- LocalTableScan [id#11, info#12]
scala> r.show(false)
+---+-----------------------+-----------------------+
|id |info                   |info                   |
+---+-----------------------+-----------------------+
|0  |https://www.iteblog.com|https://www.iteblog.com|
|1  |iteblog_hadoop         |iteblog_hadoop         |
|2  |iteblog                |iteblog                |
+---+-----------------------+-----------------------+
```

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

### 例子
```text
// 因为我们下面测试数据都很小，所以我们先把 BroadcastJoin 关闭
scala> spark.conf.set("spark.sql.autoBroadcastJoinThreshold", 1)
// 为了启用 Shuffle Hash Join 必须将 spark.sql.join.preferSortMergeJoin 设置为 false
scala> spark.conf.set("spark.sql.join.preferSortMergeJoin", false)
scala> val iteblogDF1 = Seq(
     |   (2, "iteblog")
     | ).toDF("id", "info")
iteblogDF1: org.apache.spark.sql.DataFrame = [id: int, info: string]
scala> val iteblogDF2 = Seq(
     |   (0, "https://www.iteblog.com"),
     |   (1, "iteblog_hadoop"),
     |   (2, "iteblog")
     | ).toDF("id", "info")
iteblogDF2: org.apache.spark.sql.DataFrame = [id: int, info: string]
scala> val r = iteblogDF1.join(iteblogDF, Seq("id"), "inner")
r: org.apache.spark.sql.DataFrame = [id: int, info: string ... 1 more field]
scala> r.explain
== Physical Plan ==
*(1) Project [id#52, info#53, info#37]
+- ShuffledHashJoin [id#52], [id#36], Inner, BuildLeft
   :- Exchange hashpartitioning(id#52, 200), true, [id=#172]
   :  +- LocalTableScan [id#52, info#53]
   +- Exchange hashpartitioning(id#36, 200), true, [id=#173]
      +- LocalTableScan [id#36, info#37]
scala> r.show(false)
+---+-------+-------+
|id |info   |info   |
+---+-------+-------+
|2  |iteblog|iteblog|
+---+-------+-------+
```


### 注意事项
在使用SHJ的时候，Spark构建了build hash map，所以如果分区后的数据还比较大的话，可能会出现OOM错误，在Spark中，ShuffleHashJoin的实现
在`ShuffleHashJoinExec`中。

## Shuffle Sort Merge Join(SMJ)
前面两种Join策略对表的大小都是有要求的，如果参与的表都很大，那么就要考虑使用Shuffle Sort Merge Join了。

### 实现思想
也是对参与join的key使用相同的分区算法和分区数进行分许，目的就是为了保证相同的keys能够落到同一分区中，分区之后再对每个分区按照Join的
keys进行排序，最后reduce端获取两张表相同分区的数据进行Merge Join。

### 适用条件
* 仅支持等值join，并且要求参与的key可排序
Spark里面的大表基本可以使用SortMergeJoin来实现，对应的类是 `SortMergeJoinExec` ，我们可以对参与join的两张表按照keys进行Bucket
来避免Shuffle Sort Merge Join的Shuffle操作，因为Bucket的表事先以及按照keys进行分区排序，所以锁Shuffle Sort Merge Join的时候无需
再做分区和排序了。

## Cartesian product join(笛卡尔积)
和 MySQL 一样，如果 Spark 中两张参与 Join 的表没指定 where 条件(ON 条件)那么会产生 Cartesian product join，
这个 Join 得到的结果其实就是两张行数的乘积。

### 适用条件
必须是inner join，其支持等值和不等值操作

### 例子
```text
// 因为我们下面测试数据都很小，所以我们先把 BroadcastJoin 关闭
scala> spark.conf.set("spark.sql.autoBroadcastJoinThreshold", 1)
scala> val iteblogDF1 = Seq(
     |   (0, "111"),
     |   (1, "222"),
     |   (2, "333")
     | ).toDF("id", "info")
iteblogDF1: org.apache.spark.sql.DataFrame = [id: int, info: string]
scala> val iteblogDF2 = Seq(
     |   (0, "https://www.iteblog.com"),
     |   (1, "iteblog_hadoop"),
     |   (2, "iteblog")
     | ).toDF("id", "info")
iteblogDF2: org.apache.spark.sql.DataFrame = [id: int, info: string]
// 这里也可以使用 val r = iteblogDF1.crossJoin(iteblogDF2)
scala> val r = iteblogDF1.join(iteblogDF2, Nil, "inner")
r: org.apache.spark.sql.DataFrame = [id: int, info: string ... 2 more fields]
scala> r.explain
== Physical Plan ==
CartesianProduct
:- LocalTableScan [id#157, info#158]
+- LocalTableScan [id#168, info#169]
scala> r.show(false)
+---+----+---+-----------------------+
|id |info|id |info                   |
+---+----+---+-----------------------+
|0  |111 |0  |https://www.iteblog.com|
|0  |111 |1  |iteblog_hadoop         |
|0  |111 |2  |iteblog                |
|1  |222 |0  |https://www.iteblog.com|
|1  |222 |1  |iteblog_hadoop         |
|1  |222 |2  |iteblog                |
|2  |333 |0  |https://www.iteblog.com|
|2  |333 |1  |iteblog_hadoop         |
|2  |333 |2  |iteblog                |
+---+----+---+-----------------------+
```
Cartesian product join 产生数据的行数是两表的乘积，当 Join 的表很大时，其效率是非常低下的，所以我们尽量不要使用这种 Join。
在 Spark 中 Cartesian product join 的实现可以参见 `CartesianProductExec` 类。

## Broadcast Nested Loop Join(BNLJ)
可以吧BNLJ的过程看作如下计算：
```scala
for record_1 in relation_1:
  for record_2 in relation_2:
    # join condition is executed
```
可以看出BNLJ在某些情况下会对某张表进行多次扫描，可见其效率非常地下。从名字可以看出，BNLJ会根据相关条件对小表进行广播，以减少表的扫描
次数。触发广播需要满足以下三个条件：
1. right outer join会广播左表
2. left outer,left semi,left anti或者existence join时会广播右表
3. inner join时两张表都会广播

### 适用条件
Broadcast nested loop join 支持等值和不等值 Join，支持所有的 Join 类型。

## Spark如何选择Join策略
Spark 有五种 Join 策略，那么 Spark 是按照什么顺序来选择呢？前面我们也说了，Spark 的 Join 策略是在 JoinSelection 类里面实现的，首先
Spark的计算引擎优化器并不是万能的，有些场景下会选择错误的join策略，所有Spark 2.4 & Spark 3.0引入了join hint，也就是用户可以自己选择
join策略。用户指定的策略优先级最高，大体流程如下：

1. 如果是等值join，那么就按照下面的顺序选择join策略
    * 用户是不是指定了BROADCAST hint(BROADCAST,BROADCASTJOIN,MAPJOIN中的一个)，如果指定了，那么就适用broadcast hash join
    * 用户是不是指定了SHUFFLE MERGE hint(SHUFFLE_MERGE,MERGE,MERGEJOIN中的一个)，如果指定了，那就使用Shuffle sort merge join，
    * 用户是不是指定了Shuffle Hash Join hint(Shuffle Hash)，如果指定了，那就用Shuffle Hash Join
    * 用户是不是指定了 shuffle-and-replicate nested loop join hint(SHUFFLE_REPLICATE_NL)，如果指定了，
    那就用 Cartesian product join；
    * 如果用户没有指定任何 Join hint，那根据 Join 的适用条件按照 
    Broadcast Hash Join -> Shuffle Hash Join -> Sort Merge Join -> Cartesian Product Join -> Broadcast Nested Loop Join 
    顺序选择 Join 策略
2. 如果是不等值 Join，那么是按照下面顺序选择 Join 策略：
    * 用户是不是指定了 BROADCAST hint （BROADCAST、BROADCASTJOIN 以及 MAPJOIN 中的一个），如果指定了，那就广播对应的表，
    并选择 Broadcast Nested Loop Join；
    * 用户是不是指定了 shuffle-and-replicate nested loop join hint （SHUFFLE_REPLICATE_NL），如果指定了，
    那就用 Cartesian product join；
    * 如果用户没有指定任何 Join hint，那根据 Join 的适用条件按照 
    Broadcast Nested Loop Join ->Cartesian Product Join -> Broadcast Nested Loop Join 顺序选择 Join 策略