# Netty专题
## Netty程序的大体流程
1. 首先声明两个事件循环组，bossGroup和workerGroup
2. bossGroup主要是接收请求，并不对请求进行处理，bossGroup接到请求后交给bossGroup去处理
3. 创建ServerBootstrap，用于后续的启动服务
4. 往ServerBootstrap中设置事件循环组、channel(管道类型)和处理器，绑定端口和关闭服务的相关设置
5. 在处理器中，我们可以在管道中添加相关处理类