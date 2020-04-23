package zhengwei.LeetCode.Daily;

import java.util.Random;

/**
 * <p>面试题：08.11：硬币</p>
 * 动态规划，背包问题
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/23 9:06
 */
public class LeetCode0811WaysToChange {
    private static int waysToChange(int n) {
        final int[] coins = {1, 5, 10, 25};
        int[] res = new int[n + 1];
        //特殊情况，res[0]=1是为了表示当n正好能够被coin表示的表示方法的次数
        res[0] = 1;
        for (int coin : coins) {
            for (int i = coin; i <= n; i++) {
                res[i] = (res[i] + res[i - coin]) % 1000000007;
            }
        }
        return res[n];
    }

    public static void main(String[] args) {
        System.out.println(waysToChange(new Random().nextInt()));
    }
}
