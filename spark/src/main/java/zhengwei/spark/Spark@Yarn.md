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
#### 注意点：
1. 在提交Spark任务的时候指定的 `--executor-memory` 和 `spark.executor.memory` 控制executor堆的大小。JVM本身会占用一部分内存， `spark.yarn.xxx.memoryOverhead` 指定了在向yarn申请内存时在指定的内存容量的基础上再加上额外的内存再去申请内存
2. 如果Executor设置的内存过大，会导致GC缓慢，官方推荐64G内存为一个Executor的内存上限
3. hdfs client对于多线程高并发存在性能问题，建议一个Executor设置不多于5个core
### Yarn中的关键参数
因为Spark@Yarn之上，Spark的task运行在Yarn的NodeManager之上，对于Spark申请资源，Yarn中的配置也起到关键作用，由如下关键配置：
* `yarn.app.mapreduce.am.resource.mb`:ApplicationMaster能够申请到的最大内存，默认1536M
* `yarn.nodemanager.resource.memory-mb`:NodeManager能够申请的最大内存，默认8192M
* `yarn.scheduler.minimum-allocate-mb`:调度一个Container时，能够申请的最小内存，默认为1024M
* `yarn.scheduler.maximum-allocate-mb`:调度一个Container时，能够申请的最大内存，默认8192M
### 
