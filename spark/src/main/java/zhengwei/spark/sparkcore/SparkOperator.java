package zhengwei.spark.sparkcore;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * spark的算子操作
 * @author zhengwei AKA Sherlock
 * @since 2019/5/6 18:54
 */
class SparkOperator implements Serializable {
    private static JavaSparkContext jsc;
    private static JavaPairRDD<String, Integer> rdd1;
    private static JavaPairRDD<String, String> rdd2;
    private static JavaRDD<String> rdd3;
    @BeforeAll
    static void getJsc(){
        final SparkConf conf = new SparkConf().setMaster("local").setAppName("test");
        jsc = new JavaSparkContext(conf);
        //第二个参数可以指定分区数
        rdd1 = jsc.parallelizePairs(Arrays.asList(
                new Tuple2<>("zw1", 18),
                new Tuple2<>("zw1", 19),
                new Tuple2<>("zw2", 20),
                new Tuple2<>("zw2", 21),
                new Tuple2<>("zw3", 22),
                new Tuple2<>("zw3", 23)),2
        );
        rdd2 = jsc.parallelizePairs(Arrays.asList(
                new Tuple2<>("zw1", "18"),
                new Tuple2<>("zw1", "19"),
                new Tuple2<>("zw2", "20"),
                new Tuple2<>("zw2", "21"),
                new Tuple2<>("zw3", "22"),
                new Tuple2<>("zw3", "23")),2
        );
        rdd3 = jsc.parallelize(Arrays.asList("zw1","zw2","zw3","zw4","zw5","zw6","zw7","zw8","zw9","zw10","zw11","zw12"),3);
    }

    /**
     * 测试mapPartitionsWithIndex,repartition,coalesce算子
     */
    @Test
    void testOperator(){
        //生成RDD
        JavaRDD<String> rdd1 = jsc.parallelize(Arrays.asList("zw1", "zw2", "zw3", "zw4", "zw5", "zw6", "zw7", "zw8", "zw9", "zw10"), 3);
        //RDD的分区个数
        System.out.println(rdd1.partitions().size());
        /**
         * 映射分区中的信息，拿到一个分区里面的信息(迭代器)之后处理数据返回处理之后的数据(迭代器)
         * 接受一个匿名函数，匿名函数的第一个参数是分区的索引号，第二个参数是分区中的数据集合，第三个参数是处理完数据之后返回的数据的集合
         * 转换操作
         */
        JavaRDD<String> rdd2 = rdd1.mapPartitionsWithIndex((index, dataOfIndex) -> {
            List<String> list = new ArrayList<>();
            while (dataOfIndex.hasNext()) {
                String next = dataOfIndex.next();
//                System.out.println("partition index is [" + index + "],value is [" + next + "]");
                list.add("partition index is [" + index + "],value is [" + next + "]");
            }
            return list.iterator();
        }, true);

        //重新分区，有Shuffle，先落地再拉取，可以对RDD重新分区，分区可多可少
        //repartition的底层调用的是coalesce(num,true)
        JavaRDD<String> repartitionRDD = rdd2.repartition(4);
        //与repartition一样，可以对RDD进行分区，可以增多分区也可以减少分区
        //第一个参数是要分区的个数，第二个参数是手动指定是否进行Shuffle，默认false不产生Shuttle,true是要产生Shuffle
        JavaRDD<String> coalesceRDD = rdd1.coalesce(4,false);
        //action
        rdd1.collect();
        List<String> rdd2Collect = rdd2.collect();
        rdd2Collect.forEach(System.out::println);
        System.out.println("--------------------------------------------------------");
        List<String> repartitionRDDCollect = repartitionRDD.collect();
        repartitionRDDCollect.forEach(System.out::println);
        jsc.stop();
    }

    /**
     * 测试zip算子和zipWithIndex算子
     */
    @Test
    void testZipOperator(){

        //按key进行分组，key相同的rdd将被分到一起
//        JavaPairRDD<String, Iterable<Integer>> rdd1GroupByKeyRDD = rdd1.groupByKey();
        /*
         * zip算子
         * 将两个RDD压在一起，形成一个新的KV格式RDD
         * 如果两个RDD的分区中的数据条数不一致的话，则会报错
         */
        JavaPairRDD<Tuple2<String, Integer>, Tuple2<String, String>> zipRDD = rdd1.zip(rdd2);
        //如果这里使用System.out::println的话会报object not serializable错
        zipRDD.foreach((VoidFunction<Tuple2<Tuple2<String, Integer>, Tuple2<String, String>>>) tuple2Tupl2 -> System.out.println(tuple2Tupl2));
        /*
         * 测试zipWithIndex算子
         * 把一个RDD压缩成一个KV格式的一个RDD，并加上下标
         */
        JavaPairRDD<Tuple2<String, Integer>, Long> tuple2LongJavaPairRDD = rdd1.zipWithIndex();
        tuple2LongJavaPairRDD.foreach((VoidFunction<Tuple2<Tuple2<String, Integer>, Long>>) tuple2LongTuple2 -> System.out.println(tuple2LongTuple2));
    }

    /**
     * countByKey算子是action算子，是对key相同的进行计数
     * countByValue算子是action算子，是对value相同的进行计数
     */
    @Test
    void testCountBy(){
        Map<String, Long> stringLongMap = rdd1.countByKey();
        stringLongMap.forEach((k,v)-> System.out.println("key->"+k+",value->"+v));
        Map<Tuple2<String, Integer>, Long> tuple2LongMap = rdd1.countByValue();
        tuple2LongMap.forEach((k,v)-> System.out.println("key->"+k+",value->"+v));
    }

    /**
     * map类算子
     * foreach类算子
     */
    @Test
    void testMapOperator(){
        //map算子逐条执行，不区分分区
        JavaRDD<String> map01 = rdd1.map(record-> {
            System.out.println(record._1+"===="+record._2);
            System.out.println("map01 connect to db ...");
            return record._1+"===="+record._2;
        });
        //foreach算子也是逐条执行的，不区分分区
        map01.foreach(x-> System.out.println(x));
        /*
        mapPartitions算子按分区执行，效率要比map算子要高，可以考虑把map算子替换成mapPartitions算子
        需要注意的是mapPartitions接受的函数的参数，iter是每个分区中的数据的迭代器，可以遍历这个迭代器获取到分区中的数据
        最终返回的是一个iterator,这个迭代器包含所有分区处理之后的信息，迭代器中的每个元素对应于一个分区中的数据
         */
        JavaRDD<String> map02 = rdd1.mapPartitions(iter -> {
            List<String> list = new ArrayList<>();
            iter.forEachRemaining(record -> {
                list.add(record._1 + "====" + record._2 + 1 + "====");
                System.out.println("map02 connect to db ...");
            });
            return list.iterator();
        });
        //foreachPartitions算子是区分分区的，效率要比foreach要高
        map02.foreach(x-> System.out.println(x));
        JavaRDD<String> map03 = rdd3.mapPartitions(iterator -> {
            StringBuilder sb = new StringBuilder();
            List<String> list = new ArrayList<>();
            iterator.forEachRemaining(sb::append);
            list.add(sb.toString());
            return list.iterator();
        });
        map03.foreach(x-> System.out.println(x));
    }

    /**
     * 一个Application中有几个Action算子就会有几个Job
     *
     */
    @Test
    void testJobAlone(){
        JavaRDD<String> map = rdd3.map(x -> {
            System.out.println(x);
            x += "_map";
            return x;
        });
        JavaRDD<String> filter = map.filter(x -> {
            System.out.println(x);
            return x.contains("zw");
        });
        filter.collect();
        filter.collect();
    }
}
