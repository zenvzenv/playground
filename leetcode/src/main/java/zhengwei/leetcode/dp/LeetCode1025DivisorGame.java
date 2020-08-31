package zhengwei.leetcode.dp;

/**
 * 1025. 除数博弈
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/31 20:13
 */
public class LeetCode1025DivisorGame {
    public boolean divisorGame(int N) {
        //dp[i] 表示当前数据的胜负状态
        boolean[] dp = new boolean[N + 1];
        //必败态
        dp[1] = false;
        //必胜态
        dp[2] = true;
        for (int i = 3; i < N; i++) {
            for (int j = 1; j < i; j++) {
                //dp[i - j] 相当于 Alice 先手进行了选择
                //先手之后如果，当前状态是false(必败态)，那么 Alice 必胜，反之亦然
                if (i % j == 0 && !dp[i - j]) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[N];
    }
}
