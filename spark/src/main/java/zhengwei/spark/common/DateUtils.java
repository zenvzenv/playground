package zhengwei.spark.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/12 19:36
 */
public final class DateUtils {
    private final static int[] daysOfMonthOfLeapYear = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private final static int[] daysOfMonthOfNonLeapYear = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }

    /**
     * 获取 hdfs 的时间路径
     *
     * @param cycle  直到周期
     * @param preday 往前推多少天
     * @return 时间路径
     */
    public static String[] getTimeDayPath(String cycle, int preday) {
        final String daytime = cycle.substring(0, 8);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        Date date = new Date();
        try {
            date = simpleDateFormat.parse(daytime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获取一个日历变量
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        /*
         * 获取 年、月、日、时
         */
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int day = cal.get(Calendar.DATE);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        // 获取前preday小时时间 年、月、日
        cal2.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - preday);
        final int beforeyear = cal2.get(Calendar.YEAR);
        final int beforemonth = cal2.get(Calendar.MONTH) + 1;
        final int beforeday = cal2.get(Calendar.DATE);
        String path_cycleday = "";
        String path_beforeMonthday = "";
        // 是否是同一个月
        if (beforemonth == month) {
            StringBuilder timeDay = new StringBuilder();
            for (int i = beforeday; i < day; i++) {
                timeDay.append(",dd=").append(i);
            }
            timeDay = new StringBuilder(timeDay.substring(1));
            /// user/test/statengine/app_region_index_5min/yyyy=2020/mm=5/dd=11/hh=14/mi=55
            path_cycleday = "yyyy=" + year + "/" + "mm=" + month + "/" + "{" + timeDay + "}" + "/*/*";
        } else {
            StringBuilder timeDay = new StringBuilder();
            int dayOfMonth = isLeapYear(beforeyear) ? daysOfMonthOfLeapYear[beforemonth - 1]
                    : daysOfMonthOfNonLeapYear[beforemonth - 1];
            for (int i = beforeday; i <= dayOfMonth; i++) {
                timeDay.append(",dd=").append(i);
            }
            timeDay = new StringBuilder(timeDay.substring(1, timeDay.length()));
            path_beforeMonthday = "yyyy=" + beforeyear + "/" + "mm=" + beforemonth + "/" + "{" + timeDay + "}" + "/*/*";

            timeDay = new StringBuilder();
            for (int i = 1; i < day; i++) {
                timeDay.append(",dd=").append(i);
            }
            if (!timeDay.toString().equals("")) {
                timeDay = new StringBuilder(timeDay.substring(1, timeDay.length()));
                path_cycleday = "yyyy=" + year + "/" + "mm=" + month + "/" + "{" + timeDay + "}" + "/*/*";
            }
        }

        if (path_cycleday.equals(""))
            return new String[]{path_beforeMonthday};
        return new String[]{path_cycleday, path_beforeMonthday};
    }

    public static String longToStringByFmt(long time, String fmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        Date dt = new Date();
        dt.setTime(time * 1_000L);
        return sdf.format(dt);
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(getTimeDayPath("20201113172000", 30)));
        System.out.println(longToStringByFmt(System.currentTimeMillis()/1_000L,"HHmm"));
    }
}
