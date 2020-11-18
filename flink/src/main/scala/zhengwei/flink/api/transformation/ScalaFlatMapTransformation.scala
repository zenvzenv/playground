package zhengwei.flink.api.transformation

import org.apache.flink.streaming.api.scala._

object ScalaFlatMapTransformation {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source = env.fromElements("1 2 3", "4 5 6", "7 8 9")
    val flatMap = source.flatMap(str => str.split(" "))
    flatMap.print()
    env.execute()
  }
}
