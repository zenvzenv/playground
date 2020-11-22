package zhengwei.flink.api.function

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration

/**
 *
 * 在 flink 中所有的 function 都有 rich function 版本。
 * rich function 拥有比 function 更多的方法，可以获取到运行时上下文和有生命周期
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/22 13:54
 */
class MyRichFunction extends RichMapFunction[String,String]{
  //一般做一些初始化操作，比如数据可靠的连接
  //构造方法执行之后，map 方法执行之前
  override def open(parameters: Configuration): Unit = {
    getRuntimeContext
  }

  override def map(value: String): String = ???

  //一般做收尾工作，关闭连接，清空状态
  override def close(): Unit = super.close()
}
