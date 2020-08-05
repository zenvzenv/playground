package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * @author zhengwei AKA Awei
 * @since 2020/8/5 10:05
 */
public class LeetCodeRob {
    //dp
    public static int robI(int[] nums) {
        final int length = nums.length;
        if (length == 0) return 0;
        if (length == 1) return nums[0];
        int[] dp = new int[length];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < length; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i]);
        }
        return dp[length - 1];
    }

    //dp
    public static int robII(int[] nums) {
        final int length = nums.length;
        if (length == 0) return 0;
        if (length == 1) return nums[0];
        if (length == 2) return Math.max(nums[0], nums[1]);
        return Math.max(
                myRob(Arrays.copyOfRange(nums, 0, length - 1)),
                myRob(Arrays.copyOfRange(nums, 1, length))
        );
    }

    private static int myRob(int[] nums) {
        return robI(nums);
    }

    public static void main(String[] args) {
        System.out.println(robII(new int[]{1, 3, 1, 3, 100}));
    }
}
