# SparkSubmit
提交命令
```bash
./bin/spark-submit \
--class org.apache.spark.examples.JavaSparkPi \
--master yarn \
--deploy-mode cluster \
examples/jars/spark-examples.jar 10
```
spark-submit过程
```text
// submit入口，启动JVM进程
org.apache.spark.deploy.SparkSubmit#main
    //提交任务，args就是我们在命令行传给spark-submit的参数，--class、--master etc.
    -- submit.doSubmit(args)
        -- org.apache.spark.deploy.SparkSubmit.doSubmit
            //格式化我们传进去的参数
            -- org.apache.spark.deploy.SparkSubmit.parseArguments
                //封装命令行参数
                -- new SparkSubmitArguments(args)
            // 通过模式匹配匹配上submit方法，此处有个模式匹配，匹配action是哪种提交模式，action在SparkSubmitArguments的主构造
            // 方法中loadEnvironmentArguments()初始化为submit，接下来执行submit相关流程
            -- org.apache.spark.deploy.SparkSubmit#submit
                -- org.apache.spark.deploy.SparkSubmit#runMain#doRunMain
                    -- org.apache.spark.deploy.SparkSubmit#runMain
                        //准备提交环境
                        -- prepareSubmitEnvironment(args) => val (childArgs, childClasspath, sparkConf, childMainClass)
                            -- yarn-client
                                childMainClass = args.mainClass (SparkPi)
                            -- yarn-cluster
                                childMainClass = "org.apache.spark.deploy.yarn.YarnClusterApplication"
                        //反射加载类
                        -- mainClass = Utils.classForName(childMainClass)
                        //构造出app实例对象，JavaMainApplication里面有start方法，里面也是会使用反射找到main方法，并执行
                        //JavaMainApplication 继承自 SparkApplication
                        -- new JavaMainApplication(mainClass)
                        //启动main方法，YarnClusterApplication继承自SparkApplication并覆写了start，start方法里面实现具体的
                        //启动逻辑
                        -- org.apache.spark.deploy.SparkApplication.start
                        // 最终调用Client里面的run方法启动一个yarn-client
                        -- org.apache.spark.deploy.yarn.Client.run
```
yarn-client流程
```text
//启动一个yarn-client
-- org.apache.spark.deploy.yarn.Client.run
    -- new ClientArguments(args)
        -- yarnClient = YarnClient.createYarnClient 
            -- YarnClient client = new YarnClientImpl();
    -- new Client(new ClientArguments(args), conf).run()
        // 初始化yarn客户端并启动yarn客户端
        -- submitApplication()
            // 封装指令，/bin/java xxx
            -- val containerContext = createContainerLaunchContext(newAppResponse)
                -- cluster模式下发送的java命令 => /bin/java org.apache.spark.deploy.yarn.ApplicationMaster
                    -- org.apache.spark.deploy.yarn.ApplicationMaster$.main
                -- client模式下发送的java命令 => /bin/java org.apache.spark.deploy.yarn.ExecutorLauncher
                    -- org.apache.spark.deploy.yarn.ApplicationMaster$.main
            -- val appContext = createApplicationSubmissionContext(newApp, containerContext)
            // 通过yarn client提交任务
            -- yarnClient.submitApplication(appContext)
```
ApplicationMaster
```text
-- main
    // 拼装参数
    -- new ApplicationMasterArguments(args)
    // 实例化AM
    -- master = new ApplicationMaster(amArgs)
    // 启动AM
    -- master.run()
        // 集群模式的话运行 AM 的 runDriver
        -- cluster => org.apache.spark.deploy.yarn.ApplicationMaster#runDriver
            // 启动用户的 --class 指定的类
            -- userClassThread = startUserApplication()
                // 反射 --class 指定的类，调用其中的 main 方法
                -- val mainMethod = userClassLoader.loadClass(args.userClass).getMethod("main", classOf[Array[String]])
                // 启动 Driver 线程，启动 --class 的 main 方法
                -- Thread.start()
            // 向 RM 注册 AM
            -- registerAM(host, port, userConf, sc.ui.map(_.webUrl))
                -- client.createAllocator
                    // 向 RM 申请资源
                    -- allocator.allocateResources()
                        -- handleAllocatedContainers(allocatedContainers.asScala)
                            -- runAllocatedContainers(containersToUse)
                                -- launcherPool.execute(new Runnable(new ExecutorRunnable(...).run)) 
                                    -- startContainer()
                                        // 准备启动 Executor 的命令，bin/java -server org.apache.spark.executor.CoarseGrainedExecutorBackend
                                        // 启动 CoarseGrainedExecutorBackend 进程
                                        -- val commands = prepareCommand()
            // 等待 --class 的 main 方法执行结束
            -- userClassThread.join()
        // 客户端模式的话运行 runExecutorLauncher
        -- client => org.apache.spark.deploy.yarn.ApplicationMaster#runExecutorLauncher
```
CoarseGrainedExecutorBackend
```text
-- org.apache.spark.executor.CoarseGrainedExecutorBackend#main
    // 启动 ExecutorBackend 并向 RPCEnv 中注册此 ExecutorBackend
    -- org.apache.spark.executor.CoarseGrainedExecutorBackend$#run
        -- env.rpcEnv.setupEndpoint("Executor", new CoarseGrainedExecutorBackend(
                   env.rpcEnv, driverUrl, executorId, hostname, cores, userClassPath, env))
        // RpcEnv 的生命周期是 constructor -> onStart -> receive* -> onStop，紧接着调用 onStart 启动 RpcEndpoint
        -- org.apache.spark.executor.CoarseGrainedExecutorBackend.onStart
            // 向 Driver 反向注册 Executor
            -- ref.ask[Boolean](RegisterExecutor(executorId, self, hostname, cores, extractLogUrls))
        // 注册完毕之后， Driver 发送消息给 ExecutorBackend 表示注册成功，executor 才正式实例化
        -- org.apache.spark.executor.CoarseGrainedExecutorBackend.receive
            -- case RegisteredExecutor => executor = new Executor(executorId, hostname, env, userClassPath, isLocal = false)
            -- case LaunchTask(data) => 接受 task 并处理
```