package zhengwei.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * 学习Spark SQL
 * DataFrame
 * Dataset
 * @author zhengwei AKA Sherlock
 * @since 2019/6/2 13:43
 */
public class SparkSQLOperator {
    private static SparkSession spark;
    private static Dataset<Row> df;
    private static JavaSparkContext jsc;
    private static JavaRDD<String> rdd1;
    private static JavaRDD<String> rdd2;
    @BeforeAll
    static void init(){
        //创建SparkSession，和SparkCore创建上下文的方式不一样
        //SparkCore创建JavaSparkContext，而SparkSQL创建的是SparkSession
        spark=SparkSession
                .builder()
                .master("local")
                .appName("SparkSQL")
                .config("spark.some.config.option","some-value")
                .getOrCreate();
        //创建spark core上下文
        jsc = new JavaSparkContext(spark.sparkContext());
        //读取json->Dataset，读取之后字段会按ASCII表进行排序
        //不能读取嵌套的json文件
        df=spark.read().json("src/main/resources/input/SparkSQLTestFile.zw");
        //创建RDD
        rdd1= jsc.parallelize(Arrays.asList(
                "{\"name\": \"zhengwei\",\"age\": 18}",
                "{\"name\": \"zhangsan\",\"age\": 19}",
                "{\"name\": \"lisi\",\"age\": 18}",
                "{\"name\": \"wangwu\",\"age\": 21}",
                "{\"name\": \"wangerma\",\"age\": 19}"
        ));
        rdd2= jsc.parallelize(Arrays.asList(
                "{\"name\": \"zhengwei\",\"score\": 100}",
                "{\"name\": \"zhangsan\",\"score\": 90}",
                "{\"name\": \"lisi\",\"score\": 80}",
                "{\"name\": \"wangwu\",\"score\": 60}",
                "{\"name\": \"wangerma\",\"score\": 70}"
        ));
    }
    @Test
    void testReadJson(){
        //打印读取到的json
        df.show();
        //打印列的约束条件
        df.printSchema();
    }

    /**
     * SparkSQL的一些基本操作
     */
    @Test
    void testSparkSQLOperator(){
        //select name,age from xxx where age > 18
        df.select(df.col("name"), df.col("age")).where(df.col("age").gt(18)).show();
        //select name from xxx
        df.select(df.col("name")).show();
        //select everyone ,but increment the age 1
        df.select(df.col("age").plus(1),df.col("name")).show();
        //select people older than 20
        df.filter(df.col("age").gt(20)).show();
        //count people by age
        df.groupBy(df.col("age")).count().show();
    }
    @Test
    void testTempView(){
        //创建一个临时表，创建临时表之后，我们就可以像写sql一样去查询数据
        //创建的people这张临时表既不在内存中也不在磁盘中，相当于一个指针指向json源文件，底层操作解析Spark Job读取源文件
        df.createOrReplaceTempView("people");
        spark.sql("select * from people").show();
    }

    /**
     * 把Dataset转换成RDD
     */
    @Test
    void datasetToRDD(){
        //调用JavaRDD即可把Dataset转换成JavaRDD
        JavaRDD<Row> javaRDD = df.javaRDD();
//        javaRDD.foreach(System.out::println);
        //特别注意：索引从0开始，这里索引值是经过ASCII排序排序过后的顺序
        javaRDD.foreach(row -> System.out.println(row.get(1)));
        //获取指定列名
        javaRDD.foreach(row -> System.out.println(row.getAs("age").toString()));
    }

    /**
     * 把json格式的RDD转换成Dataset
     */
    @Test
    void rddToDataset(){
        Dataset<Row> nameAgeDf = spark.read().json(rdd1);
        nameAgeDf.show();
        Dataset<Row> nameScoreRdf = spark.read().json(rdd2);
        nameScoreRdf.show();
        nameAgeDf.createOrReplaceTempView("nameAge");
        nameScoreRdf.createOrReplaceTempView("nameScore");
        //联表查询时不支持取别名
        spark.sql("select nameAge.name,nameScore.score from nameAge join nameScore on nameAge.name=nameScore.name").show();
    }

    /**
     * 通过反射的方式生成DataFrame
     * 注意：1.自定义的实体类Person必须要实现序列化->节点之间的传输数据，需要class的序列化
     *      2.RDD转DataFrame会把自定义的字段按ASCII排序
     *      3.自定义实体类的访问修饰必须是public
     */
    @Test
    void ordinaryRDDToDataset(){
        JavaRDD<String> rdd1 = jsc.textFile("src/main/resources/input/SparkGroupByKeyTopN.zw");
        JavaRDD<Person> personRdd = rdd1.map(line -> {
            String[] split = line.split("[ ]");
            return new Person(split[0], Integer.parseInt(split[1]));
        });
//        Encoder<Person> personEncoder= Encoders.bean(Person.class);
        /*
         * 传入Person.class的时候，SparkSession时通过反射的方式创建DataFrame的，
         * 底层通过反射获取Person所有的field，结合RDD本身，就生成了DataFrame
         */
        Dataset<Row> df = spark.createDataFrame(personRdd,Person.class);
        df.show();
    }
    @AfterAll
    static void end(){
        spark.stop();
    }
}
