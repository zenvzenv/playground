package zhengwei.leetcode.lcof;

/**
 * @author zhengwei AKA Awei
 * @since 2020/6/3 11:28
 */
public class Lcof63MaxProfit {
    public int maxProfit(int[] prices) {
        //定义dp[i]是第i天获得的最大利润
        //dp[i] = max(dp[i - 1], dp[i - 1] - prices[i])
        int[] dp = new int[prices.length + 1];
        return 0;
    }
}
