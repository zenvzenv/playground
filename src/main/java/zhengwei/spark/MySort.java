package zhengwei.spark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import scala.Tuple3;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 自定义排序
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/11 19:19
 */
class MySort {
	private static JavaRDD<People> peopleJavaRDD;
	private static JavaRDD<String> lines;

	@BeforeAll
	static void init() {
		SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("MySort");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		lines = jsc.parallelize(Arrays.asList("zhengwei1 18 80", "zhengwei2 23 89", "zhengwei3 25 99", "zhengwei4 23 60", "zhengwei5 24 99"));
		peopleJavaRDD = lines.map(line -> new People(
				line.split("[ ]")[0],
				Integer.parseInt(line.split(" ")[1]),
				Integer.parseInt(line.split(" ")[2])
		));
	}

	/**
	 * 利用对象实现Comparable接口实现排序
	 */
	@Test
	void sort1() {
		//false表降序
		JavaRDD<People> sortedPeopleRDD = MySort.peopleJavaRDD.sortBy(people -> people, false, 1);
		sortedPeopleRDD.foreach(people -> System.out.println(people));
	}
	@Test
	void sort2(){
		JavaRDD<HandsomeMan> handsomeManJavaRDD = lines.map(line -> new HandsomeMan(new Tuple3<>(
				line.split(" ")[0],
				Integer.parseInt(line.split(" ")[1]),
				Integer.parseInt(line.split(" ")[2])
		)));
		JavaRDD<HandsomeMan> sort = handsomeManJavaRDD.sortBy(handsomeMan -> handsomeMan, false, 1);
		sort.foreach(x-> System.out.println(x));
	}

	/**
	 * 注意：此方法在Java中不起作用，scala.Tuple2 cannot be cast to java.lang.Comparable
	 * 利用元祖的排序规则进行排序
	 * 元祖排序规则：按参数顺序依次进行比较
	 */
	@Test
	void sort3(){
		JavaRDD<Tuple3<String, Integer, Integer>> tuple3JavaRDD = lines.map(line -> new Tuple3<>(
				line.split(" ")[0],
				Integer.parseInt(line.split(" ")[1]),
				Integer.parseInt(line.split(" ")[2])
		));
		int partitions = tuple3JavaRDD.getNumPartitions();
		JavaRDD<Tuple3<String, Integer, Integer>> sorted = tuple3JavaRDD.sortBy(tuple3 -> new Tuple2<>(-tuple3._3(), tuple3._2()), false, partitions);
		sorted.foreach(x-> System.out.println(x));
	}
}

@Data
@ToString
@AllArgsConstructor
class People implements Serializable, Comparable<People> {
	private String name;
	private int age;
	private int faceValue;

	//实现排序,返回正数是正序。负数是倒序
	//如果颜值相等的话。根据年龄升序。年龄不等，按照颜值降序
	@Override
	public int compareTo(People o) {
		if (this.faceValue == o.getFaceValue()) {
			return -(this.age - o.getAge());
		} else {
			return this.getFaceValue() - o.getFaceValue();
		}
	}
}
@Data
@ToString
@AllArgsConstructor
class HandsomeMan implements Serializable,Comparable<HandsomeMan>{
	private Tuple3<String,Integer,Integer> ageAndFaceValue;
	@Override
	public int compareTo(HandsomeMan o) {
		if (this.ageAndFaceValue._3().equals(o.getAgeAndFaceValue()._3())){
			return -(this.ageAndFaceValue._2()-o.getAgeAndFaceValue()._2());
		} else{
			return this.getAgeAndFaceValue()._3()-o.getAgeAndFaceValue()._3();
		}
	}
}
