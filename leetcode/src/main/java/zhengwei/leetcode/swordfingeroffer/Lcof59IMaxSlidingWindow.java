package zhengwei.leetcode.swordfingeroffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhengwei AKA Awei
 * @since 2020/6/2 15:06
 */
public class Lcof59IMaxSlidingWindow {
    public static int[] maxSlidingWindow(int[] nums, int k) {
        int left = 0;
        int len = nums.length;
        if (len == 0) return new int[0];
        List<Integer> list = new ArrayList<>();

        while ((left + k - 1) <= len - 1) {
            int max = nums[left];
            for (int i = left; i <= left + k - 1; i++) {
                max = Math.max(max, nums[i]);
            }
            left++;
            list.add(max);
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(maxSlidingWindow(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3)));
    }
}
