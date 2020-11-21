package zhengwei.flink.api.transformation

import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * 将两个数据类型相同的流合并在一起
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/21 16:22
 */
object ScalaUnionTransformation {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source1 = env.socketTextStream("localhost", 8888)
    println(source1.parallelism)
    val source2 = env.socketTextStream("localhost", 8889)
    println(source2.parallelism)
    val unionSource = source1.union(source2)
    println(unionSource.parallelism)
    val print: DataStreamSink[String] = unionSource.print("union")
    print.setParallelism(1)
    env.execute()
  }
}
