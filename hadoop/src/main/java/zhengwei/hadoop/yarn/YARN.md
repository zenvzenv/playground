# YARN(Yet Another Resource Negotiator)
## 组件
### Resource Manager(简称为RM)
1. Resource Manager中包含两个组件
    1. Application Manager(简称为AM)，应用程序管理器
        * AM主要负责接受client端发送的job请求，为应用开启一个Container(资源池)，来运行我们的Application Master
        * 监控Application Master，并且在遇到失败时重启Application Master
        * Application Master
            1. 用户每提交一个任务都会产生一个Application Master，这个Application Master就是负责整个任务的管理者
            2. 与Resource Manager中的Resource Scheduler调度资源以开启任务
            3. 与Node Manager通信以启动和停止任务
            4. 监控Application Master下的所有的任务，如果失败会重新启动任务
        * Container资源池
            1. 是yarn中的抽象，封装了某个节点的多维度资源，如内存，CPU，磁盘，网络或IO
            2. 资源池主要是把NodeManager中的资源切分出来，组成一个可以单独可以单独运行任务的容器
    2. Resource Scheduler(简称为RS)，调度器
        * 调度集群中的机器，让我们集群的每个阶段都被充分利用
        * 值得注意的是：调度器只是负责调度，它不负责任何的计算
        * 调度器分为三种：
            1. 容器调度器
            2. 公平调度器
            3. 队列调度器
### Node Manager(简称NM)
1. NodeManager是一个JVM进程运行在集群中的节点上，每个节点都会有自己的NodeManager
2. NM是一个salve进程，它负责接收ResourceManager的资源分配请求，分配具体的Container给应用。
3. 同时，它还负责监控并报告Container使用信息给ResourceManager。
4. 通过和ResourceManager配合，NodeManager负责整个Hadoop集群中的资源分配工作。ResourceManager是一个全局的进程，而NodeManager只是每个节点上的进程，管理这个节点上的资源分配和监控运行节点的健康状态。
5. NodeManager的具体任务列表：
```text
- 接收ResourceManager的请求，分配Container给应用的某个任务
- 和ResourceManager交换信息以确保整个集群平稳运行。ResourceManager就是通过收集每个NodeManager的报告信息来追踪整个集群健康状态的，而NodeManager负责监控自身的健康状态。
- 管理每个Container的生命周期
- 管理每个节点上的日志
- 执行Yarn上面应用的一些额外的服务，比如MapReduce的shuffle过程
```
### Container
1. Container是Yarn框架中的计算单元，是具体执行Task的基本单位。
2. Container与集群节点的关系是：一个节点会允许多个Container，但一个Container不能跨节点
3. 一个Container就是一组计算资源的抽象，它包含
```text
1. CPU core
2. Memory in MB
```
4. 既然Container指的是具体节点上的计算资源，那就意味着Container中必定包含计算资源的位置信息：计算资源位于哪个机架的哪台机器上，所以我们在请求某个Container时，其实是向某台机器发起的请求，请求的是这台机器的CPU和内存。
5. 任何一个Job或Application必须运行一个或多个Container，在Yarn框架中，ResourceManager只负责告诉ApplicationMaster哪些NodeManager可用，ApplicationMaster需要去找具体的NodeManager请求开启Container
