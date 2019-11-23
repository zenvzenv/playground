package zhengwei.temp;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import scala.Tuple2;
import zhengwei.util.common.SparkUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * @author zhengwei AKA Awei
 * @since 2019/11/14 15:20
 */
public class NantongTower {
    @Test
    void nantongTowerBySparkSql() {
        SparkSession session = SparkUtils.INSTANCE.getSparkSession("NantongTower");
        //spark默认csv文件的schema为_c0,_c1,_c2...
        Dataset<Row> nantongTowerEnodebDS = session.read().csv("D:/temp/nantongtower/nantongtowerenodeb.csv");
        //需要自己指定schema,schema是字符串数组
        nantongTowerEnodebDS.toDF("enodeb", "lac", "ci").createOrReplaceTempView("nantongTowerEnodeb");
        Dataset<Row> nantongTowerDataDS = session.read().csv("D:/temp/nantongtower/nantongtowerdata.csv");
        nantongTowerDataDS.toDF("lac", "ci", "cycle_time", "attach_succ_cnt", "attach_num",
                "attach_req_duration", "auc_succ_cnt", "auc_req_cnt", "auc_req_duration",
                "pdp_ms_succ_cnt", "pdp_ms_req_cnt", "pdp_ms_req_duration", "routeupdate_succ_cnt",
                "routeupdate_req_cnt", "routeupdate_req_duration").createOrReplaceTempView("nantongTowerData");
        String sql = "select b.enodeb,a.cycle_time,sum(attach_succ_cnt),sum(attach_num),sum(attach_req_duration) from nantongTowerData a left join nantongTowerEnodeb b on a.lac=b.lac and a.ci=b.ci group by b.enodeb,a.cycle_time";
        session.sql(sql).show();
    }

    public static void main(String[] args) {
        JavaSparkContext jsc = SparkUtils.INSTANCE.getJsc("NantongTower");
        Map<String, String> baseInfoMap = jsc.textFile("D:/temp/nantongtower/nantongtowerenodeb.csv")
                .mapToPair(line -> {
                    String[] fields = line.split("[,]");
                    String baseName = fields[0];
                    String lac = fields[1];
                    String ci = fields[2];
                    return new Tuple2<>(lac + "_" + ci, baseName);
                })
                .collectAsMap();
        Broadcast<Map<String, String>> baseInfoBroadcast = jsc.broadcast(baseInfoMap);
        JavaPairRDD<String, Iterable<String>> stringIterableJavaPairRDD = jsc.textFile("D:/temp/nantongtower/nantongtowerdata.csv")
                .mapToPair(line -> {
                    line = line.trim();
                    String[] fields = line.split("[,]");
                    String lac = fields[0];
                    String ci = fields[1];
                    String cycleTime = fields[2];
                    String baseName = baseInfoBroadcast.value().get(lac + "_" + ci);
                    return new Tuple2<>(cycleTime + "," + baseName, line);
                })
                .groupByKey();
        stringIterableJavaPairRDD
                .map(x -> {
                    String cycleTimeAndName = x._1;
                    Iterator<String> iterator = x._2.iterator();
                    double total_attach_suc_cnt = 0;
                    double total_attach_num = 0;
                    double total_attach_req_duration = 0;
                    double total_auc_suc_cnt = 0;
                    double total_auc_req_cnt = 0;
                    double total_auc_req_duration = 0;
                    double total_pdp_ms_suc_cnt = 0;
                    double total_pdp_ms_req_cnt = 0;
                    double total_pdp_ms_req_duration = 0;
                    double total_route_upd_suc_cnt = 0;
                    double total_route_upd_req_cnt = 0;
                    double total_route_req_duration = 0;
                    //不可使用x._2.iterator()来循环，会死循环。
                    while (iterator.hasNext()) {
                        String[] fields = iterator.next().split("[,]");
                        total_attach_suc_cnt += Double.parseDouble(fields[3]);
                        total_attach_num += Double.parseDouble(fields[4]);
                        total_attach_req_duration += Double.parseDouble(fields[5]);
                        total_auc_suc_cnt += Double.parseDouble(fields[6]);
                        total_auc_req_cnt += Double.parseDouble(fields[7]);
                        total_auc_req_duration += Double.parseDouble(fields[8]);
                        total_pdp_ms_suc_cnt += Double.parseDouble(fields[9]);
                        total_pdp_ms_req_cnt += Double.parseDouble(fields[10]);
                        total_pdp_ms_req_duration += Double.parseDouble(fields[11]);
                        total_route_upd_suc_cnt += Double.parseDouble(fields[12]);
                        total_route_upd_req_cnt += Double.parseDouble(fields[13]);
                        total_route_req_duration += Double.parseDouble(fields[14]);
                    }
                    double attach_suc_rate = total_attach_suc_cnt / total_attach_num;
                    double attach_delay = total_attach_req_duration / total_attach_suc_cnt;
                    double auc_suc_rate = total_auc_suc_cnt / total_auc_req_cnt;
                    double auc_delay = total_auc_req_duration / total_auc_suc_cnt;
                    double pdp_ms_suc_rate = total_pdp_ms_suc_cnt / total_pdp_ms_req_cnt;
                    double pdp_ms_delay = total_pdp_ms_req_duration / total_pdp_ms_suc_cnt;
                    double route_upd_suc_rate = total_route_upd_suc_cnt / total_route_upd_req_cnt;
                    double route_upd_delay = total_route_req_duration / total_route_upd_suc_cnt;
                    return cycleTimeAndName + "," +
                            (Double.isNaN(attach_suc_rate) ? 0.0 : attach_suc_rate) + "," +
                            (Double.isNaN(attach_delay) ? 0.0 : attach_delay) + "," +
                            (Double.isNaN(auc_suc_rate) ? 0.0 : auc_suc_rate) + "," +
                            (Double.isNaN(auc_delay) ? 0.0 : auc_delay) + "," +
                            (Double.isNaN(pdp_ms_suc_rate) ? 0.0 : pdp_ms_suc_rate) + "," +
                            (Double.isNaN(pdp_ms_delay) ? 0.0 : pdp_ms_delay) + "," +
                            (Double.isNaN(route_upd_suc_rate) ? 0.0 : route_upd_suc_rate) + "," +
                            (Double.isNaN(route_upd_delay) ? 0.0 : route_upd_delay);
                })
                .coalesce(1)
                .saveAsTextFile("D:/temp/nantongtower/nantongtowerresult");
    }
}
