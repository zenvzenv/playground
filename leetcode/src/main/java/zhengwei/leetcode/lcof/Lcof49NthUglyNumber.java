package zhengwei.leetcode.lcof;

/**
 * 剑指 Offer 49. 丑数
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/4 19:51
 */
public class Lcof49NthUglyNumber {
    public static int nthUglyNumber(int n) {
        //dp[i]表示第i个丑数
        int[] dp = new int[n];
        int a = 0, b = 0, c = 0;
        dp[0] = 1;
        for (int i = 1; i < n; i++) {
            int n2 = dp[a] * 2, n3 = dp[b] * 3, n5 = dp[c] * 5;
            dp[i] = Math.min(Math.min(n2, n3), n5);
            if (dp[i] == n2) a++;
            if (dp[i] == n3) b++;
            if (dp[i] == n5) c++;
        }
        return dp[n - 1];
    }

    public static void main(String[] args) {
        System.out.println(nthUglyNumber(10));
    }
}
