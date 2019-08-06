package zhengwei.spark.sparksql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import zhengwei.common.IpUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhengwei AKA Awei
 * @since 2019/7/21 9:13
 */
public class SparkSQL {
	private static SparkSession spark;
	private static Dataset<Row> df;
	private static JavaSparkContext jsc;

	@BeforeAll
	static void init() {
		//创建SparkSession，和SparkCore创建上下文的方式不一样
		//SparkCore创建JavaSparkContext，而SparkSQL创建的是SparkSession
		spark = SparkSession
				.builder()
				.master("local[*]")
				.appName("SparkSQL")
				.config("spark.some.config.option", "some-value")
				.config("spark.sql.shuffle.partitions", 1)//配置join或者聚合操作shuffle数据时分区的数量，数据量大时可以调大这个参数，默认200
				.getOrCreate();
		//创建spark core上下文
		jsc = new JavaSparkContext(spark.sparkContext());
		//读取json->Dataset，读取之后字段会按ASCII表进行排序
		//不能读取嵌套的json文件
		df = spark.read().json("src/main/resources/input/SparkSQLTestFile.zw");
	}

	/**
	 * Spark SQL用到的实体类必须是被public修饰的要不然会报错，会获取不到内部属性
	 * Caused by: java.util.concurrent.ExecutionException: org.codehaus.commons.compiler.CompileException: File 'generated.java'
	 */
	@Test
	void testSparkSQLJoin() {
		Encoder<PersonInfo> personInfoEncoder = Encoders.bean(PersonInfo.class);
		Dataset<PersonInfo> personDS = spark.createDataset(Arrays.asList(
				new PersonInfo(1, "zhengwei1", "China"),
				new PersonInfo(2, "zhengwei2", "USA"),
				new PersonInfo(3, "zhengwei3", "EURO"),
				new PersonInfo(4, "zhengwei4", "JP")), personInfoEncoder);
		personDS.registerTempTable("person");
		Encoder<Local> localEncoder = Encoders.bean(Local.class);
		Dataset<Local> localDS = spark.createDataset(Arrays.asList(
				new Local("China", "中国"),
				new Local("USA", "美国"),
				new Local("EURO", "欧洲")), localEncoder);
		localDS.registerTempTable("local");
		Dataset<Row> result = spark.sql("select person.no,person.name,person.local,local.nameChina from person join local on person.local=local.nameEnglish");
		result.show();

		Dataset<Row> result2 = personDS.join(localDS, personDS.col("local").equalTo(localDS.col("nameEnglish")), "right");
		result2.show();
	}

	@Test
	void testCreateDataset() {
		Dataset<String> personDS = spark.createDataset(Arrays.asList("1,zhengwei1,China", "2,zhengwei2,USA"), Encoders.STRING());
		MapFunction mapFunction=(MapFunction<String,Tuple3<String,String,String>>) value->{
			String[] fields = value.split("[,]");
			String id = fields[0];
			String name = fields[1];
			String local = fields[2];
			return new Tuple3<>(id, name, local);
		};
		Dataset<Tuple3<String, String, String>> personDSMap = personDS.map(mapFunction, Encoders.tuple(Encoders.STRING(), Encoders.STRING(), Encoders.STRING()));
		Dataset<Row> newPersonDS = personDSMap.toDF("id", "name", "local");
		newPersonDS.show();
	}

	/**
	 * Spark SQL版本的IP归属地统计
	 */
	@Test
	void sparkIpLocation() {
		Dataset<String> lines = spark.read().textFile("src/main/resources/input/prov_operator_info.txt");
		MapFunction mapFunction=(MapFunction<String, Tuple4<Long, Long, String, String>>) line -> {
			String[] fields = line.split("[|]");
			long srcIp = Long.parseLong(fields[0]);
			long destIp = Long.parseLong(fields[1]);
			String province = fields[2];
			String operator = fields[3];
			return new Tuple4<>(srcIp, destIp, province, operator);
		};
		Dataset<Tuple4<Long, Long, String, String>> provinceOperateInfoDS = lines.map(mapFunction, Encoders.tuple(Encoders.LONG(), Encoders.LONG(), Encoders.STRING(), Encoders.STRING()));
		Dataset<Row> newProvinceOperatorDS = provinceOperateInfoDS.toDF("srcIp", "destIp", "province", "operator");
		newProvinceOperatorDS.registerTempTable("ipInfo");
		Dataset<String> logDS = spark.read().textFile("src/main/resources/input/netflow.zw");
		Dataset<Row> logInfo = logDS.map((MapFunction<String, Long>) line -> {
			String[] fields = line.split("[|]");
			return IpUtil.ipToLong(fields[0]);
		}, Encoders.LONG()).toDF("ip");
		logInfo.registerTempTable("logInfo");
		Dataset<Row> result = spark.sql("select province,count(*) counts from ipInfo join logInfo on srcIp<=ip and destIp>=ip group by province order by counts desc");
		result.show();
	}

