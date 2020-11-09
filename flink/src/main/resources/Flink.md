# Flink
## 运行时架构
### JobManager
在一个 Flink Cluster 中有一个 Flink Master 和若干个 Task Manager 组成，一个 Flink Master 中有一个 Resource Manager 和多个 Job 
Manager。  
JobManager 中的 Scheduler 组件负责调度执行该 job 的 DAG 中所有 Task，发出资源请求，即整个资源调度的起点。  
JobManager 中的 Slot Pool 持有分配到该 Job 的所有资源
### TaskManager
负责 Task 的执行，其中 slot 是 TaskManager 资源的子集
#### slot
是 Flink 资源管理的的基本单位，slot 的概念贯穿资源调度过程的始终
### ResourceManager
负责整个 Flink Cluster 的资源调度，以及与外部系统的对接，这里的外部系统指的是 k8s,yarn,Mesos 等资源管理系统
### Dispatcher 
