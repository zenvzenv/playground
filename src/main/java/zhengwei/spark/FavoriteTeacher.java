package zhengwei.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计出最受欢迎老师和学科
 * @author zhengwei AKA Sherlock
 * @since 2019/7/4 19:20
 */
public class FavoriteTeacher {
	private final static String FILE="src/main/resources/input/teacher.log";
	private final static JavaSparkContext jsc;
	private static JavaRDD<String> lines;
	static {
		final SparkConf conf=new SparkConf();
		conf.setAppName("Teacher").setMaster("local[2]");
		jsc=new JavaSparkContext(conf);
	}
	@BeforeAll
	static void init(){
		lines=jsc.textFile(FILE);
	}

	/**
	 * 每门学科中老师的排名
	 * 每门学科中，老师的点击量的倒序
	 */
	@Test
	void method1(){
		JavaPairRDD<Tuple2<String, String>, Integer> subjectAndTeacherOne = lines.mapToPair(line -> {
			int i1 = line.lastIndexOf("/");
			String teacher = line.substring(i1 + 1);
			int i2 = line.indexOf("//");
			String url = line.substring(i2 + 2, i1);
			String subject = url.split("[.]")[0];
			return new Tuple2<>(new Tuple2<>(subject, teacher), 1);
		});
		// 以<<subject,teacher>,1>
		JavaPairRDD<Tuple2<String, String>, Integer> reduce = subjectAndTeacherOne.reduceByKey(Integer::sum);
		JavaPairRDD<String, Iterable<Tuple2<Tuple2<String, String>, Integer>>> group = reduce.groupBy(record -> record._1._1);
		//分完组之后，一个分区可能包含多个组，但是组之间是互相的独立的，每个迭代器都包含一个组的信息即每个迭代器中包含一个学科的数据
		JavaPairRDD<String, Map<Tuple2, Integer>> sorted = group.mapValues(iter -> {
			Map<Tuple2, Integer> map = new HashMap<>();
			iter.forEach(x -> map.put(x._1, x._2));
			// 因为以及分好组了，所有的组信息都会在一台节点上，这样我们就可以调用Java的排序方法
			// 调用Java中的排序，其实是把数据都拉取到该节点的内存中进行排序的，并不是分布式的，如果数据量很大的话，可能会把内存撑爆
			return map.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		});
		List<Tuple2<String, Map<Tuple2, Integer>>> collect = sorted.collect();
		collect.forEach(System.out::println);
	}

	/**
	 * 对method1进行一些调整
	 * 利用spark的排序方法来对数据进行排序，这样就不会有内存溢出的问题了
	 */
	@Test
	void method2(){

	}

	public static void main(String[] args) {
		String s="http://bigdata.edu360.cn/laozhang";
		int i1 = s.lastIndexOf("/");
		System.out.println(s.substring(i1+1));
		int i2 = s.indexOf("//");
		System.out.println(s.substring(i2+2,i1));
	}
}
