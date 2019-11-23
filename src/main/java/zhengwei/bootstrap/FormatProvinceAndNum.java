package zhengwei.bootstrap;

import com.csvreader.CsvWriter;
import org.junit.jupiter.api.Test;
import zhengwei.util.common.SysUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/15 11:07
 */
public class FormatProvinceAndNum {
    private static final Map<String, String> PROVINCE_NUM_MAP = new HashMap<>();

    static {
        try (InputStream is = FormatProvinceAndNum.class.getClassLoader().getResourceAsStream("shidc/province_num.txt");
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while (SysUtils.isNotNull(line = br.readLine())) {
                String[] fields = line.split("[,]");
                PROVINCE_NUM_MAP.put(fields[1], fields[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (InputStream is = FormatProvinceAndNum.class.getClassLoader().getResourceAsStream("shidc/province_city_county.csv");
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
             BufferedReader br = new BufferedReader(isr)) {
            String rec;
            String[] argsArr;
            CsvWriter csvWriter = new CsvWriter("src/main/resources/shidc/result.csv", ',', StandardCharsets.UTF_8);
            Map<String, Set<String>> result = new HashMap<>();
            while (SysUtils.isNotNull(rec = br.readLine())) {
                String[] fields = rec.split("[,]");
                //省份
                String province = fields[0];
                //地市
                String city = fields[1];
                //区县
                String county = fields[2];
                if (!result.containsKey(province)) {
                    Set<String> countySet = new LinkedHashSet<>();
                    if (province.equals(city)) {
                        countySet.add(county);
                    } else {
                        countySet.add(city);
                    }
                    result.put(province, countySet);
                } else {
                    Set<String> countySet = result.get(province);
                    if (province.equals(city)) {
                        countySet.add(county);
                    } else {
                        countySet.add(city);
                    }
                }
            }
            System.out.println(result);
            result.forEach((provinceName, cities) -> {
                //省份编号
                String provinceNum = PROVINCE_NUM_MAP.get(provinceName);
                int length = 4;
                int cityNum = 100;
                for (String city : cities) {
                    String[] content = new String[4];
                    content[0] = provinceName;
                    content[1] = provinceNum;
                    content[2] = city;
                    if (String.valueOf(cityNum).length() < length) {
                        content[3] = provinceNum + "0" + cityNum;
                        cityNum += 100;
                    } else {
                        content[3] = provinceNum + cityNum;
                        cityNum += 100;
                    }
                    try {
                        csvWriter.writeRecord(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    csvWriter.flush();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLinkedHashSet() {
        Set<Integer> s = new LinkedHashSet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        s.forEach(System.out::println);
    }

    /*public static void main(String[] args) throws IOException {
        Document wholeCountyDocument = Jsoup.connect("https://xingzhengquhua.51240.com").get();
        Elements table = wholeCountyDocument.body().select("table");
        Element wholeCounty = table.get(1);
        //各个省
        Elements counties = wholeCounty.select("tr");
        for (int i = 2; i < counties.size(); i++) {
            Element county = counties.get(i);
            String countyName = county.select("td").get(0).text();
            String url = county.select("td").get(1).select("a").attr("href");
            System.out.println(countyName + "-" + url);
            Document countyDoc = Jsoup.connect("https://xingzhengquhua.51240.com" + url).get();
            Elements countyInfos = countyDoc.select("table").get(1).select("tr");
            for (int j = 3; j < countyInfos.size(); j++) {
                Element city = countyInfos.get(i);
                System.out.println(city.select("td").get(0).text());
            }
        }
    }*/
}
