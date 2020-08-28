package zhengwei.leetcode.daily;

import java.util.List;

/**
 * LeetCode第120题：三角形最小路径和
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/14 9:45
 */
public class LeetCode120MinimumTotal {
    //dp
    public int minimumTotal(List<List<Integer>> triangle) {
        //dp[i][0] = dp[i - 1][0] + triangle[i][0]
        //dp[i][j] = min(dp[i - 1][j], dp[i -1][j - 1]) + triangle[i][j]
        //dp[i][i] = dp[i - 1][i - 1] + triangle[i][i]
        //三角形的层级
        int level = triangle.size();
        int[][] dp = new int[level][level];
        dp[0][0] = triangle.get(0).get(0);
        for (int i = 1; i < level; i++) {
            //三角形中每行的第一列
            dp[i][0] = dp[i - 1][0] + triangle.get(i).get(0);
            for (int j = 1; j < i; j++) {
                //三角形中每行的中间列
                dp[i][j] = Math.min(dp[i - 1][j], dp[i - 1][j - 1]) + triangle.get(i).get(j);
            }
            //三角形中每行的最后一列
            dp[i][i] = dp[i - 1][i - 1] + triangle.get(i).get(i);
        }
        int res = dp[level - 1][0];
        for (int i = 1; i < level; i++) {
            res = Math.min(res, dp[level - 1][i]);
        }
        return res;
    }
}
