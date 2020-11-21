package zhengwei.flink.api.transformation

import org.apache.flink.streaming.api.scala._

/**
 * 合流操作，将两个流(这两个流中的数据类型可以不一样)合并在一个流中
 * <p>
 *   connected -> map
 * </p>
 * <p>
 *   connected stream -> data stream
 * </p>
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/21 15:51
 */
object ScalaConnectTransformation {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //第一个数据源
    val source1 = env.socketTextStream("localhost", 8888)
    //第二个数据源
    val source2 = env.socketTextStream("localhost", 8889)
    val source2Int = source2.map(s => s.toInt)
    //看似将一个流合并了，但其实内部还是流分别处理属于自己的数据，相当于一国两制
    val connectedStream = source1.connect(source2Int)
    //用 coMap 对数据进行分别处理
    //第一个函数对第一个数据源进行操作
    //第二个函数对第二个数据源进行操作
    //两个函数互不影响
    val coMapStream = connectedStream.map(
      s1 => s1 + "connected",
      s2 => s2 + "connected"
    )
    coMapStream.print("coMap")
    env.execute()
  }
}
