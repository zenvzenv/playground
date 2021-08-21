package zhengwei.leetcode.doubleweek31;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/25 23:31
 */
public class LeetCode5457NumOfSubarrays {
    public int numOfSubarrays(int[] nums) {
        int count = 0, pre = 0;
        Map<Boolean, Integer> map = new HashMap<>();
        for (int num : nums) {
            pre += num;
            boolean flag = (pre & 1) == 1;
            if (flag) map.put(flag, map.getOrDefault(flag, 0) + 1);
        }
        return map.get(true);
    }
}
