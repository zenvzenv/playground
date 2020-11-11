package zhengwei.flink.api.source

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.flink.streaming.api.functions.source.SourceFunction

import scala.util.Random

/**
 * 自定义 SourceFunction
 */
class MySource extends SourceFunction[SensorReading] {
  val running = new AtomicBoolean(true)

  override def run(sourceContext: SourceFunction.SourceContext[SensorReading]): Unit = {
    val random = new Random(System.currentTimeMillis())

    //生成初始温度
    var currentTuple = (1 to 10).map(i => ("sensor" + i, random.nextDouble() * 100))

    //不停的产生数据，除非被 cancel
    while (running.get()) {
      //在上次基础上进行维度的微调
      currentTuple = currentTuple.map(data => (data._1, data._2 + random.nextGaussian()))
      //获取时间戳
      val timestamp = System.currentTimeMillis()
      //collect 获取数据源数据
      currentTuple.foreach(data => sourceContext.collect(SensorReading(data._1, timestamp, data._2)))
      TimeUnit.MILLISECONDS.sleep(100)
    }
  }

  override def cancel(): Unit = {
    running.set(false)
  }
}
