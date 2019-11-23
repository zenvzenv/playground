package zhengwei.spark.sparksql;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Spark SQL的多数据源
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/23 8:58
 */
public class SparkSQLMultipleDataSource {
	private static JavaSparkContext jsc;
	private static SparkSession spark;

	@BeforeAll
	static void init() {
		spark = SparkSession.builder()
				.appName("SparkSQLMultipleDataSource")
				.master("local[2]")
				.getOrCreate();
		jsc = new JavaSparkContext(spark.sparkContext());
	}

	/**
	 * Spark SQL从以数据库作为数据源读取数据
	 * Spark SQL也可以写出到多种数据源
	 */
	@Test
	void jdbcDataSource() {
		Map<String, String> options = new HashMap<>();
		options.put("url", "jdbc:oracle:thin:@//10.21.17.121:1521/ORCL");
		options.put("driver", "oracle.jdbc.OracleDriver");
		options.put("dbtable", "PERSON_SPARK_TEST");
		options.put("user", "sal");
		options.put("password", "sal123");
		/*
		懒加载，不会去读表中的数据，但是回去读取表的元数据信息以获取表头信息
		 */
		Dataset<Row> jdbcDS = spark.read().format("jdbc").options(options).load();
		//打印Dataset中的schema信息，此操作不是action
		jdbcDS.printSchema();
		Dataset<Row> teens = jdbcDS.filter(jdbcDS.col("AGE").gt(18));
//		teens.show();
		//将结果输出到数据库中
		Dataset<Row> result = teens.select(teens.col("NAME"), teens.col("AGE")).orderBy(teens.col("AGE"));
		/*Properties properties=new Properties();
		properties.put("user","sal");
		properties.put("password","sal123");
		result.write().mode("ignore").jdbc("jdbc:oracle:thin:@//10.21.17.121:1521/ORCL","SPARK_SQL_TEST_WRITE",properties);*/
//		result.show();
//		result.toJavaRDD();
		//1.将结果写入到普通文本文件中(只能输出一列数据，不支持多列数据)
//		result.write().text("e:/temp/spark/sql/text");
		//将结果保存为json文件
		result.write().json("e:/temp/spark/sql/json");

		//将结果保存为csv文件
		result.write().csv("e:/temp/spark/sql/csv");
		//将结果保存为parquet文件
		result.write().parquet("e:/temp/spark/sql/parquet");
	}

	/**
	 * 以csv作为数据源，并读入数据
	 */
	@Test
	void csvDataSource() {
		Dataset<Row> csvDS = spark.read().csv("e:/temp/spark/sql/csv");
		//自带的Schema为_c0,_c1...
		csvDS.printSchema();
		//自己只对Schema
		Dataset<Row> personDS = csvDS.toDF("name", "age");
		csvDS.printSchema();
		personDS.show();
	}

	/**
	 * 以json作为数据源
	 */
	@Test
	void jsonDataSource(){
		Dataset<Row> jsonDS = spark.read().json("e:/temp/spark/sql/json");
		jsonDS.printSchema();
		jsonDS.show();
	}

	/**
	 * 以parquet作为数据源
	 * parquet是列式存储的一种文件格式，SparkSQL可以指定读取parquet的指定的列，过滤掉不需要的列，只读取必要信息，提高效率
	 * (有待完善)
	 */
	@Test
	void parquetDataSource(){
		Dataset<Row> parquetDS = spark.read().parquet("e:/temp/spark/sql/parquet");
		parquetDS.printSchema();
		parquetDS.show();
	}
	@AfterAll
	static void close() {
		spark.stop();
	}
}
