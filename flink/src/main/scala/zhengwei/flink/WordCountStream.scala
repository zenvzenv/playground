package zhengwei.flink

import org.apache.flink.streaming.api.scala._

object WordCountStream {
  def main(args: Array[String]): Unit = {
    //执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //接受一个socket文本
    val dataStream = env.socketTextStream("localhost", 8888)
    //每条数据进程处理
    dataStream.flatMap(_.split("[ ]"))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)
      .print()
      .setParallelism(2)//默认并行度是当前电脑的核心数量
    //启动
    env.execute("flink stream word count")
  }
}
