package zhengwei.leetcode.daily;

/**
 * LeetCode第96题：不同的二叉搜索树
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/15 11:09
 */
public class LeetCode96NumTrees {
    public static int numTrees(int n) {
        //dp[i] = dp[i - 1] * dp[n - i]
        int[] dp = new int[n + 1];
        dp[0] = 1;
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            for (int j = 1; j <= i; j++) {
                dp[i] = dp[j - 1] * dp[i - j];
            }
        }
        return dp[n];
    }
}
