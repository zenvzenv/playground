package zhengwei.leetcode.lcci;

import java.util.Arrays;

/**
 * 程序员面试金典第16.11题：跳水板
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/8 10:22
 */
public class Lcci1611DivingBoard {
    public static int[] divingBoard(int shorter, int longer, int k) {
        if (k == 0) return new int[0];
        if (shorter == longer) {
            return new int[shorter * k];
        }
        int[] res = new int[k + 1];
        for (int i = 0; i <= k; i++) {
            res[i] = shorter * (k - i) + longer * i;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(divingBoard(1, 2, 3)));
    }
}
