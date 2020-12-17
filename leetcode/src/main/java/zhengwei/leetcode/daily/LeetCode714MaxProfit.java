package zhengwei.leetcode.daily;

/**
 * 714. 买卖股票的最佳时机含手续费
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/17 9:19
 */
public class LeetCode714MaxProfit {
    public int maxProfit(int[] prices, int fee) {
        //dp[i][0] 代表不持有股票时能获得的最大利润
        //dp[i][1] 代表持有股票时能获得的最大利润
        int[][] dp = new int[prices.length][2];
        //对于第一天不持有股票时没有利润
        dp[0][0] = 0;
        //对于第一天持有股票时利润为负数，即买入股票的价格
        dp[0][1] = -prices[0];
        for (int i = 1; i < prices.length; i++) {
            //对于今天不持有可以从两个状态转移
            //1. 昨天也不持有
            //2. 昨天持有，今天卖出
            //两者取最大值
            dp[i][0] = Math.max(dp[i - 1][0], dp[i][1] + prices[i] - fee);

            //对于今天持有可以从两个状态转移
            //1. 昨天也持有
            //2. 昨天不持有，今天买入
            //两者取最大值即可
            dp[i][1] = Math.max(dp[i][1], dp[i][0] - prices[i]);
        }
        return dp[prices.length - 1][0];
    }
}
