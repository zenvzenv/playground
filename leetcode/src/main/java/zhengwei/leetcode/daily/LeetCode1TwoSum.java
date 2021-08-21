package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * @author zhengwei AKA Awei
 * @since 2020/6/9 20:09
 */
public class LeetCode1TwoSum {
    public static int[] twoSum(int[] nums, int target) {
        int[] res = new int[2];
        int index = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > target) i++;
            else {
                if (index >= 1) break;
                res[index] = i;
                index += 1;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(twoSum(new int[]{2, 7, 11, 15}, 9)));
    }
}
