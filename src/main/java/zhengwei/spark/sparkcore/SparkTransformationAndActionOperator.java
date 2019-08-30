package zhengwei.spark.sparkcore;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
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
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/6/30 8:34
 */
public class SparkTransformationAndActionOperator {
	private static JavaSparkContext jsc;
	private static final SparkConf conf = new SparkConf();
	private static JavaRDD<String> rdd1;
	private static JavaRDD<String> rdd2;
	private static JavaRDD<Integer> rdd3;
	private static JavaRDD<Integer> rdd4;
	private static JavaRDD<String> rdd5;
	private static JavaPairRDD<String, Integer> rdd6;
	private static JavaPairRDD<String, Integer> rdd7;
	private static JavaPairRDD<String, Integer> rdd8;

	@BeforeAll
	static void init() {
		conf.setAppName("SparkOperator").setMaster("local[2]");
		jsc = new JavaSparkContext(conf);
		rdd1 = jsc.parallelize(Arrays.asList("zhengwei", "zhangsan", "lisi", "wangwu", "maliu"));
		rdd2 = jsc.parallelize(Arrays.asList("lisi", "wanwu", "maliu", "zhengwei1", "zhengwei2"));
		rdd3 = jsc.parallelize(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
		rdd4 = jsc.parallelize(Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12, 5, 6, 12));
		rdd5 = jsc.parallelize(Arrays.asList("a b c", "d e f", "g h i"));
		rdd6 = jsc.parallelizePairs(Arrays.asList(
				new Tuple2<>("zhengwei1", 18),
				new Tuple2<>("zhengwei2", 20),
				new Tuple2<>("zhengwei3", 22),
				new Tuple2<>("zhengwei1", 20),
				new Tuple2<>("zhengwei2", 25),
				new Tuple2<>("zhengwei3", 30)
		));
		rdd7 = jsc.parallelizePairs(Arrays.asList(
				new Tuple2<>("zhengwei1", 18),
				new Tuple2<>("zhengwei2", 20),
				new Tuple2<>("zhengwei3", 22),
				new Tuple2<>("zhengwei4", 22)
		));
		rdd8 = jsc.parallelizePairs(Arrays.asList(
				new Tuple2<>("zhengwei1", 18),
				new Tuple2<>("zhengwei2", 20),
				new Tuple2<>("zhengwei3", 22),
				new Tuple2<>("zhengwei1", 20)
		));
	}

	@Test
	void testMap() {
		JavaRDD<String> map = rdd1.map(x -> x + "_map");
		List<String> collect = map.collect();
		collect.forEach(System.out::println);
	}

	@Test
	void testFilter() {
		JavaRDD<Integer> filter = rdd3.filter(x -> x > 5);
		filter.foreach(x -> System.out.println(x));
	}

	@Test
	void testFlatMap() {
		JavaRDD<String> flatMap = rdd5.flatMap(x -> Arrays.asList(x.split("[ ]")).iterator());
		flatMap.foreach(x -> System.out.println(x));
	}

	@Test
	void testMapPartitions() {
		JavaRDD<String> mapPartitions = rdd2.mapPartitions(iter -> {
			List<String> result = new ArrayList<>();
			iter.forEachRemaining(x -> result.add(x + "_mapPartitions"));
			return result.iterator();
		});
		mapPartitions.foreach(x -> System.out.println(x));
	}

	@Test
	void testMapPartitionsWithIndex() {
		JavaRDD<String> mapPartitionsWithIndex = rdd2.mapPartitionsWithIndex((index, iter) -> {
			List<String> result = new ArrayList<>();
			//拿到一个分区，然后通过分区获取里面的数据
			iter.forEachRemaining(x -> result.add("partition index->" + index + ",record->" + x));
			return result.iterator();
		}, true);
		mapPartitionsWithIndex.foreach(x -> System.out.println(x));
	}

	@Test
	void testUnion() {
		JavaRDD<String> union = rdd1.union(rdd2);
		union.foreach(x -> System.out.println(x));
	}

	@Test
	void testIntersection() {
		JavaRDD<String> intersection = rdd1.intersection(rdd2);
		intersection.foreach(x -> System.out.println(x));
	}

	@Test
	void testDistinct() {
		JavaRDD<Integer> distinct = rdd4.distinct();
		distinct.foreach(x -> System.out.println(x));
	}

