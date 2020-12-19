package zhengwei.leetcode.daily;

/**
 * 48. 旋转图像
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/19 9:54
 */
public class LeetCode48Rotate {
    public void rotate(int[][] matrix) {
        int rows = matrix.length;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                matrix[i][j] ^= matrix[j][i];
                matrix[j][i] ^= matrix[i][j];
                matrix[i][j] ^= matrix[j][i];
            }
        }

        int middle = rows >>> 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < middle; j++) {
                matrix[i][rows - 1 - j] ^= matrix[i][j];
                matrix[i][j] ^= matrix[i][rows - 1 - j];
                matrix[i][rows - 1 - j] ^= matrix[i][j];
            }
        }
    }
}
