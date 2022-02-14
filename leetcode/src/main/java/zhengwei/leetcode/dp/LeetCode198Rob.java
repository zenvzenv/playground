package zhengwei.leetcode.dp;

/**
 * 198. 打家劫舍
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/14
 */
public class LeetCode198Rob {
    public int rob1(int[] nums) {
        if (null == nums || 0 == nums.length) {
            return 0;
        }
        final int len = nums.length;
        if (1 == len) return nums[0];
        if (2 == len) return Math.max(nums[0], nums[1]);
        // 此处可以优化成滚动数组
        final int[] dp = new int[len];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < len; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i]);
        }
        return dp[len - 1];
    }

    public int rob2(int[] nums) {
        if (null == nums || 0 == nums.length) {
            return 0;
        }
        final int len = nums.length;
        if (1 == len) return nums[0];
        if (2 == len) return Math.max(nums[0], nums[1]);
        int p = nums[0], q = Math.max(nums[0], nums[1]), result = Integer.MIN_VALUE;
        for (int i = 2; i < len; i++) {
            result = Math.max(q, p + nums[i]);
            p = q;
            q = result;
        }
        return result;
    }
}
