# Hadoop专栏
## 一、yarn
### 1.Resource Manager(简称为RM)
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