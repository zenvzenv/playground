package zhengwei.spark.accumulator;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.util.AccumulatorV2;
import zhengwei.util.common.SparkUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhengwei AKA Awei
 * @since 2020/4/9 11:39
 */
public class MyAccumulator<B> extends AccumulatorV2<B, Set<B>> implements Serializable {
    private final Set<B> set = new HashSet<>();

    @Override
    public boolean isZero() {
        return set.isEmpty();
    }

    @Override
    public AccumulatorV2<B, Set<B>> copy() {
        return this;
    }

    @Override
    public void reset() {
        set.clear();
    }

    @Override
    public void add(B v) {
        set.add(v);
    }

    @Override
    public void merge(AccumulatorV2<B, Set<B>> other) {
        set.addAll(other.value());
    }

    @Override
    public Set<B> value() {
        return set;
    }

    public static void main(String[] args) {
        final JavaSparkContext jsc = SparkUtils.INSTANCE.getJsc();
        MyAccumulator<String> accumulator = new MyAccumulator<>();
        jsc.sc().register(accumulator, "MyAcc");
        final List<String> result = jsc.parallelize(Arrays.asList("2", "4", "2", "1", "3", "2", "1"), 4)
                .map(x -> {
                    accumulator.add(x);
                    System.out.println(x);
                    return x + "____";
                })
                .collect();
        System.out.println(accumulator.value());
    }
}