	/**
	 * 自定Spark SQL中的函数
	 */
	/*@Test
	void sparkSQLUDF() {
		Dataset<String> lines = spark.read().textFile("src/main/resources/input/prov_operator_info.txt");
		Dataset<Tuple4<Long, Long, String, String>> broadcastDS = lines.map((MapFunction<String, Tuple4<Long, Long, String, String>>) line -> {
			String[] fields = line.split("[|]");
			long srcIp = IpUtil.ipToLong(fields[0]);
			long destIp = IpUtil.ipToLong(fields[1]);
			String province = fields[2];
			String operator = fields[3];
			return new Tuple4<>(srcIp, destIp, province, operator);
		}, Encoders.tuple(Encoders.LONG(), Encoders.LONG(), Encoders.STRING(), Encoders.STRING()));
		Tuple4<Long, Long, String, String>[] ipInfoBroadcast = broadcastDS.collect();
		//广播匹配规则
		Broadcast<Tuple4<Long, Long, String, String>[]> broadcast = jsc.broadcast(ipInfoBroadcast);
		Dataset<String> logDS = spark.read().textFile("src/main/resources/input/netflow.zw");
		Dataset<Row> newLogDS = logDS.map((MapFunction<String, Long>) log -> {
			String[] fileds = log.split("[|]");
			return IpUtil.ipToLong(fileds[0]);
		}, Encoders.LONG()).toDF("ip_long");
		newLogDS.registerTempTable("v_log");

		*//*
		自定义Spark SQL函数
		需要自己注册，指定函数的名字，函数体，和函数返回类型
		这样的效率比join的效率要高，因为在不使用自定义函数之前，所有的IP比对都是比较的两张临时表通过join来完成，这样数据量如果小的话，还是能够接受的，数据量一大的话，就会有大量的shuffle，效率很低
		自定义函数这种方法避免了两张临时表的join操作，也就避免了shuffle操作，对比规则也是被广播出去的，每个Executor中都会有一份广播变量
		自定函数也是在Executor中执行的，是由Driver发送给Executor的。
		*//*
		spark.udf().register("ip2Province", (UDF1<Long, String>) ipLong -> {
			Tuple4<Long, Long, String, String> resultTuple = binarySearch(broadcast, ipLong);
			if (SysUtils.isNotNull(resultTuple)) {
				return resultTuple._3();
			}
			return "未知";
		}, DataTypes.LongType);
		Dataset<Row> result = spark.sql("select ip2Province(ip),count(*) counts from v_log group by province order by counts desc");
		result.show();
	}*/

	/**
	 * 二分查找法
	 *
	 * @param broadcast 匹配规则的广播变量
	 * @param searchIp  需要匹配的IP
	 * @return 如果匹配到了则返回IP所在那一个Tuple元祖，如果没找到则返回null
	 */
	static Tuple4<Long, Long, String, String> binarySearch(Broadcast<Tuple4<Long, Long, String, String>[]> broadcast, long searchIp) {
		Tuple4<Long, Long, String, String>[] broadcastValue = broadcast.getValue();
		int low = 0;
		int high = broadcastValue.length - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			if ((searchIp >= broadcastValue[middle]._1() && searchIp <= broadcastValue[middle]._2())) {
				return broadcastValue[middle];
			} else if (searchIp < broadcastValue[middle]._1()) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return null;
	}

	@Test
	void test111() {
		JavaRDD<String> rdd1 = jsc.parallelize(Arrays.asList("1", "2", "3"), 3);
		JavaRDD<String> map = rdd1.map(x -> format(x));
		map.collect();
	}

	static String format(String s) throws UnknownHostException {
		System.out.println(Thread.currentThread());
		return "zw-" + s;
	}

	@Test
	void testMyUDAF() {
		GeoMean geoMean = new GeoMean();
		Dataset<Long> range = spark.range(1, 11);
		range.registerTempTable("v_range");
		spark.udf().register("gm", geoMean);
		Dataset<Row> result = spark.sql("select gm(id) result from v_range");
		result.show();
	}

