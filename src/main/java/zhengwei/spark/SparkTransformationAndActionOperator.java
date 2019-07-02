package zhengwei.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Spark算子的详细版本
 * @author zhengwei AKA Sherlock
 * @since 2019/6/30 8:34
 */
public class SparkTransformationAndActionOperator {
	private static JavaSparkContext jsc;
	private static final SparkConf conf=new SparkConf();
	private static JavaRDD<String> rdd1;
	private static JavaRDD<String> rdd2;
	private static JavaRDD<Integer> rdd3;
	private static JavaRDD<Integer> rdd4;
	private static JavaRDD<String> rdd5;
	private static JavaPairRDD<String,Integer> rdd6;
	@BeforeAll
	static void init(){
		conf.setAppName("SparkOperator").setMaster("local[2]");
		jsc=new JavaSparkContext(conf);
		rdd1= jsc.parallelize(Arrays.asList("zhengwei","zhangsan","lisi","wangwu","maliu"));
		rdd2=jsc.parallelize(Arrays.asList("lisi","wanwu","maliu","zhengwei1","zhengwei2"));
		rdd3=jsc.parallelize(Arrays.asList(1,2,3,4,5,6,7,8,9));
		rdd4=jsc.parallelize(Arrays.asList(5,6,7,8,9,10,11,12,5,6,12));
		rdd5=jsc.parallelize(Arrays.asList("a b c","d e f","g h i"));
		rdd6=jsc.parallelizePairs(Arrays.asList(
				new Tuple2<>("zhengwei1",18),
				new Tuple2<>("zhengwei2",20),
				new Tuple2<>("zhengwei3",22),
				new Tuple2<>("zhengwei1",20),
				new Tuple2<>("zhengwei2",25),
				new Tuple2<>("zhengwei3",30)
		));
	}
	@Test
	void testMap(){
		JavaRDD<String> map = rdd1.map(x -> x + "_map");
		List<String> collect = map.collect();
		collect.forEach(System.out::println);
	}
	@Test
	void testFilter(){
		JavaRDD<Integer> filter = rdd3.filter(x -> x > 5);
		filter.foreach(x-> System.out.println(x));
	}
	@Test
	void testFlatMap(){
		JavaRDD<String> flatMap = rdd5.flatMap(x -> Arrays.asList(x.split("[ ]")).iterator());
		flatMap.foreach(x-> System.out.println(x));
	}
	@Test
	void testMapPartitions(){
		JavaRDD<String> mapPartitions = rdd2.mapPartitions(iter -> {
			List<String> result = new ArrayList<>();
			iter.forEachRemaining(x->result.add(x+"_mapPartitions"));
			return result.iterator();
		});
		mapPartitions.foreach(x-> System.out.println(x));
	}
	@Test
	void testMapPartitionsWithIndex(){
		JavaRDD<String> mapPartitionsWithIndex = rdd2.mapPartitionsWithIndex((index, iter) -> {
			List<String> result=new ArrayList<>();
			//拿到一个分区，然后通过分区获取里面的数据
			iter.forEachRemaining(x -> result.add("partition index->" + index + ",record->" + x));
			return result.iterator();
		},true);
		mapPartitionsWithIndex.foreach(x-> System.out.println(x));
	}
	@Test
	void testUnion(){
		JavaRDD<String> union = rdd1.union(rdd2);
		union.foreach(x-> System.out.println(x));
	}
	@Test
	void testIntersection(){
		JavaRDD<String> intersection = rdd1.intersection(rdd2);
		intersection.foreach(x-> System.out.println(x));
	}
	@Test
	void testDistinct(){
		JavaRDD<Integer> distinct = rdd4.distinct();
		distinct.foreach(x-> System.out.println(x));
	}
	@Test
	void testGroupByKey(){
		JavaPairRDD<String, Iterable<Integer>> groupByKey = rdd6.groupByKey();
		groupByKey.foreach(x->{
			String name = x._1;
			Iterable<Integer> ages = x._2;
			System.out.println(name+"->"+ages.toString());
		});
	}
	@Test
	void testReduceByKey(){
		JavaPairRDD<String, Integer> reduceByKey = rdd6.reduceByKey(Integer::sum);
		reduceByKey.foreach(x-> System.out.println(x._1+"->"+x._2));
	}
	@Test
	void testAggregate(){
		Integer aggregate = rdd4.aggregate(0, Integer::sum, Integer::sum);
		String aggregate1 = rdd1.aggregate("|", (x1, y1) -> x1 + y1, (x2, y2) -> x2 + y2);
		System.out.println(aggregate);
		System.out.println(aggregate1);
	}
	@Test
	void testAggregateByKey(){
		JavaPairRDD<String, Integer> aggregateByKey = rdd6.aggregateByKey(2, Integer::sum, Integer::sum);
		aggregateByKey.foreach(x-> System.out.println(x._1+"->"+x._2));
	}
	@Test
	void testCollectAsMap(){
		Map<String, Integer> collectAsMap = rdd6.mapValues(x -> x * 10).collectAsMap();
		collectAsMap.forEach((k,v)-> System.out.println(k+"->"+v));
	}
	@Test
	void testFlatMapValues(){
		JavaPairRDD<String, String> flatMapValues = rdd1.mapToPair(x -> new Tuple2<>(x, x + "_map")).flatMapValues(x -> Arrays.asList(x.split("[_]")));
		flatMapValues.foreach(x-> System.out.println(x._1+"->"+x._2));
	}
	@Test
	void testFoldByKey(){
		JavaPairRDD<String, Integer> foldByKey = rdd6.foldByKey(0, (x, y) -> Integer.valueOf(x + String.valueOf(y)));
		foldByKey.foreach(x-> System.out.println(x._1+"->"+x._2));
	}
	@Test
	void testCombineByKey(){
		JavaPairRDD<String, Integer> combineByKey = rdd6.combineByKey(x -> x, Integer::sum, Integer::sum);
		combineByKey.collectAsMap().forEach((x,y)-> System.out.println(x+"--"+y));
		JavaPairRDD<String, List<Integer>> combineByKey1 = rdd6.combineByKey(
				c -> {
					List<Integer> list = new ArrayList<>();
					list.add(c);
					return list;
				},
				(c, v) -> {
					c.add(v);
					return c;
				},
				(c1, c2) -> {
					c1.addAll(c2);
					return c1;
				}
		);
		combineByKey1.collectAsMap().forEach((k,v)-> System.out.println(k+"--"+v));
	}
	@AfterAll
	static void end(){
		jsc.stop();
	}
}
