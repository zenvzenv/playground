package zhengwei.antlr.userviolation;

import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import java.time.Duration;

/**
 * @author zhengwei AKA zenv
 * @since 2021/6/23 10:03
 */
public class MyWatermarkStrategy implements WatermarkStrategy<Event> {

    @Override
    public WatermarkGenerator<Event> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new MyWatermarkGen(Duration.ofSeconds(0));
    }
}
