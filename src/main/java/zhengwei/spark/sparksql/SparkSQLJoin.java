package zhengwei.spark.sparksql;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Spark SQL join操作
 * 默认是 `inner`. 必须是以下类型的一种:`inner`, `cross`, `outer`, `full`, `full_outer`, `left`, `left_outer`,`right`, `right_outer`, `left_semi`, `left_anti`.
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/8 16:37
 */
public class SparkSQLJoin {
	private static SparkSession spark;
	private static JavaSparkContext jsc;
	private static Dataset<Row> tradeDF;
	private static Dataset<Row> orderDF;

	@BeforeAll
	static void init() {
		spark = SparkSession
				.builder()
				.master("local[*]")
				.appName("SparkSQLJoin")
				.getOrCreate();
		jsc = new JavaSparkContext(spark.sparkContext());
		JavaRDD<String> tradeRDD = jsc.parallelize(Arrays.asList(
				"1 A 小王 北京",
				"2 B 小李 天津",
				"3 A 小刘 北京")
		);
		JavaRDD<Row> tradeRowRDD = tradeRDD.map(x -> RowFactory.create(
				x.split("[ ]")[0],
				x.split("[ ]")[1],
				x.split("[ ]")[2],
				x.split("[ ]")[3]
		));
		List<StructField> tradeSchema = Arrays.asList(
				DataTypes.createStructField("orderNo", DataTypes.StringType, true),
				DataTypes.createStructField("seller", DataTypes.StringType, true),
				DataTypes.createStructField("buyer", DataTypes.StringType, true),
				DataTypes.createStructField("city", DataTypes.StringType, true)
		);
		StructType tradeStructType = DataTypes.createStructType(tradeSchema);
		tradeDF = spark.createDataFrame(tradeRowRDD, tradeStructType);
		JavaRDD<String> orderRDD = jsc.parallelize(Arrays.asList(
				"1 小王 电视 12 2015-08-01 09:08:31",
				"1 小王 冰箱 24 2015-08-01 09:08:14",
				"2 小李 空调 12 2015-09-02 09:01:31"
		));
		JavaRDD<Row> orderRowRDD = orderRDD.map(x -> RowFactory.create(
				x.split("[ ]")[0],
				x.split("[ ]")[1],
				x.split("[ ]")[2],
				x.split("[ ]")[3],
				x.split("[ ]")[4]
		));
		List<StructField> orderSchema = Arrays.asList(
				DataTypes.createStructField("orderNo", DataTypes.StringType, false),
				DataTypes.createStructField("name", DataTypes.StringType, false),
				DataTypes.createStructField("productName", DataTypes.StringType, true),
				DataTypes.createStructField("price", DataTypes.StringType, false),
				DataTypes.createStructField("time", DataTypes.StringType, true)
		);
		StructType orderStructType = DataTypes.createStructType(orderSchema);
		orderDF = spark.createDataFrame(orderRowRDD, orderStructType);
	}

	/**
	 * 若不指定join类型，默认是inner join
	 */
	@Test
	void testSparkSQLJoin(){
//		tradeDF.show();
//		orderDF.show();
		//不指定inner
		tradeDF.join(orderDF).where(tradeDF.col("orderNo").equalTo(orderDF.col("orderNo"))).where(tradeDF.col("buyer").equalTo(orderDF.col("name"))).show();
//		tradeDF.join(orderDF,tradeDF.col("orderNo").equalTo(orderDF.col("orderNo"))).show();
		//指定inner
		tradeDF.join(orderDF,tradeDF.col("orderNo").equalTo(orderDF.col("orderNo")),"inner")/*.where(col.equalTo(col))*/.show();
	}

	@AfterAll
	static void end(){
		spark.stop();
	}
}