	@Test
	void testSparkSQLFavTeacher() throws AnalysisException {
		Dataset<String> teacherDS = spark.read().textFile("src/main/resources/input/teacher.log");
		Dataset<Row> subjectAndTeacherDS = teacherDS.map((MapFunction<String, Tuple2<String, String>>) line -> {
			int lastIndexOf = line.lastIndexOf("/");
			String teacher = line.substring(lastIndexOf + 1);
			String subject = new URL(line).getHost().split("[.]")[0];
			return new Tuple2<String, String>(subject, teacher);
		}, Encoders.tuple(Encoders.STRING(), Encoders.STRING())).toDF("subject", "teacher");
		/*MapFunction mapFunction= (MapFunction<String, Tuple2<String, String>>) line -> {
			int lastIndexOf = line.lastIndexOf("/");
			String teacher = line.substring(lastIndexOf + 1);
			String subject = new URL(line).getHost().split("[.]")[0];
			return new Tuple2<String, String>(subject, teacher);
		};
		teacherDS.map(mapFunction,Encoders.tuple(Encoders.STRING(),Encoders.STRING()));*/
		/*MapFunction mapFunction= (MapFunction<String, Tuple2<String, String>>) line -> {
			int lastIndexOf = line.lastIndexOf("/");
			String teacher = line.substring(lastIndexOf + 1);
			String subject = new URL(line).getHost().split("[.]")[0];
			return new Tuple2<>(subject, teacher);
		};
		MapFunction mapFunction1=(Object line)->{
			String s = line.toString();
			int lastIndexOf = s.lastIndexOf("/");
			String teacher = s.substring(lastIndexOf + 1);
			String subject = new URL(s).getHost().split("[.]")[0];
			return new Tuple2<>(subject, teacher);
		};
		Dataset subjectAndTeacherDS = teacherDS.map(mapFunction, Encoders.tuple(Encoders.STRING(), Encoders.STRING())).toDF("subject", "teacher");*/
		subjectAndTeacherDS.createTempView("v_subject_teacher");
		Dataset<Row> result1 = spark.sql("SELECT subject, teacher, count(*) counts FROM v_subject_teacher GROUP BY subject, teacher");
		result1.createTempView("v_temp_subject_teacher_count");
		Dataset<Row> result2 = spark.sql("SELECT *, row_number() over(order by counts desc) g_rk FROM (SELECT subject, teacher, counts, row_number() over(partition by subject order by counts desc) sub_rk FROM v_temp_subject_teacher_count) temp2 WHERE sub_rk <= 3");

		result2.show();
	}

	public static void main(String[] args) throws MalformedURLException {
		String host = new URL("http://javaee.edu360.cn/laoyang").getHost();
		System.out.println(host);
	}

	@AfterAll
	static void end() {
		spark.stop();
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PersonInfo {
		private int no;
		private String name;
		private String local;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Local {
		private String nameEnglish;
		private String nameChina;
	}
}

/**
 * 自定义Spark SQL聚合函数
 * 求算术平方根
 */
class GeoMean extends UserDefinedAggregateFunction {
	private StructType inputSchema;
	private StructType bufferSchema;

	public GeoMean() {
		List<StructField> inputFields = new ArrayList<>();
		inputFields.add(DataTypes.createStructField("value", DataTypes.DoubleType, true));
		this.inputSchema = DataTypes.createStructType(inputFields);

		List<StructField> bufferFields = new ArrayList<>();
		bufferFields.add(DataTypes.createStructField("product", DataTypes.DoubleType, true));
		bufferFields.add(DataTypes.createStructField("count", DataTypes.LongType, true));
		this.bufferSchema = DataTypes.createStructType(bufferFields);
	}

	//该聚合函数的输入参数类型
	@Override
	public StructType inputSchema() {
		return inputSchema;
	}

	//集合函数缓冲区中的数据类型(有序的，索引从0开始)
	@Override
	public StructType bufferSchema() {
		return bufferSchema;
	}

	//返回值类型
	@Override
	public DataType dataType() {
		return DataTypes.DoubleType;
	}

	//这个函数是否具有在相同的输入上返回相同的输出，一般为true
	@Override
	public boolean deterministic() {
		return true;
	}

	//初始化给定的聚合函数缓冲区，在索引为0的值的初始值为1，索引值为1的值得参与运算得数得个数应该为0
	@Override
	public void initialize(MutableAggregationBuffer buffer) {
		buffer.update(0, 1D);//乘积
		buffer.update(1, 0L);//数据个数
	}

	//更新，合并中间结果(合并每个分区的运算)
	//buffer->当前值
	//input->同一分区中要合并的值
	@Override
	public void update(MutableAggregationBuffer buffer, Row input) {
		if (!input.isNullAt(0)) {
			double product = buffer.getDouble(0) * input.getDouble(0);
			long count = buffer.getLong(1) + 1L;
			buffer.update(0, product);
			buffer.update(1, count);
		}
	}

	//各个分区进行合并，并把结果更新到buffer1中
	@Override
	public void merge(MutableAggregationBuffer buffer1, Row buffer2) {
		//每个分区计算的结果进行相乘
		double product = buffer1.getDouble(0) * buffer2.getDouble(0);
		//每个分区参与计算的数的个数
		long count = buffer1.getLong(1) + buffer2.getLong(1);
		buffer1.update(0, product);
		buffer1.update(1, count);
	}

	//计算最终结果
	@Override
	public Object evaluate(Row buffer) {
		return Math.pow(buffer.getDouble(0), (double) 1 / buffer.getLong(1));
	}
}