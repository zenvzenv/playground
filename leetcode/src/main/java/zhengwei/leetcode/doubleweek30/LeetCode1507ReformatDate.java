package zhengwei.leetcode.doubleweek30;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/13 10:03
 */
public class LeetCode1507ReformatDate {
    private static final Map<String, String> map = new HashMap<>();

    static {
        map.put("Jan", "01");
        map.put("Feb", "02");
        map.put("Mar", "03");
        map.put("Apr", "04");
        map.put("May", "05");
        map.put("Jun", "06");
        map.put("Jul", "07");
        map.put("Aug", "08");
        map.put("Sep", "09");
        map.put("Oct", "10");
        map.put("Nov", "11");
        map.put("Dec", "12");
    }

    public static String reformatDate(String date) {
        final String[] dates = date.split(" ");
        String day = dates[0].substring(0, dates[0].length() - 2);
        String month = dates[1];
        final String year = dates[2];
        month = map.get(month);
        if (Integer.parseInt(day) < 10) {
            day = "0" + day;
        }
        return year + "-" + month + "-" + day;
    }
}
