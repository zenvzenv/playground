package zhengwei.spark.sparksql;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import zhengwei.spark.sparkcore.Person;

import java.util.*;

/**
 * 学习Spark SQL
 * Spark　SQL架构在Spark Core之上，里面相关的DataFrame和Dataset都是在RDD的基础之上再次进行包装，是更加完善的RDD
 * DataFrame和Dataset和RDD一样，有分区，有依赖关系(即血统)，有Shuffle，有transformation和action算子，transformation算子依旧是懒加载，需要action算子去触发计算
 * <p>
 * DataFrame
 * 特殊的RDD，即在RDD的基础上套了一层Schema，相当于一张二维表
 * Dataset
 *
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
	static void init() {
		//创建SparkSession，和SparkCore创建上下文的方式不一样
		//SparkCore创建JavaSparkContext，而SparkSQL创建的是SparkSession
		spark = SparkSession
				.builder()
				.master("local")
				.appName("SparkSQL")
				.config("spark.some.config.option", "some-value")
				.config("spark.sql.shuffle.partitions", 1)//配置join或者聚合操作shuffle数据时分区的数量，数据量大时可以调大这个参数，默认200
				.getOrCreate();
		//创建spark core上下文
		jsc = new JavaSparkContext(spark.sparkContext());
		//读取json->Dataset，读取之后字段会按ASCII表进行排序
		//不能读取嵌套的json文件
		df = spark.read().json("src/main/resources/input/SparkSQLTestFile.zw");
		//创建RDD
		rdd1 = jsc.parallelize(Arrays.asList(
				"{\"name\": \"zhengwei\",\"age\": 18}",
				"{\"name\": \"zhangsan\",\"age\": 19}",
				"{\"name\": \"lisi\",\"age\": 18}",
				"{\"name\": \"wangwu\",\"age\": 21}",
				"{\"name\": \"wangerma\",\"age\": 19}"
		));
		rdd2 = jsc.parallelize(Arrays.asList(
				"{\"name\": \"zhengwei\",\"score\": 100}",
				"{\"name\": \"zhangsan\",\"score\": 90}",
				"{\"name\": \"lisi\",\"score\": 80}",
				"{\"name\": \"wangwu\",\"score\": 60}",
				"{\"name\": \"wangerma\",\"score\": 70}"
		));
	}

	@Test
	void testReadJson() {
		//打印读取到的json
		df.show();
		//打印列的约束条件
		df.printSchema();
	}

	/**
	 * SparkSQL的一些基本操作
	 */
	@Test
	void testSparkSQLOperator() {
		//select name,age from xxx where age > 18
		df.select(df.col("name"), df.col("age")).where(df.col("age").gt(18)).show();
		//select name from xxx
		df.select(df.col("name")).show();
		//select everyone ,but increment the age 1
		df.select(df.col("age").plus(1), df.col("name")).show();
		//select people older than 20
		df.filter(df.col("age").gt(20)).show();
		//count people by age
		df.groupBy(df.col("age")).count().show();
	}

	@Test
	void testTempView() {
		//创建一个临时表，创建临时表之后，我们就可以像写sql一样去查询数据
		//创建的people这张临时表既不在内存中也不在磁盘中，相当于一个指针指向json源文件，底层操作解析Spark Job读取源文件
		df.createOrReplaceTempView("people");
		spark.sql("select * from people").show();
	}

	/**
	 * 把Dataset转换成RDD
	 */
	@Test
	void datasetToRDD() {
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
	void rddToDataset() {
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
	 * 2.RDD转DataFrame会把自定义的字段按ASCII排序
	 * 3.自定义实体类的访问修饰必须是public
	 */
	@Test
	void ordinaryRDDToDataset() {
		JavaRDD<String> rdd1 = jsc.textFile("src/main/resources/input/SparkSQLOrdinaryFile.zw");
		JavaRDD<Person> personRdd = rdd1.map(line -> {
			String[] split = line.split("[ ]");
			return new Person(split[0], Integer.parseInt(split[1]));
		});
//        Encoder<Person> personEncoder= Encoders.bean(Person.class);
		/*
		 * 传入Person.class的时候，SparkSession时通过反射的方式创建DataFrame的，
		 * 底层通过反射获取Person所有的field，结合RDD本身，就生成了DataFrame
		 */
		Dataset<Row> df = spark.createDataFrame(personRdd, Person.class);
		df.show();
	}

	/**
	 * 通过动态的Schema创建DataFrame
	 */
	@Test
	void createDataFrameByDynamicSchema() {
		JavaRDD<String> lines = jsc.textFile("src/main/resources/input/SparkSQLOrdinaryFile.zw");
		JavaRDD<Row> rowRDD = lines.map(line -> RowFactory.create(
				line.split("[ ]")[0],
				line.split("[ ]")[1]
		));
		//动态创建DataFrame中的元数据，一般来说这里的字段来源于字符串，也可以源自外部数据库
		//创建Row的字段顺序要和动态Schema的顺序一致，并且表头字段不会按照ASCII排序
		List<StructField> schemaInfo = Arrays.asList(
				DataTypes.createStructField("name", DataTypes.StringType, true),
				DataTypes.createStructField("age", DataTypes.StringType, true)
		);
		StructType schema = DataTypes.createStructType(schemaInfo);
		Dataset<Row> df = spark.createDataFrame(rowRDD, schema);
		df.show();
	}

	/**
	 * 将DataFrame保存成parquet格式的文件
	 */
	@Test
	void saveAsParquet() {
		JavaRDD<String> jsonRDD = jsc.textFile("src/main/resources/input/SparkSQlTestFile.zw");
		Dataset<Row> df = spark.read().json(jsonRDD);
        /*
        Append,追加
        Overwrite,覆盖
        ErrorIfExists,如果存在就报错
        Ignore,如果存在就忽略
         */
		df.write().mode(SaveMode.Overwrite).format("parquet").save("src/main/resources/input/SparkSQLParquet");
	}

	/**
	 * 从parquet读取并加载成DataFrame
	 */
	@Test
	void readFromParquet() {
		//两种方式是一样的
		spark.read().parquet("src/main/resources/input/SparkSQLParquet").show();
		spark.read().format("parquet").load("src/main/resources/input/SparkSQLParquet").show();
	}

	@Test
	void createDFFromJDBC() {

        /*
        第一种方式读取MySQL数据库表，加载DataFrame
         */
		Map<String, String> map = new HashMap<>();
		map.put("url", "jdbc:mysql://127.0.0.1:3306/test");
		map.put("username", "username");
		map.put("password", "password");
		map.put("dbtable", "person");
		Dataset<Row> person = spark.read().format("jdbc").options(map).load();
		person.show();
		person.registerTempTable("person");
        /*
        第二中方式读取mysql数据库表，加载DataFrame
         */
		DataFrameReader reader = spark.read().format("jdbc");
		reader.option("url", "jdbc:mysql://127.0.0.1:3306/test");
		reader.option("username", "username");
		reader.option("password", "password");
		reader.option("dbtable", "person");
		Dataset<Row> score = reader.load();
		score.show();
		score.registerTempTable("score");
		Dataset<Row> result = spark.sql("select person.id,person.name,person.age,score.score from person,score where person.name=score.name");
		result.show();
		//将DataFrame存储到MySQL中
		Properties properties = new Properties();
		properties.put("user", "username");
		properties.put("password", "password");
        /*
        Append,追加
        Overwrite,覆盖
        ErrorIfExists,如果存在就报错
        Ignore,如果存在就忽略
         */
		result.write().mode(SaveMode.Overwrite).jdbc("jdbc:mysql://127.0.0.1:3306/test", "result", properties);
	}
}
