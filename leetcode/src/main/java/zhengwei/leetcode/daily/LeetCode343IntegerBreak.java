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

    //这于减绳子类似，尽量取3的幂
    public static int integerBreak2(int n) {
        if (n <= 3) return n - 1;
        int remainder = n % 3;
        int quotient = n / 3;
        if (remainder == 0) return (int) Math.pow(3, quotient);
        if (remainder == 1) return (int) Math.pow(3, quotient - 1) * 4;
        else return (int) Math.pow(3, quotient) * 2;
    }

    public static void main(String[] args) {
        System.out.println(integerBreak(3));
    }
}
