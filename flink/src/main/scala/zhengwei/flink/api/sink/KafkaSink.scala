package zhengwei.flink.api.sink

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/22 14:20
 */
object KafkaSink {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val props = new Properties
    val source = env.addSource(new FlinkKafkaConsumer011[String]("testTopic", new SimpleStringSchema(), props))
    source
      .map(line => {
        val fields = line.split("[,]")
        (fields(0), fields(1).toInt)
      })
      .keyBy(tuple2 => tuple2._1)
      .sum(1)
      .map(item => item._1 + "_" + item._2)
      .addSink(new FlinkKafkaProducer011[String]("localhost:9092", "testTopic", new SimpleStringSchema()))

    env.execute()
  }
}
