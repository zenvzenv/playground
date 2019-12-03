package zhengwei.bootstrap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/14 10:00
 */
public class ProvinceAndCity {
    public static void main(String[] args) {
        Connection connect = Jsoup.connect("https://xingzhengquhua.51240.com/")
                .userAgent("Mozilla");
        System.out.println(connect);
    }
}
