# Spark专栏
* 学习和实验spark一些功能的时候写的一些代码
* RDD
    1. RDD(Resilient Distributed Dataset)：弹性分布式数据集
        1. 是Spark的运算基石，为用户屏蔽了底层对数据的复杂抽象和处理，为用户提供了一组方便的数据转换和求值方法。
        2. RDD的弹性表现：
            1. 存储的弹性：内存与磁盘的自动切换
            2. 容错的弹性：数据丢失可以根据血统重新计算出来
            3. 计算的弹性：计算出错重试机制
            4. 分片的弹性：根据需要对RDD进行重新分区
        3. RDD都做了什么
            1. RDD的创建：我们可以从hdfs、hive、hbase和集合数据类型(makeRDD,parallelize)中获取读取数据并获得RDD，也可以通过一个RDD的转换得到另一个RDD
            2. RDD的转换：Spark提供一系列的转换RDD算子，用来对数据的转换，例如map,flatMap,reduceByKey...，转换算子是懒执行的，需要有行动算子触发转换算子的执行
            3. RDD的缓存：如果一个RDD的血统过长，那么在数据都是进行重算时效率将会很低，那么我们可以把后续需要用到的RDD进行缓存起来，这样Spark会切断血统，提升重算效率
            4. RDD的行动：行动算子用来触发转换算子的执行，一个application中有几个job取决于有几个行动算子
            5. RDD的输出：Spark可以将得到的结果输出到hdfs上，也可以时本地文件系统，还可以收集最终结果到一个集合中以供后续操作