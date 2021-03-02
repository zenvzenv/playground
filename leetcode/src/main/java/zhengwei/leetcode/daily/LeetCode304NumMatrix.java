package zhengwei.leetcode.daily;

/**
 * 304. 二维区域和检索 - 矩阵不可变
 *
 * @author zhengwei AKA zenv
 * @since 2021/3/2 10:03
 */
public class LeetCode304NumMatrix {
    private int[][] oneDimensional;

    //一维前缀和
    public LeetCode304NumMatrix(int[][] matrix) {
        final int rows = matrix.length;
        if (rows > 0) {
            final int cols = matrix[0].length;
            oneDimensional = new int[rows][cols + 1];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    oneDimensional[i][j + 1] = matrix[i][j] + oneDimensional[i][j];
                }
            }
        }
    }

    public int sumRegion(int row1, int col1, int row2, int col2) {
        int ans = 0;
        for (int i = row1; i <= row2; i++) {
            ans += (oneDimensional[i][col2 + 1] - oneDimensional[i][col1]);
        }
        return ans;
    }
}
