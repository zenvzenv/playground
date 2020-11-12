package zhengwei.flink.api.source

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

/**
 * kafka 数据源
 */
object KafkaSource {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val parameterTool = ParameterTool.fromArgs(args)
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", parameterTool.get("bootstrapServers"))
    properties.setProperty("group.id", parameterTool.get("groupId"))
    //自动维护偏移量
    env.addSource(new FlinkKafkaConsumer011[String]("sensor", new SimpleStringSchema(), properties)).print()
    env.execute("kafka source")
  }
}
