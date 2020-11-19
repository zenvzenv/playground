package zhengwei.spark.sql

import org.apache.spark.sql.SparkSession

/**
 * Spark SQL Explain
 * Parsed Logical Plan -> Analyzed Logical Plan -> Optimized Logical Plan -> Physical Plan
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/19 11:28
 */
object SparkSqlExplain {

  case class Student(id: Long, name: String, chinese: String, math: String, english: String, age: Int)

  case class Score(sid: Long, weight1: String, weight2: String, weight3: String)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("spark sql explain")
      .config("spark.some.config.option", "some-value")
      .appName("spark explain")
      .master("local[*]")
      .getOrCreate()
    //引入spark sql的隐式转换
    import spark.implicits._

    val student = spark
      .sparkContext
      .textFile("student.txt")
      .map(_.split("[,]"))
      .map(student => Student(student(0).toLong, student(1), student(2), student(3), student(4), student(5).toInt))
      .toDF

    student.createOrReplaceTempView("student")

    val score = spark
      .sparkContext
      .textFile("score.txt")
      .map(_.split("[,]"))
      .map(score => Score(score(0).toLong, score(1), score(2), score(3)))
      .toDF()

    score.createOrReplaceTempView("score")

    /*
    == Parsed Logical Plan ==
    'Project [unresolvedalias('sum('chineseScore), None)]
    +- 'Filter ('z.chineseScore < 100)
       +- 'SubqueryAlias `z`
          +- 'Project ['x.id, (('x.chinese + 20) + 30) AS chineseScore#34, 'x.math]
             +- 'Join Inner, ('x.id = 'y.sid)
                :- 'SubqueryAlias `x`
                :  +- 'UnresolvedRelation `student`
                +- 'SubqueryAlias `y`
                   +- 'UnresolvedRelation `score`

    == Analyzed Logical Plan ==
    sum(chineseScore): double
    Aggregate [sum(chineseScore#34) AS sum(chineseScore)#40]
    +- Filter (chineseScore#34 < cast(100 as double))
       +- SubqueryAlias `z`
          +- Project [id#7L, ((cast(chinese#9 as double) + cast(20 as double)) + cast(30 as double)) AS chineseScore#34, math#10]
             +- Join Inner, (id#7L = sid#25L)
                :- SubqueryAlias `x`
                :  +- SubqueryAlias `student`
                :     +- SerializeFromObject [assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true])).id AS id#7L, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true])).name, true, false) AS name#8, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true])).chinese, true, false) AS chinese#9, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true])).math, true, false) AS math#10, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true])).english, true, false) AS english#11, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true])).age AS age#12]
                :        +- ExternalRDD [obj#6]
                +- SubqueryAlias `y`
                   +- SubqueryAlias `score`
                      +- SerializeFromObject [assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true])).sid AS sid#25L, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true])).weight1, true, false) AS weight1#26, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true])).weight2, true, false) AS weight2#27, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true])).weight3, true, false) AS weight3#28]
                         +- ExternalRDD [obj#24]

    == Optimized Logical Plan ==
    Aggregate [sum(chineseScore#34) AS sum(chineseScore)#40]
    +- Project [((cast(chinese#9 as double) + 20.0) + 30.0) AS chineseScore#34]
       +- Join Inner, (id#7L = sid#25L)
          :- Project [id#7L, chinese#9]
          :  +- Filter (isnotnull(chinese#9) && (((cast(chinese#9 as double) + 20.0) + 30.0) < 100.0))
          :     +- SerializeFromObject [assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).id AS id#7L, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).name, true, false) AS name#8, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).chinese, true, false) AS chinese#9, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).math, true, false) AS math#10, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).english, true, false) AS english#11, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).age AS age#12]
          :        +- ExternalRDD [obj#6]
          +- Project [sid#25L]
             +- SerializeFromObject [assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).sid AS sid#25L, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).weight1, true, false) AS weight1#26, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).weight2, true, false) AS weight2#27, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).weight3, true, false) AS weight3#28]
                +- ExternalRDD [obj#24]

    == Physical Plan ==
    *(6) HashAggregate(keys=[], functions=[sum(chineseScore#34)], output=[sum(chineseScore)#40])
    +- Exchange SinglePartition
       +- *(5) HashAggregate(keys=[], functions=[partial_sum(chineseScore#34)], output=[sum#42])
          +- *(5) Project [((cast(chinese#9 as double) + 20.0) + 30.0) AS chineseScore#34]
             +- *(5) SortMergeJoin [id#7L], [sid#25L], Inner
                :- *(2) Sort [id#7L ASC NULLS FIRST], false, 0
                :  +- Exchange hashpartitioning(id#7L, 200)
                :     +- *(1) Project [id#7L, chinese#9]
                :        +- *(1) Filter (isnotnull(chinese#9) && (((cast(chinese#9 as double) + 20.0) + 30.0) < 100.0))
                :           +- *(1) SerializeFromObject [assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).id AS id#7L, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).name, true, false) AS name#8, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).chinese, true, false) AS chinese#9, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).math, true, false) AS math#10, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).english, true, false) AS english#11, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Student, true]).age AS age#12]
                :              +- Scan[obj#6]
                +- *(4) Sort [sid#25L ASC NULLS FIRST], false, 0
                   +- Exchange hashpartitioning(sid#25L, 200)
                      +- *(3) Project [sid#25L]
                         +- *(3) SerializeFromObject [assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).sid AS sid#25L, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).weight1, true, false) AS weight1#26, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).weight2, true, false) AS weight2#27, staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, assertnotnull(input[0, zhengwei.spark.sql.SparkSqlExplain$Score, true]).weight3, true, false) AS weight3#28]
                            +- Scan[obj#24]
     */
    spark.sql("select sum(chineseScore) from (select x.id,x.chinese+20+30 as chineseScore,x.math from student x inner join score y on x.id=y.sid) z where z.chineseScore < 100")
      .explain(true)
  }
}
