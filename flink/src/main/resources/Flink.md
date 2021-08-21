# Flink
## 运行时架构
### JobManager
在一个 Flink Cluster 中有一个 Flink Master 和若干个 Task Manager 组成，一个 Flink Master 中有一个 Resource Manager 和多个 Job 
Manager。  
JobManager 中的 Scheduler 组件负责调度执行该 job 的 DAG 中所有 Task，发出资源请求，即整个资源调度的起点。  
JobManager 中的 Slot Pool 持有分配到该 Job 的所有资源
### TaskManager
是一个 JVM 进程，负责 Task 的执行，其中 slot 是 TaskManager 资源的子集
#### TaskSlot
是 Flink 资源管理的的基本单位，来控制能够接收多少个任务，slot 的概念贯穿资源调度过程的始终，一个 TaskManager 中有多少个 TaskSlot
就意味着能够支持多少的并发
1. TaskSlot 资源独占，这样一个 TaskManager 中可以运行多种不同的任务，任务之间不受影响
2. TaskManager 中能够同时运行的子任务的数量可以通过 TaskSlot 来控制
#### TaskSharing
TaskSlot 跑完一些任务可以重复利用，可以继续跑后续的 SubTask(在同一个线程中)
### ResourceManager
负责整个 Flink Cluster 的资源调度，以及与外部系统的对接，这里的外部系统指的是 k8s,yarn,Mesos 等资源管理系统
### Dispatcher 

## Flink On Yarn

### Session 模式
```text
+------------+
|            |
| client     +-------------------+
|            |                   |
+------------+                   |
                   +-------------v---------+       +------------------+
                   |                       |       |                  |
+------------+     |         Flink         |       |                  |
|            |     |                       +------>+    Yarn-Cluster  |
| client     +----->     Yarn-Session      |       |                  |
|            |     |                       |       |                  |
+------------+     +------------^----------+       +------------------+
                                |
+------------+                  |
|            |                  |
| client     +------------------+
|            |
+------------+
```
1. 特点：需要预先申请资源来启动 JobManager 个 TaskManager
2. 优点：不需要每次提交任务时再去申请资源，而是使用已经申请好的资源，从而提高执行效率
3. 缺点：任务执行完毕之后，资源不会释放，因此一直占用资源
4. 使用场景：适合作业提交频繁的场景，小作业比较多的场景
PS：即在 yarn 集群中启动一个 Flink 集群，并重复提使用该集群

### Per-Job 模式
```text
+--------+       +------------------+       +-------------------+
| client +------>+   flink_session  +------>+                   |
+--------+       +------------------+       |                   |
                                            |                   |
+--------+       +------------------+       |                   |
| client +------>+   flink_session  +------>+    yarn-cluster   |
+--------+       +------------------+       |                   |
                                            |                   |
+--------+       +------------------+       |                   |
| client +------>+   flink_session  +------>+                   |
+--------+       -------------------+       +-------------------+
```
1. 特点：每次提交都要申请一次资源
2. 作业完成后资源会被释放
3. 每次提交作业都会去申请资源，会降低效率
4. 使用场景：适合作业少、大作业场景

## Flink 名词解释
1. DataFlow：Flink 程序在执行时会被映射成的一个数据模型
2. Operator：数据流模型中的每一个操作被称为Operator，分为 Source/transfer/Sink
3. Partition：数据流模型是分布式并行的，执行时会分成 1~n 个分区
4. SubTask：多个分区可以并行，每一个都是独立运行在一个线程中的，也就是一个 SubTask 任务，可以类比 Spark 中的 Task
5. Parallelism：并行度，就是可以同时真正执行的子任务的数量/分区数
6. OperatorChain：对于多个 OneToOne Operator 可以合并在一起，形成 OperatorChain，一个 OperatorChain 放到一个 SubTask(Thread) 
中执行，类比于 Spark 的 Stage
7. Task：一个 OperatorChain 是一个 Task，即是一组 SubTask 或 单个无法分割的 SubTask 组成，可以类比于 Spark 中的 TaskSet

## Flink Operator 传递模式
### OneToOne 模式
两个 Operator 用此模式传递数据的时候，会保持数据的分区数和顺序，类似于 Spark 的窄依赖
### Redistributing 模式
这种模式会改变数据的分区数，类似于 Spark 的宽依赖

## Flink 执行图
StreamGraph -> JobGraph -> ExecutionGraph -> PhysicalGraph
### StreamGraph
最初的程序执行逻辑流程，也就是算子(Operator)的前后顺序，在 Client 上生成
### JobGraph
将 OneToOne 的Operator 合并成 **OperatorChai**，在 Client 上生成
### ExecutionGraph
将 JobGraph 根据代码中设置的并行度和请求的资源进行**并行化**规划，在 JobManager 上生成
### PhysicalGraph
将 ExecutionGraph 的并行计划落到具体的 TaskManager 上，将具体的 Task 落到具体的 SubTask(Thread) 内执行