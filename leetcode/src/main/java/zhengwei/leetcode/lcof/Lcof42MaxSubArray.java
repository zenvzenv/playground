package zhengwei.leetcode.lcof;

/**
 * 剑指offer第42题：连续子数组的最大和
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/28 17:21
 */
public class Lcof42MaxSubArray {
    //dp
    public int maxSubArray(int[] nums) {
        int res = nums[0];
        for (int i = 1; i < nums.length; i++) {
            if (nums[i - 1] > 0) nums[i] = nums[i] + nums[i - 1];
            res = Math.max(res, nums[i]);
        }
        return res;
    }
}
