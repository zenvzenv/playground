package zhengwei.leetcode.swordfingeroffer;

import java.util.Arrays;

/**
 * 剑指offer第47题
 * 顺时针遍历二维数组
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/5 21:00
 */
public class Lcof47SpiralOrder {
    private static int[] spiralOrder(int[][] matrix) {
        int top = 0;
        int bottom = matrix.length - 1;
        int left = 0;
        int right = matrix[0].length - 1;
        int index = 0;
        int[] res = new int[(bottom + 1) * (right + 1)];
        while (true) {
            //从左往右
            for (int i = left; i <= right; i++) res[index++] = matrix[top][i];
            top++;
            if (top > bottom) break;
            //从上到下
            for (int i = top; i <= bottom; i++) res[index++] = matrix[i][right];
            right--;
            if (right < left) break;
            //从右到左
            for (int i = right; i >= left; i--) res[index++] = matrix[bottom][i];
            bottom--;
            if (bottom < top) break;
            //从下到上
            for (int i = bottom; i >= top; i--) res[index++] = matrix[i][left];
            left++;
            if (left > right) break;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(spiralOrder(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}})));
    }
}
