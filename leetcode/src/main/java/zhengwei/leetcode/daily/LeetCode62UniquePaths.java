package zhengwei.leetcode.daily;

/**
 * LeetCode第62题：不同路径
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/6 20:38
 */
public class LeetCode62UniquePaths {
    public static int uniquePaths(int m, int n) {
        //dp[i][j] = dp[i - 1][j] + dp[i][j - 1]
        int[][] dp = new int[m][n];
        dp[0][0] = 0;
        dp[0][1] = 1;
        dp[1][0] = 1;
        for (int i = 2; i < m; i++) {
            for (int j = 2; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }

    public static void main(String[] args) {
        System.out.println(uniquePaths(3, 2));
    }
}
