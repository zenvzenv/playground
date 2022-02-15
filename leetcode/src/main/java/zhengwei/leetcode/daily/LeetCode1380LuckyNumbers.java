package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 1380. 矩阵中的幸运数
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/15
 */
public class LeetCode1380LuckyNumbers {
    public List<Integer> luckyNumbers(int[][] matrix) {
        final int rowLen = matrix.length, colLen = matrix[0].length;
        final int[] rows = new int[rowLen], cols = new int[colLen];
        Arrays.fill(rows, Integer.MAX_VALUE);
        for (int i = 0; i < rowLen; i++) {
            for (int j = 0; j < colLen; j++) {
                rows[i] = Math.min(rows[i], matrix[i][j]);
                cols[j] = Math.max(cols[j], matrix[i][j]);
            }
        }
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < rowLen; i++) {
            for (int j = 0; j < colLen; j++) {
                if (matrix[i][j] == rows[i] && matrix[i][j] == cols[j]) {
                    result.add(matrix[i][j]);
                }
            }
        }
        return result;
    }
}
