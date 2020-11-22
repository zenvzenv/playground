package zhengwei.flink.api.sink

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommandDescription, RedisMapper}

/**
 * 写入到 redis 的 sink
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/22 14:30
 */
object RedisSink {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source = env.readTextFile("test.txt")
    val map = source
      .map(line => {
        val fields = line.split("[,]")
        (fields(0), fields(1).toInt)
      })
      .keyBy(tuple2 => tuple2._1)
      .sum(1)
      .map(item => item._1 + "_" + item._2)
    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("localhost")
      .setPort(6379)
      .build();
    map.addSink(new RedisSink[String](conf, new MyRedisMapper))

    env.execute()
  }
}

class MyRedisMapper extends RedisMapper[String] {
  override def getCommandDescription: RedisCommandDescription = ???

  override def getKeyFromData(t: String): String = ???

  override def getValueFromData(t: String): String = ???
}