package zhengwei.flink

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._

object WordCountBatch {
  def main(args: Array[String]): Unit = {
    //创建执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment
    //读取文件
    val inputPath = args(0)
    val inputDataSet = env.readTextFile(inputPath)
    //切分数据得到word->group->count
    inputDataSet.flatMap(_.split("[ ]"))
      .map((_, 1))
      .groupBy(0)
      .sum(1)
      .print()
  }
}
