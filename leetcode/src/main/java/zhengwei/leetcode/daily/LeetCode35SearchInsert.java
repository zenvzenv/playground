package zhengwei.leetcode.daily;

/**
 * LeetCode第35题：查找最小的插入位置
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/17 9:09
 */
public class LeetCode35SearchInsert {
    public static int searchInsert(int[] nums, int target) {
        int left = 0;
        int right = nums.length;
        while (left < right) {
            int middle = left + (right - left) / 2;
            if (nums[middle] == target) {
                right = middle;
            } else if (nums[middle] > target) {
                right = middle;
            } else if (nums[middle] < target) {
                left = middle + 1;
            }
        }
        return left;
    }
}
