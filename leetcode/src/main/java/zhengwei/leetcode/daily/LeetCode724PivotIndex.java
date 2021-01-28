package zhengwei.leetcode.daily;

/**
 * 724. 寻找数组的中心索引
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/28 9:08
 */
public class LeetCode724PivotIndex {
    public int pivotIndex1(int[] nums) {
        if (null == nums || nums.length == 0) return -1;
        for (int i = 0; i < nums.length; i++) {
            final int sum1 = sum(nums, 0, i);
            final int sum2 = sum(nums, i + 1, nums.length);
            if (sum1 == sum2) return i;
        }
        return -1;
    }

    private int sum(int[] nums, int s, int e) {
        int sum = 0;
        for (int i = s; i < e; i++) {
            sum += nums[i];
        }
        return sum;
    }

    /**
     * 前缀和
     */
    public int pivotIndex2(int[] nums) {
        if (null == nums || 0 == nums.length) return -1;
        int total = 0;
        for (int num : nums) {
            total += num;
        }
        int sum = 0;
        for (int i = 0; i < nums.length; i++) {
            if (2 * sum + nums[i] == total) {
                return i;
            }
            sum += nums[i];
        }
        return -1;
    }
}
