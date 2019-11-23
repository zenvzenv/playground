package zhengwei.test;

import com.csvreader.CsvWriter;
import org.junit.jupiter.api.Test;
import zhengwei.util.common.IpUtil;
import zhengwei.util.common.SysUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/12 9:41
 */
public class ProjectTest {
    @Test
    void getStartAndDestIp() {
        String netMask = IpUtil.getNetMask("11");
        System.out.println(netMask);
        System.out.println(IpUtil.getStartAddr("47.96.0.0", netMask));
        System.out.println(IpUtil.ipToLong(IpUtil.getStartAddr("47.96.0.0", netMask)));
        System.out.println(IpUtil.getEndAddr("47.96.0.0", netMask));
        System.out.println(IpUtil.ipToLong(IpUtil.getEndAddr("47.96.0.0", netMask)));
    }

    @Test
    void testIp() {
        /*System.out.println(IpUtil.ipToLong("182.254.130.119"));
        System.out.println(IpUtil.ipToLong("182.254.130.231"));
        System.out.println(IpUtil.ipToLong("182.254.135.15"));
        System.out.println(IpUtil.ipToLong("139.224.95.49"));*/
        System.out.println(IpUtil.ipLongToString(1850486785L));

        System.out.println(IpUtil.ipLongToString(1850408961L));
        System.out.println(IpUtil.ipLongToString(1850474494L));
    }

    @Test
    void unionTencentAndAli() {
        try (InputStream is = new FileInputStream("I:/temp/enterprise_ip_info.txt");
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String rec;
            String[] argsArr;
            CsvWriter csvWriter = new CsvWriter("I:/temp/enterprise_ip_info_new.txt", '|', StandardCharsets.UTF_8);
            Map<Long, String[]> unsortedMap = new HashMap<>();
            while (SysUtils.isNotNull(rec = br.readLine())) {
                String[] content = rec.split("[|]");
                unsortedMap.put(Long.parseLong(content[1]), content);
            }
            LinkedHashMap<Long, String[]> sortedMap = unsortedMap
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            sortedMap.forEach((k, v) -> {
                try {
                    csvWriter.writeRecord(v);
                    csvWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getAliIpTable() {
        try (InputStream is = new FileInputStream("I:/temp/ali_ip.txt");
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String rec;
            String[] argsArr;
            CsvWriter csvWriter = new CsvWriter("I:/temp/ali_ip_result.txt", '|', StandardCharsets.UTF_8);
            Map<Long, String[]> unsortedMap = new HashMap<>();
            while (SysUtils.isNotNull(rec = br.readLine())) {
                String[] content = new String[3];
                argsArr = rec.split("[/]");
                String ip = argsArr[0];
                String mask = argsArr[1];
                String netMask = IpUtil.getNetMask(mask.trim());
                String startIp = String.valueOf(IpUtil.ipToLong(IpUtil.getStartAddr(ip, netMask)));
                String endIp = String.valueOf(IpUtil.ipToLong(IpUtil.getEndAddr(ip, netMask)));
                content[0] = startIp;
                content[1] = endIp;
                content[2] = "210002";
                unsortedMap.put(Long.parseLong(content[1]), content);
            }
            LinkedHashMap<Long, String[]> sortedMap = unsortedMap
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            sortedMap.forEach((k, v) -> {
                try {
                    csvWriter.writeRecord(v);
                    csvWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void formatIpToClient() {
        try (InputStream is = new FileInputStream("I:/temp/ip对应客户.csv");
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String rec;
            String[] argsArr;
            CsvWriter csvWriter = new CsvWriter("I:/temp/ip对应客户_format.csv", '|', StandardCharsets.UTF_8);
            Map<Long, List<String[]>> unsortedMap = new HashMap<>();
            while (SysUtils.isNotNull(rec = br.readLine())) {
                String[] fields = rec.split("[,]");
                String name = fields[1].trim();
                String[] ipAndMask = fields[0].split("[/]");
                String ip = ipAndMask[0].trim();
                for (int i = 1; i < ipAndMask.length; i++) {
                    String[] content = new String[3];
                    String netMask = IpUtil.getNetMask(ipAndMask[i].trim());
                    String startIp = String.valueOf(IpUtil.getStartAddr(ip, netMask));
                    String endIp = String.valueOf(IpUtil.getEndAddr(ip, netMask));
                    content[0] = startIp;
                    content[1] = endIp;
                    content[2] = name;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testTemp() {
//        System.out.println(Long.parseLong("1078.00000000000000000000"));
        System.out.println(Double.parseDouble("1078.00000000000000000000")*8);
    }
}
