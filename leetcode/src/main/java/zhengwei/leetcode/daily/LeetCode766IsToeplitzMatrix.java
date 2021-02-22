package zhengwei.leetcode.daily;

/**
 * 766. 托普利茨矩阵
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/22 9:22
 */
public class LeetCode766IsToeplitzMatrix {
    public boolean isToeplitzMatrix(int[][] matrix) {
        final int rows = matrix.length, cols = matrix[0].length;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols - 1; j++) {
                if (matrix[i][j] != matrix[i + 1][j + 1]) {
                    return false;
                }
            }
        }
        return true;
    }
}
