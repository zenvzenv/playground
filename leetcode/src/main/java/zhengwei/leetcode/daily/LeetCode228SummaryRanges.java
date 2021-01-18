package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.List;

/**
 * 228. 汇总区间
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/10 20:49
 */
public class LeetCode228SummaryRanges {
    public static List<String> summaryRanges(int[] nums) {
        if (null == nums || 0 == nums.length) return new ArrayList<>(0);
        final int length = nums.length;
        List<String> result = new ArrayList<>();
        int p = 0;
        while (p < length) {
            int low = p;
            //为了下次循环 p 的值有变化
            p++;
            while (p < length && nums[p] - nums[p - 1] == 1) {
                p++;
            }
            int high = p - 1;
            if (low == high) result.add(String.valueOf(nums[low]));
            else result.add(nums[low] + "->" + nums[high]);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(summaryRanges(new int[]{0, 1, 2, 4, 5, 7}));
    }
}
