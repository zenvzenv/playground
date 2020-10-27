package zhengwei.flink

import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * Flink乱序处理
 */
object OutOfOrderCase {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    env.addSource(new SourceFunction[(String, Long)] {
      override def run(ctx: SourceFunction.SourceContext[(String, Long)]): Unit = {
        ctx.collect("key", 0L)
        ctx.collect("key", 1000L)
        ctx.collect("key", 2000L)
        ctx.collect("key", 3000L)
        ctx.collect("key", 3000L)
        ctx.collect("key", 4000L)
        ctx.collect("key", 5000L)
        // out of order
        ctx.collect("key", 4000L)
        ctx.collect("key", 6000L)
        ctx.collect("key", 6000L)
        ctx.collect("key", 7000L)
        ctx.collect("key", 8000L)
        ctx.collect("key", 10000L)
        // out of order
        ctx.collect("key", 8000L)
        ctx.collect("key", 9000L)
      }

      override def cancel(): Unit = {}
    })
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[(String, Long)] {
//        private val outOfOrder = 3000
        private val outOfOrder = 0

        override def checkAndGetNextWatermark(lastElement: (String, Long), extractedTimestamp: Long): Watermark = {
          val ts = lastElement._2 - outOfOrder
          new Watermark(ts)
        }

        override def extractTimestamp(element: (String, Long), previousElementTimestamp: Long): Long = {
          element._2
        }
      })
      .keyBy(0)
      .window(TumblingEventTimeWindows.of(Time.of(5, TimeUnit.SECONDS)))
      .sum(1)
      .print()
    env.execute("OutOfOrder")
  }
}
