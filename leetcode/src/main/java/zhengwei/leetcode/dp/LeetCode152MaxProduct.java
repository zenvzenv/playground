package zhengwei.leetcode.dp;

/**
 * 152. 乘积最大子数组
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/16
 */
public class LeetCode152MaxProduct {
    public static int maxProduct(int[] nums) {
        final int len = nums.length;
        // 到当前元素的最大乘积
        int imax = 1;
        // 到当前元素的最小乘积
        int imin = 1;
        int result = Integer.MIN_VALUE;
        for (int num : nums) {
            // 当当前值是负数时，因为我们维护了两个数组，最大乘积和最小乘积数组
            // 最小值乘上负数就变成了最大值，最大值乘上负数就变成了最小值
            // 所以互换最大值和最小值
            if (num < 0) {
                imax ^= imin;
                imin ^= imax;
                imax ^= imin;
            }
            imax = Math.max(num, imax * num);
            imin = Math.min(num, imin * num);
            result = Math.max(result, imax);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(maxProduct(new int[]{-2}));
    }
}
