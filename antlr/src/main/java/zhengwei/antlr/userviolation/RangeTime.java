package zhengwei.antlr.userviolation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import zhengwei.util.common.DateUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2021/5/11 9:08
 */
@Setter
@Getter
@AllArgsConstructor
public class RangeTime extends SimpleCondition<Event> {
    private String startTime;
    private String endTime;

    @Override
    public boolean filter(Event o) {
        final long startTimeLong = DateUtils.strToLong("yyyyMMddHHmmss", startTime);
        final long endTimeLong = DateUtils.strToLong("yyyyMMddHHmmss", endTime);
        final long currentTimeSec = System.currentTimeMillis();
        return startTimeLong <= currentTimeSec && currentTimeSec <= endTimeLong;
    }
}
