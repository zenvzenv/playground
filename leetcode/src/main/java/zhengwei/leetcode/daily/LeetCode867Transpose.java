package zhengwei.leetcode.daily;

/**
 * 867. 转置矩阵
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/25 9:13
 */
public class LeetCode867Transpose {
    public int[][] transpose(int[][] A) {
        final int rows = A.length, cols = A[0].length;
        final int[][] ans = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ans[j][i] = A[i][j];
            }
        }
        return ans;
    }
}
