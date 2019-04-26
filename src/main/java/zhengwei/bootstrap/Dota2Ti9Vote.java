package zhengwei.bootstrap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import zhengwei.common.HttpAPIClient;

import java.io.IOException;

/**
 * @author zhengwei AKA DG
 * @since 2019/4/2 14:05
 */
public class Dota2Ti9Vote {
    private static final String URL="https://i.qq.com/";
    public static void main(String[] args) {
        try {
            Document document = Jsoup.connect(URL).get();
            System.out.println(document.title());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
