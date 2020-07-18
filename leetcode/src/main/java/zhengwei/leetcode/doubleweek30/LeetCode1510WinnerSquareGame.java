package zhengwei.leetcode.doubleweek30;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/18 10:54
 */
public class LeetCode1510WinnerSquareGame {
    public static boolean winnerSquareGame(int n) {
        //博弈论
        //dp[0] false 为必败态
        //dp[1] true 为必胜态
        //此后所有状态均可有上面的两个状态推导出来
        boolean[] dp = new boolean[n + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; i - j * j >= 0; j++) {
                if (!dp[i - j * j]) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[n];
    }

    public static void main(String[] args) {
        System.out.println(winnerSquareGame(5));
    }
}
