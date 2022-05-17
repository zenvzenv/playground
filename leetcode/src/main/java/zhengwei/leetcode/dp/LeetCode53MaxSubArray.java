package zhengwei.leetcode.dp;

/**
 * 53. 最大子数组和
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/15
 */
public class LeetCode53MaxSubArray {
    public static int maxSubArray1(int[] nums) {
        if (null == nums || 0 == nums.length) {
            return 0;
        }
        final int len = nums.length;
        if (1 == len) return nums[0];
        // 到当前位置的子数组的最大值
        final int[] dp = new int[len];
        dp[0] = nums[0];
        int result = dp[0];
        for (int i = 1; i < len; i++) {
            dp[i] = Math.max(nums[i] + dp[i - 1], nums[i]);
            result = Integer.max(result, dp[i]);
        }
        return result;
    }

    public static int maxSubArray2(int[] nums) {
        if (null == nums || 0 == nums.length) {
            return 0;
        }
        final int len = nums.length;
        int p = nums[0], result = nums[0];
        for (int i = 1; i < len; i++) {
            p = Math.max(nums[i], p + nums[i]);
            result = Math.max(result, p);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(maxSubArray1(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}));
    }
}
