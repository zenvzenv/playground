package zhengwei.leetcode.daily;

/**
 * LeetCode第746题：使用最小的花费爬楼梯
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/6 17:20
 */
public class LeetCode746MinCostClimbingStairs {
    private static int minCostClimbingStairs(int[] cost) {
        //状态转移方程
        //dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i - 2]
        int[] dp = new int[cost.length + 3];
        //辅助变量，为了计算计算第一个楼梯的值
        //实际的数组是(以第一个例子为例):[0, 0, 10, 15, 20, 0(顶点)]
        dp[0] = 0;
        dp[1] = 0;
        for (int i = 2; i < dp.length - 1; i++) {
            //因为实际的dp数组向右移动了两个位置，在取cost的时候需要向左移动两个位置
            dp[i] = Math.min(dp[i - 1], dp[i - 2]) + cost[i - 2];
        }
        //对于山顶需要特殊处理，山顶的体力值为0
        dp[dp.length - 1] = Math.min(dp[dp.length - 2], dp[dp.length - 3]);
        return dp[cost.length + 2];
    }

    public static void main(String[] args) {
        System.out.println(minCostClimbingStairs(new int[]{10, 15, 20}));
    }
}
