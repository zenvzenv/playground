package zhengwei.leetcode.doubleweek30;

import java.util.Arrays;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/13 10:13
 */
public class LeetCode1508RangeSum {
    public static int rangeSum(int[] nums, int n, int left, int right) {
        int length = n * (n + 1) / 2;
        int[] newNums = new int[length];
        int index = 0;
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = i; j < n; j++) {
                sum += nums[j];
                if (j == i) {
                    newNums[index] = nums[i];
                } else {
                    newNums[index] = sum;
                }
                index++;
            }
        }
        Arrays.sort(newNums);
        System.out.println(Arrays.toString(newNums));
        int res = 0;
        for (int i = left - 1; i <= right - 1; i++) {
            res += newNums[i];
        }
        return res;
    }
}
