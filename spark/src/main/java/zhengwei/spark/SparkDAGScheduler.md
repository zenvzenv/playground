# Spark DAGScheduler
Spark中`DAGScheduler`的主要作用是将Job按照RDD的依赖关系划分成若干个TaskSet，也称为Stage；之后结合当前缓存情况及数据就近的原则，
将Stage提交给TaskScheduler
## 提交流程
```text
org.apache.spark.SparkContext.runJob
                ↓
org.apache.spark.scheduler.DAGScheduler.runJob
                ↓
org.apache.spark.scheduler.DAGScheduler.submitJob
                ↓
org.apache.spark.scheduler.JobSubmitted
                ↓
org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive
                ↓
org.apache.spark.scheduler.DAGSchedulerEventProcessLoop#doOnReceive
                ↓
org.apache.spark.scheduler.DAGScheduler.handleJobSubmitted
```
1. 是由RDD中的action算子(foreach,collect等)去调用SparkContext中的runJob来触发整个job执行
```scala
  def runJob[T, U: ClassTag](
      rdd: RDD[T],
      func: (TaskContext, Iterator[T]) => U,
      partitions: Seq[Int],
      resultHandler: (Int, U) => Unit): Unit = {
    if (stopped.get()) {
      throw new IllegalStateException("SparkContext has been shutdown")
    }
    //获取调用位置
    val callSite = getCallSite
    val cleanedFunc = clean(func)
    logInfo("Starting job: " + callSite.shortForm)
    if (conf.getBoolean("spark.logLineage", false)) {
      logInfo("RDD's recursive dependencies:\n" + rdd.toDebugString)
    }
    //调用DAGScheduler的runJob去提交job
    dagScheduler.runJob(rdd, cleanedFunc, partitions, callSite, resultHandler, localProperties.get)
    progressBar.foreach(_.finishAll())
    rdd.doCheckpoint()
  }
```
2. SparkContext中的runJob又会去触发DAGScheduler中的runJob
```scala
  def runJob[T, U](
      rdd: RDD[T],
      func: (TaskContext, Iterator[T]) => U,
      partitions: Seq[Int],
      callSite: CallSite,
      resultHandler: (Int, U) => Unit,
      properties: Properties): Unit = {
    val start = System.nanoTime
    //调用DAGScheduler的submitJob提交
    //实际时封装成一个JobSubmitter，这是一个DAGSchedulerEvent的时间，提交到EventLoop上去等待线程去执行
    val waiter = submitJob(rdd, func, partitions, callSite, resultHandler, properties)
    ThreadUtils.awaitReady(waiter.completionFuture, Duration.Inf)
    waiter.completionFuture.value.get match {
      case scala.util.Success(_) =>
        logInfo("Job %d finished: %s, took %f s".format
          (waiter.jobId, callSite.shortForm, (System.nanoTime - start) / 1e9))
      case scala.util.Failure(exception) =>
        logInfo("Job %d failed: %s, took %f s".format
          (waiter.jobId, callSite.shortForm, (System.nanoTime - start) / 1e9))
        // SPARK-8644: Include user stack trace in exceptions coming from DAGScheduler.
        val callerStackTrace = Thread.currentThread().getStackTrace.tail
        exception.setStackTrace(exception.getStackTrace ++ callerStackTrace)
        throw exception
    }
  }
```
3. 把任务包装成JobSubmitter，提交到DAGSchedulerEventProcessLoop中，DAGSchedulerEventProcessLoop是一个线程池，内部维护了一个
阻塞队列，队列中存放的是事件，等待线程去取出执行。而被包装成JobSubmitter就是一个DAGSchedulerEvent，被post到
DAGSchedulerEventProcessLoop中，DAGSchedulerEventProcessLoop中会去循环监听事件，并对监听到的数据进行相对应的操作，
对于JobSubmitter这个事件对应的操作是执行DAGScheduler中的handleJobSubmitted。
```scala
  //往DAGSchedulerEventProcessLoop中提交DAGSchedulerEvent事件
  def post(event: E): Unit = {
    eventQueue.put(event)
  }
  /**
   * The main event loop of the DAG scheduler.
   * 轮询的去获取对应的事件，并作出事件相对应的操作
   */
  override def onReceive(event: DAGSchedulerEvent): Unit = {
    val timerContext = timer.time()
    try {
      doOnReceive(event)
    } finally {
      timerContext.stop()
    }
  }
  //针对于JobSubmitter的操作是去执行DAGScheduler中的handleJobSubmitted
  private def doOnReceive(event: DAGSchedulerEvent): Unit = event match {
    case JobSubmitted(jobId, rdd, func, partitions, callSite, listener, properties) =>
      dagScheduler.handleJobSubmitted(jobId, rdd, func, partitions, callSite, listener, properties)
  }
```
4. 最终调用DAGScheduler中的handleJobSubmitter方法提交Stage
```scala
  private[scheduler] def handleJobSubmitted(jobId: Int,
      finalRDD: RDD[_],
      func: (TaskContext, Iterator[_]) => _,
      partitions: Array[Int],
      callSite: CallSite,
      listener: JobListener,
      properties: Properties) {
    var finalStage: ResultStage = null
    try {
      // New stage creation may throw an exception if, for example, jobs are run on a
      // HadoopRDD whose underlying HDFS files have been deleted.
      //调用createResultStage创建ResultStage对象，task的数量和partition的数量保持一致
      finalStage = createResultStage(finalRDD, func, partitions, jobId, callSite)
    } catch {
      case e: BarrierJobSlotsNumberCheckFailed =>
        logWarning(s"The job $jobId requires to run a barrier stage that requires more slots " +
          "than the total number of slots in the cluster currently.")
        // If jobId doesn't exist in the map, Scala coverts its value null to 0: Int automatically.
        val numCheckFailures = barrierJobIdToNumTasksCheckFailures.compute(jobId,
          new BiFunction[Int, Int, Int] {
            override def apply(key: Int, value: Int): Int = value + 1
          })
        if (numCheckFailures <= maxFailureNumTasksCheck) {
          messageScheduler.schedule(
            new Runnable {
              override def run(): Unit = eventProcessLoop.post(JobSubmitted(jobId, finalRDD, func,
                partitions, callSite, listener, properties))
            },
            timeIntervalNumTasksCheck,
            TimeUnit.SECONDS
          )
          return
        } else {
          // Job failed, clear internal data.
          barrierJobIdToNumTasksCheckFailures.remove(jobId)
          listener.jobFailed(e)
          return
        }

      case e: Exception =>
        logWarning("Creating new stage failed due to exception - job: " + jobId, e)
        listener.jobFailed(e)
        return
    }
    // Job submitted, clear internal data.
    barrierJobIdToNumTasksCheckFailures.remove(jobId)

    val job = new ActiveJob(jobId, finalStage, callSite, listener, properties)
    clearCacheLocs()
    logInfo("Got job %s (%s) with %d output partitions".format(
      job.jobId, callSite.shortForm, partitions.length))
    logInfo("Final stage: " + finalStage + " (" + finalStage.name + ")")
    logInfo("Parents of final stage: " + finalStage.parents)
    logInfo("Missing parents: " + getMissingParentStages(finalStage))

    val jobSubmissionTime = clock.getTimeMillis()
    jobIdToActiveJob(jobId) = job
    activeJobs += job
    finalStage.setActiveJob(job)
    val stageIds = jobIdToStageIds(jobId).toArray
    val stageInfos = stageIds.flatMap(id => stageIdToStage.get(id).map(_.latestInfo))
    listenerBus.post(
      SparkListenerJobStart(job.jobId, jobSubmissionTime, stageInfos, properties))
    submitStage(finalStage)
  }
```
