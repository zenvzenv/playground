package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * 135. 分发糖果
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/24 9:09
 */
public class LeetCode135Candy {
    public static int candy(int[] ratings) {
        final int length = ratings.length;
        int[] left = new int[length];
        int[] right = new int[length];
        //每个人最少有一个糖果
        Arrays.fill(left, 1);
        Arrays.fill(right, 1);
        //左规则，从左开始遍历，左边小于右边
        for (int i = 1; i < length - 1; i++) {
            if (ratings[i] > ratings[i - 1]) left[i] = left[i - 1] + 1;
        }
        //右规则，从右开始遍历，右边小于左边
        for (int i = length - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) right[i] = right[i + 1] + 1;
        }
        int result = 0;
        for (int i = 0; i < length; i++) {
            //取左右的最大值即可，可能从左边开始发，也可能从右边开始发
            result += Math.max(left[i], right[i]);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(candy(new int[]{1, 2, 87, 87, 87, 2, 1}));
    }
}
