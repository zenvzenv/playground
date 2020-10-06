package zhengwei.spark.structuredstreaming

import org.apache.spark.sql.SparkSession

object WordCount1 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("WordCount")
      .getOrCreate()

    import spark.implicits._

    val lines = spark.readStream
      .format("socket")
      .option("host", "192.168.1.225")
      .option("port", 9999)
      .load()
    val words = lines.as[String].flatMap(_.split(" "))
    val wordCounts = words.groupBy("value").count()
    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start

    query.awaitTermination()
    spark.stop()
  }
}
