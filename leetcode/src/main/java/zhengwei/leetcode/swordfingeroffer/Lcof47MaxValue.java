package zhengwei.leetcode.swordfingeroffer;

/**
 * 在一个 m*n 的棋盘的每一格都放有一个礼物，每个礼物都有一定的价值（价值大于 0）。你可以从棋盘的左上角开始拿格子里的礼物，
 * 并每次向右或者向下移动一格、直到到达棋盘的右下角。给定一个棋盘及其上面的礼物的价值，请计算你最多能拿到多少价值的礼物？
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/li-wu-de-zui-da-jie-zhi-lcof
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/3 19:11
 */
public class Lcof47MaxValue {
    public static int maxValue(int[][] grid) {
        //转移方程
        //dp[0][0] = grid[0][0]
        //dp[0][i] = grid[0][i] + dp[0][i - 1]
        //dp[i][0] = grid[i][0] + dp[i - 1][0]
        //dp[i][j] = grid[i][j] + max(dp[i - 1][j], dp[i][j - 1])
        int rows = grid.length;
        int cols = grid[0].length;
        //初始化第一行
        for (int i = 1; i < cols; i++) {
            grid[0][i] += grid[0][i - 1];
        }
        //初始化第一列
        for (int i = 1; i < rows; i++) {
            grid[i][0] += grid[i - 1][0];
        }
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                grid[i][j] += Math.max(grid[i - 1][j], grid[i][j - 1]);
            }
        }
        return grid[rows - 1][cols - 1];
    }

    public static void main(String[] args) {
        int[][] ints = new int[][]{{1, 2, 5}, {3, 2, 1}};
        System.out.println(maxValue(ints));
    }
}
