package zhengwei.flink.api.cep;

import org.apache.flink.api.common.functions.Function;
import org.apache.flink.cep.pattern.Pattern;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2021/4/28 10:26
 */
public interface InjectionPatternFunction<T> extends Function, Serializable {
    /**
     * 初始化
     */
    void init() throws Exception;

    /**
     * 获取新的 Pattern
     */
    Map<String, Pattern<T, T>> injection();

    /**
     * 一个扫描周期
     */
    long getPeriod();

    /**
     * 规则是否改变
     *
     * @return true-改变，false-未改变
     */
    boolean isChanged() throws Exception;
}
