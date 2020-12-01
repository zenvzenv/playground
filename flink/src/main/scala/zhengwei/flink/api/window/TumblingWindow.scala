package zhengwei.flink.api.window

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

/**
 * window 并不是一个对数据的操作，类似于 keyBy 操作一样，对数据进行了分桶操作，分完窗口之后需要跟上一个汇聚操作才能构成一个完整的操作。
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/23 20:14
 */
object TumblingWindow {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val parameterTool = ParameterTool.fromArgs(args)
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", parameterTool.get("bootstrapServers"))
    properties.setProperty("group.id", parameterTool.get("groupId"))
    val topic = parameterTool.get("topic")
    //自动维护偏移量
    val source = env.addSource(new FlinkKafkaConsumer011[String](topic, new SimpleStringSchema(), properties))
    val map: WindowedStream[(String, Int), String, TimeWindow] = source
      .map(line => {
        val strings = line.split("[ ]")
        (strings(0), strings(1).toInt)
      })
      .keyBy(_._1)
      //      .window(TumblingEventTimeWindows.of(Time.seconds(15)))//滚动窗口
      //      .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))//滑动窗口
      .window(EventTimeSessionWindows.withGap(Time.hours(1))) // 会话窗口
    env.execute()
  }
}
