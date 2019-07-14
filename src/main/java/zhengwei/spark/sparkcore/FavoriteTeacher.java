package zhengwei.spark.sparkcore;

import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计出最受欢迎老师和学科
 * @author zhengwei AKA Sherlock
 * @since 2019/7/4 19:20
 */
public class FavoriteTeacher implements Serializable {
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
		JavaPairRDD<Tuple2<String, String>, Integer> subjectAndTeacherOne = getSubjectAndTeacher();
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
	 * 获取学科和老师
	 * @return [[subject,teacher],1]的二元组
	 */
	private JavaPairRDD<Tuple2<String, String>, Integer> getSubjectAndTeacher() {
		return lines.mapToPair(line -> {
			int i1 = line.lastIndexOf("/");
			String teacher = line.substring(i1 + 1);
			int i2 = line.indexOf("//");
			String url = line.substring(i2 + 2, i1);
			String subject = url.split("[.]")[0];
			return new Tuple2<>(new Tuple2<>(subject, teacher), 1);
		});
	}

	/**
	 * 对method1进行一些调整
	 * 利用spark的排序方法来对数据进行排序，这样就不会有内存溢出的问题了
	 */
	@Test
	void method2(){
		JavaPairRDD<Tuple2<String, String>, Integer> subjectAndTeacherOne = getSubjectAndTeacher();
		//以<subject,teacher>为key对value进行聚合
		JavaPairRDD<Tuple2<String, String>, Integer> reduceSubjectAndTeacher = subjectAndTeacherOne.reduceByKey(Integer::sum);
		//cache到内存中(标记为Cache的RDD以后反复使用，才使用cache)，新生成的RDD只是对原RDD的引用，并不是新的RDD
		JavaPairRDD<Tuple2<String, String>, Integer> reduceSubjectAndTeacherCache = reduceSubjectAndTeacher.cache();
		//获取所有学科
		JavaRDD<String> subjectsRDD = reduceSubjectAndTeacher.map(record -> record._1._1);
		//所有学科集合
		List<String> subjectsList = subjectsRDD.collect();
		subjectsList.forEach(subject->{
			//过滤出指定学科的记录
			JavaPairRDD<Tuple2<String, String>, Integer> filterSubjectRDD = reduceSubjectAndTeacherCache.filter(record -> record._1._1.equals(subject));
			//排序，倒序
			JavaPairRDD<Tuple2<String, String>, Integer> sorted = filterSubjectRDD.mapToPair(record -> new Tuple2<>(record._2, record._1)).sortByKey(false).mapToPair(record -> new Tuple2<>(record._2, record._1));
			sorted.collectAsMap().forEach((k,v)-> System.out.println(k+"--"+v));
		});
	}

	/**
	 * 利用自定义分区器对数据进行分区
	 */
	@Test
	void method3(){
		JavaPairRDD<Tuple2<String, String>, Integer> subjectAndTeacherOne = getSubjectAndTeacher();
		//(shuffle)
		JavaPairRDD<Tuple2<String, String>, Integer> reduceSubjectAndTeacher = subjectAndTeacherOne.reduceByKey(Integer::sum);
		//获取所有学科
		JavaRDD<String> subjectsRDD = reduceSubjectAndTeacher.map(record -> record._1._1);
		//所有学科集合
		List<String> subjectsList = subjectsRDD.distinct().collect();
		//利用自定义分区器对subject进行分区,现在一个分区中只会有一个学科的数据，可以直接对一个分区中的数据进行排序操作(shuffle)
		JavaPairRDD<Tuple2<String, String>, Integer> partitionBySubject = reduceSubjectAndTeacher.partitionBy(new SubjectPartition(subjectsList));
		//一个学科对应一个分区，直接对分区进行操作就是对每一个学科的数据进行操作
		JavaPairRDD<Tuple2, Integer> sorted = partitionBySubject.mapPartitionsToPair(iter -> {
			//这里还是存在隐患，这里的排序还是用的Java的排序方法，如果一个分区的数据太大也会把内存撑爆
			Map<Tuple2, Integer> map = new HashMap<>();
			iter.forEachRemaining(record -> map.put(record._1, record._2));
			List<Tuple2<Tuple2, Integer>> list = new ArrayList<>();
			Map<Tuple2, Integer> result = map.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
			result.forEach((k, v) -> list.add(new Tuple2<>(k, v)));
			return list.iterator();
		});
		List<Tuple2<Tuple2, Integer>> collect = sorted.collect();
		sorted.saveAsTextFile("");
		collect.forEach(x-> System.out.println(x.toString()));
	}

	/**
	 * 减少shuffle次数
	 */
	@Test
	void method4(){
		JavaPairRDD<Tuple2<String, String>, Integer> subjectAndTeacherOne = getSubjectAndTeacher();
		//获取所有学科
		List<String> subjectsList = subjectAndTeacherOne.map(record -> record._1._1).collect();
		//(shuffle)汇聚的同时指定分区器，减少shuffle的次数
		JavaPairRDD<Tuple2<String, String>, Integer> reduceSubjectAndTeacher = subjectAndTeacherOne.reduceByKey(new SubjectPartition(subjectsList),Integer::sum);
		reduceSubjectAndTeacher.mapPartitionsToPair(iter->{
			return null;
		});
	}
	@AfterAll
	static void end(){
		jsc.stop();
	}
}

/**
 * 自定义分区器，按照学科进行分区划分
 */
class SubjectPartition extends Partitioner {
	private List<String> subjects;
	private Map<String,Integer> rules=new HashMap<>();
	private static int index=0;
	public SubjectPartition(List<String> subjects) {
		this.subjects = subjects;
		this.subjects.forEach(subject->{
			rules.put(subject,index);
			index++;
		});
	}

	@Override
	public int numPartitions() {
		return this.subjects.size();
	}

	@Override
	public int getPartition(Object key) {
		String subject = (String) ((Tuple2) key)._1;
		return this.rules.get(subject);
	}
}