	@Test
	void testGroupByKey() {
		JavaPairRDD<String, Iterable<Integer>> groupByKey = rdd6.groupByKey();
		groupByKey.foreach(x -> {
			String name = x._1;
			Iterable<Integer> ages = x._2;
			System.out.println(name + "->" + ages.toString());
		});
	}

	@Test
	void testReduceByKey() {
		JavaPairRDD<String, Integer> reduceByKey = rdd6.reduceByKey(Integer::sum);
		reduceByKey.foreach(x -> System.out.println(x._1 + "->" + x._2));
	}

	@Test
	void testAggregate() {
		Integer aggregate = rdd4.aggregate(0, Integer::sum, Integer::sum);
		String aggregate1 = rdd1.aggregate("|", (x1, y1) -> x1 + y1, (x2, y2) -> x2 + y2);
		System.out.println(aggregate);
		System.out.println(aggregate1);
	}

	@Test
	void testAggregateByKey() {
		JavaPairRDD<String, Integer> aggregateByKey = rdd6.aggregateByKey(2, Integer::sum, Integer::sum);
		aggregateByKey.foreach(x -> System.out.println(x._1 + "->" + x._2));
	}

	@Test
	void testCollectAsMap() {
		Map<String, Integer> collectAsMap = rdd6.mapValues(x -> x * 10).collectAsMap();
		collectAsMap.forEach((k, v) -> System.out.println(k + "->" + v));
	}

	@Test
	void testFlatMapValues() {
		JavaPairRDD<String, String> flatMapValues = rdd1.mapToPair(x -> new Tuple2<>(x, x + "_map")).flatMapValues(x -> Arrays.asList(x.split("[_]")));
		flatMapValues.foreach(x -> System.out.println(x._1 + "->" + x._2));
	}

	@Test
	void testFoldByKey() {
		JavaPairRDD<String, Integer> foldByKey = rdd6.foldByKey(0, (x, y) -> Integer.valueOf(x + String.valueOf(y)));
		foldByKey.foreach(x -> System.out.println(x._1 + "->" + x._2));
	}

	@Test
	void testCombineByKey() {
		JavaPairRDD<String, Integer> combineByKey = rdd6.combineByKey(x -> x, Integer::sum, Integer::sum);
		combineByKey.collectAsMap().forEach((x, y) -> System.out.println(x + "--" + y));
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
		combineByKey1.collectAsMap().forEach((k, v) -> System.out.println(k + "--" + v));
	}

	@Test
	void testCache() {
		//把RDD缓存到内存中，以分区为单位进行缓存
		/*jsc.setCheckpointDir("e:/temp/");
		JavaRDD<String> cache = rdd1.cache();
		//把缓存的数据抹去，即取消缓存数据
		JavaRDD<String> unpersist = cache.unpersist();
		//设置checkpoint的缓存目录(需要分布式文件系统，高可靠性)，以后缓存的数据将会存放在该hdfs的目录上
		//设置检查点checkpoint
		cache.checkpoint();
		cache.collect();*/
		//从可靠的文件系统中读取检查点数据
		final JavaRDD<Object> checkpointFileRDD = jsc.checkpointFile("e:/temp/b3251938-3644-4248-80b1-75661334e3db/rdd-0");
		checkpointFileRDD.checkpoint();
		System.out.println(checkpointFileRDD.collect().toString());
	}

	@Test
	void testJoin() {
		//join算子只会链接两个RDD中共有的Key值，如果一个RDD中没有相应的Key的话则不会出现在结果中
		JavaPairRDD<String, Tuple2<Integer, Integer>> join = rdd7.join(rdd8);
		join.collect().forEach(System.out::println);
		//左链接，以左RDD为主，左RDD中的所有记录都会出现，如果左RDD中有而右RDD里面没有的话，那么将会以Optional.empty标识
		JavaPairRDD<String, Tuple2<Integer, Optional<Integer>>> leftOuterJoin = rdd7.leftOuterJoin(rdd8);
		leftOuterJoin.collect().forEach(System.out::println);
		//右链接，以右RDD为主，右RDD中的所有记录都会出现，如果右RDD中有而左RDD中没有的话，那么将会以Optional.empty标识
		JavaPairRDD<String, Tuple2<Optional<Integer>, Integer>> rightOuterJoin = rdd7.rightOuterJoin(rdd8);
		rightOuterJoin.collect().forEach(System.out::println);
	}

	@AfterAll
	static void end() {
		jsc.stop();
	}
}
