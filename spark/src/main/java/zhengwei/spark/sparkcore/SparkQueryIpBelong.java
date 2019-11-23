package zhengwei.spark.sparkcore;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import zhengwei.util.common.IpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 查询IP归属运营商
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/10 15:00
 */
public class SparkQueryIpBelong {
	public static void main(String[] args) throws InterruptedException {
		if (args.length < 3) {
			System.out.println("<Usage> SparkQueryIpBelong <inputFilePath> <outputFilePath> <broadcastFilePath>");
			System.exit(-1);
		}
		SparkConf conf = new SparkConf().setAppName("QueryIpBelongToOperator").setMaster("local[2]");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		JavaRDD<String> lines = jsc.textFile(args[0]);
		JavaRDD<String> broadcastRDD = jsc.textFile(args[2]);
		Broadcast<List<long[]>> broadcast = getBroadcast(jsc, broadcastRDD);
		lines.map(line -> {
			String ip = line.split("[|]")[4].trim();
			Long ipLong = IpUtil.ipToLong(ip);
			long[] binarySearch = binarySearch(broadcast, ipLong);
			if (!Objects.isNull(binarySearch)){
				long provLong = binarySearch[2];
				long operatorLong = binarySearch[3];
				return provLong+"|"+operatorLong;
			}
			return null;
		}).filter(Objects::isNull).collect().forEach(System.out::println);
		System.out.println("----------------------------");
		jsc.stop();
	}

	/**
	 * 二分查找法对指定的IP进行查找，是否存在
	 *
	 * @param broadcast 广播文件
	 * @param searchIp 需要查找的IP
	 * @return 指定IP对应的索引
	 */
	static long[] binarySearch(Broadcast<List<long[]>> broadcast, long searchIp) {
		List<long[]> broadcastValue = broadcast.getValue();
		int low = 0;
		int high = broadcastValue.size() - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			if ((searchIp >= broadcastValue.get(middle)[0] && searchIp <= broadcastValue.get(middle)[1])) {
				return broadcastValue.get(middle);
			} else if (searchIp < broadcastValue.get(middle)[0]) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return null;
	}

	static Broadcast<List<long[]>> getBroadcast(JavaSparkContext jsc, JavaRDD<String> broadcastRDD) {
		List<String> broadcastList = broadcastRDD.collect();
		List<long[]> broadcast = new ArrayList<>();
		broadcastList.forEach(x -> {
			long[] info = new long[4];
			String[] fields = x.split("[|]");
			long startIp = Long.parseLong(fields[0].trim());//开始IP
			long endIp = Long.parseLong(fields[1].trim());//结束IP
			long prov = Long.parseLong(fields[2].trim());//省份
			long operator = Long.parseLong(fields[3].trim());//运营商
			info[0] = startIp;
			info[1] = endIp;
			info[2] = prov;
			info[3] = operator;
			broadcast.add(info);
		});
		return jsc.broadcast(broadcast);
	}
}
