package zhengwei.flink

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

object  WordCountStream {
  def main(args: Array[String]): Unit = {
    //执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val parameterTool = ParameterTool.fromArgs(args)
    //接受一个socket文本
    val dataStream = env.socketTextStream(parameterTool.get("host"), parameterTool.getInt("port"))
    //每条数据进程处理
    dataStream.flatMap(_.split("[ ]"))
      .filter(_.nonEmpty).disableChaining()
      .map((_, 1)).startNewChain()
      .keyBy(0)
      .sum(1).slotSharingGroup("a")
      .print()
      .setParallelism(2)//默认并行度是当前电脑的核心数量
    //启动
    env.execute("flink stream word count")
  }
}
