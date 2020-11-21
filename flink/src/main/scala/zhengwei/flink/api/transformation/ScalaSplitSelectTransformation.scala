package zhengwei.flink.api.transformation

import org.apache.flink.streaming.api.scala.{SplitStream, StreamExecutionEnvironment}

import scala.collection.Seq

/**
 * 多流操作，将一个流分成多个流
 * <p>
 *  split -> select
 * </p>
 * <p>
 *  split stream -> data stream
 * </p>
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/21 15:41
 */
object ScalaSplitSelectTransformation {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source = env.socketTextStream("localhost", 8888)
    //分割出分别以a，b打头的字符串流
    //看似是将一个流分割成了两个，实际内部还是一个流，只不过是将数据放到若干个集合中
    val split: SplitStream[String] = source.split(data => {
      val strings = data.split("[,]")
      if (strings(0).equals("a"))
        Seq("a")
      else
        Seq("b")
    })

    //筛选出不同的流
    val a = split.select("a")
    val b = split.select("b")
    a.print("a")
    b.print("b")
    env.execute()
  }
}
