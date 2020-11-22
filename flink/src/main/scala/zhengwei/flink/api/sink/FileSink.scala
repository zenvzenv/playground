package zhengwei.flink.api.sink

import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala._

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/22 14:04
 */
object FileSink {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source = env.readTextFile("test.txt")
    source
      .map(line => {
        val fields = line.split("[,]")
        (fields(0), fields(1).toInt)
      })
      .keyBy(tuple2 => tuple2._1)
      .sum(1)
      .map(item => item._1 + "_" + item._2)
      .addSink(StreamingFileSink.forRowFormat(new Path("result"), new SimpleStringEncoder[String]()).build())

    env.execute()
  }
}
