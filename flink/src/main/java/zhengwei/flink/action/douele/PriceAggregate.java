package zhengwei.flink.action.douele;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * 对商品价格的聚合
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 9:38
 */
public class PriceAggregate implements AggregateFunction<Tuple3<String, Double, Long>, Double, Double> {
    @Override
    public Double createAccumulator() {
        return 0.0d;
    }

    @Override
    public Double add(Tuple3<String, Double, Long> value, Double accumulator) {
        return accumulator + value.f1;
    }

    @Override
    public Double getResult(Double accumulator) {
        return accumulator;
    }

    @Override
    public Double merge(Double a, Double b) {
        return a + b;
    }
}
