package zhengwei.leetcode.daily;

/**
 * 303. 区域和检索 - 数组不可变
 *
 * @author zhengwei AKA zenv
 * @since 2021/3/1 9:26
 */
public class LeetCode303NumArray {
    private final int[] sums;

    //前缀和
    public LeetCode303NumArray(int[] nums) {
        this.sums = new int[nums.length + 1];
        for (int i = 0; i < nums.length; i++) {
            sums[i + 1] = sums[i] + nums[i];
        }
    }

    public int sumRange(int i, int j) {
        return sums[j + 1] - sums[i];
    }
}
