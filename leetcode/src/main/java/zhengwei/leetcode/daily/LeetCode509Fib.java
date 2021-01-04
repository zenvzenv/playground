package zhengwei.leetcode.daily;

/**
 * 509. 斐波那契数
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/4 9:14
 */
public class LeetCode509Fib {
    public int fib1(int n) {
        if (n < 2) return n;
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        for (int i = 2; i < dp.length; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    public int fib2(int n) {
        if (n < 2) return n;
        int p = 0, q = 1, result = 0;
        for (int i = 2; i <= n; i++) {
            result = p + q;
            p = q;
            q = result;
        }
        return result;
    }
}
