package zhengwei.spark.udf;

import org.apache.spark.sql.api.java.UDF2;
import zhengwei.spark.common.DateUtils;

/**
 * 将长整型时间转成字符串格式
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/13 17:10
 */
public class LongDateToStringDate implements UDF2<Integer, String, String> {
    /**
     * 将长整型时间转成字符串类型，time 的精度到秒
     *
     * @param time 到秒的长整型时间
     * @param fmt  格式化字符串
     * @return 格式化时间字符串
     */
    @Override
    public String call(Integer time, String fmt) {
        return DateUtils.longToStringByFmt(time, fmt);
    }
}
