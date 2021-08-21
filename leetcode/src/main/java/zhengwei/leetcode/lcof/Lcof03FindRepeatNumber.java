package zhengwei.leetcode.lcof;

/**
 * 剑指 Offer 03. 数组中重复的数字
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/25 18:31
 */
public class Lcof03FindRepeatNumber {
    public int findRepeatNumber(int[] nums) {
        int len = nums.length;
        for (int n : nums) {
            if (n < 0 || n >= len) return -1;
        }
        for (int i = 0; i < len; i++) {
            while (nums[i] != i && nums[nums[i]] != nums[i]) swap(nums, nums[i], i);
            if (nums[i] != i && nums[nums[i]] == nums[i]) return nums[i];
        }
        return -1;
    }

    private static void swap(int[] nums, int i, int j) {
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }
}
