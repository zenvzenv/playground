package zhengwei.leetcode.daily;

/**
 * LeetCode第343题：整数拆分的最大乘积
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/17 14:12
 */
public class LeetCode343IntegerBreak {
    public static int integerBreak(int n) {
        //dp[n]表示正整数n的最大乘积
        int[] dp = new int[n + 7];
        dp[0] = 1;
        dp[1] = 1;
        dp[2] = 1;
        dp[3] = 2;
        dp[4] = 4;
        dp[5] = 6;
        dp[6] = 9;
        dp[7] = 12;
        for (int i = 8; i < dp.length; i++) {
            dp[i] = dp[i - 3] * 3;
        }
        return dp[n];
    }

    public static void main(String[] args) {
        System.out.println(integerBreak(3));
    }
}
