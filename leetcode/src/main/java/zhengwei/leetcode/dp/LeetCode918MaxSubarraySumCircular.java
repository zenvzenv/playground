package zhengwei.leetcode.dp;

/**
 * @author zhengwei AKA zenv
 * @since 2022/2/16
 */
public class LeetCode918MaxSubarraySumCircular {
    public int maxSubarraySumCircular(int[] nums) {
        final int len = nums.length;
        int max = nums[0], min = nums[0], sum = nums[0], maxPre = nums[0], minPre = nums[0];
        // 1. 无回环现象，直接取最大子串和
        // 2. 有回环现象，整个数组的和-最小子串和
        for (int i = 1; i < len; i++) {
            maxPre = Math.max(nums[i], maxPre + nums[i]);
            max = Math.max(max, maxPre);
            sum += nums[i];
            minPre = Math.min(nums[i], minPre + nums[i]);
            min = Math.min(min, minPre);
        }
        // 如果数组内全为负数的话需要特殊考虑
        return max > 0 ? Math.max((sum - min), max) : max;
    }
}
