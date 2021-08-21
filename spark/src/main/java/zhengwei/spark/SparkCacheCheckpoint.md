#Spark Cache&Checkpoint
与Hadoop MapReduce job 不同的是Spark 的逻辑/物理执行图可能很庞大，task 中computing chain 可能会很长，计算某些RDD 也可能会很耗时。
这时，如果task 中途运行出错，那么task 的整个computing chain 需要重算，代价太高。因此，有必要将计算代价较大的RDD checkpoint 一下，
这样，当下游RDD 计算出错时，可以直接从checkpoint 过的RDD 那里读取数据继续算。
## Cache
Cache之后的数据会被整个application共享。
###哪些RDD需要cache？
会被重复使用的（但不能太大）。
### 用户怎么设定哪些RDD要cache？
因为用户只与 driver program 打交道，因此只能用 rdd.cache() 去 cache 用户能看到的 RDD。
所谓能看到指的是调用 transformation() 后生成的 RDD，而某些在 transformation() 中 Spark 自己生成的 RDD 是不能被用户直接 cache 的，
比如 reduceByKey() 中会生成的 ShuffledRDD、MapPartitionsRDD 是不能被用户直接 cache 的。
### driver program 设定 rdd.cache() 后，系统怎么对 RDD 进行 cache？
**设想：**  
当 task 计算得到 RDD 的某个 partition 的第一个 record 后，就去判断该 RDD 是否要被 cache，如果要被 cache 的话，
将这个 record 及后续计算的到的records直接丢给本地 blockManager 的 memoryStore，如果 memoryStore 存不下就交给diskStore存放到磁盘。  
**具体实现：**  
将要计算 RDD partition 的时候（而不是已经计算得到第一个 record 的时候）就去判断 partition 要不要被 cache。
如果要被 cache 的话，先将 partition 计算出来，然后 `cache` 到内存。cache 只使用 memory，写磁盘的话那就叫 `checkpoint` 了。
调用 `rdd.cache()` 后， rdd 就变成 `persistRDD` 了，其 StorageLevel 为 `MEMORY_ONLY`。
persistRDD 会告知 driver 说自己是需要被 persist 的。
