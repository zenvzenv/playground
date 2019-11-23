package zhengwei.spark.sparksql;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
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
 * join等价于inner join
 * left join等价于left outer join
 * right join等价于right outer join
 * full join等价于full outer join
 * left_semi类似于in
 * left_anti类似于not in
 * cross_join表示笛卡儿积
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/8 16:37
 */
public class SparkSQLJoin {
	private static SparkSession spark;
	private static Dataset<Row> tradeDS;
	private static Dataset<Row> orderDS;

	@BeforeAll
	static void init() throws AnalysisException {
		spark = SparkSession
				.builder()
				.master("local[*]")
				.appName("SparkSQLJoin")
				.getOrCreate();
		final JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
		jsc.setLogLevel("WARN");
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
		tradeDS = spark.createDataFrame(tradeRowRDD, tradeStructType);
		tradeDS.createTempView("tradeDS");
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
		orderDS = spark.createDataFrame(orderRowRDD, orderStructType);
		orderDS.createTempView("orderDS");
	}

	/**
	 * 若不指定join类型，默认是inner join
	 */
	@Test
	void testSparkSQLJoin() {
//		tradeDF.show();
//		orderDF.show();
		//不指定inner
		tradeDS.join(orderDS).where(tradeDS.col("orderNo").equalTo(orderDS.col("orderNo"))).where(tradeDS.col("buyer").equalTo(orderDS.col("name"))).show();
//		tradeDF.join(orderDF,tradeDF.col("orderNo").equalTo(orderDF.col("orderNo"))).show();
		//指定inner
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "inner")/*.where(col.equalTo(col))*/.show();
	}

	/**
	 * 以左表为主表，如果左表有而右表没有的以null显示
	 * left join 等价于 left outer join
	 */
	@Test
	void testSparkSQLLeftJoin() {
//		tradeDF.join(orderDF,tradeDF.col("orderNo").equalTo(orderDF.col("orderNo")),"left").show();
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "left_outer").where(tradeDS.col("buyer").equalTo(orderDS.col("name"))).show();
	}

	/**
	 * 以右表为主表，如果右表有而左表没有的话以null显示
	 * right join 等价于 right outer join
	 */
	@Test
	void testSparkSQLRightJoin() {
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "right").show();
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "right_outer").show();
	}

	/**
	 * 获得两个Dataset的并集
	 * 没有字段用null值表示
	 */
	@Test
	void testSparkSQLFullJoin() {
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "full_outer").show();
	}

	/**
	 * 类似于 in
	 */
	@Test
	void testSparkSQLLeftSemi() {
		//select * from tradeDS where orderNo in (select orderNo from orderDS)
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "left_semi").show();
		spark.sql("select * from tradeDS where orderNo in (select orderNo from orderDS)").show();
		//select * from orderDS where orderNo in (select orderNo from tradeDS)
		orderDS.join(tradeDS, orderDS.col("orderNo").equalTo(tradeDS.col("orderNo")), "left_semi").show();
		spark.sql("select * from orderDS where orderNo in (select orderNo from tradeDS)").show();
	}

	/**
	 * 类似于 not in
	 */
	@Test
	void testSparkSQLLeftAnti() {
		//select * from tradeDS where orderNo not in (select orderNo from orderDS)
		tradeDS.join(orderDS, tradeDS.col("orderNo").equalTo(orderDS.col("orderNo")), "left_anti").show();
		spark.sql("select * from tradeDS where orderNo not in (select orderNo from orderDS)").show();
	}

	/**
	 * 笛卡尔连接
	 */
	@Test
	void testSparkSQLCrossJoin() {
		tradeDS.crossJoin(orderDS).show();
	}

	@AfterAll
	static void end() {
		spark.stop();
	}
}
