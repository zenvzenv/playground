package zhengwei.leetcode.daily;

/**
 * LeetCode第33题：搜索旋转排序数组
 * @author zhengwei AKA Awei
 * @since 2020/4/27 15:38
 */
public class LeetCode33Search {
    private static int search(int[] nums, int target) {
        int len = nums.length;
        int left = 0, right = len - 1;
        while (left <= right) {
            int middle = (left + right) >> 1;
            if (nums[middle] == target) return middle;
            //左边有序
            if (nums[0] <= nums[middle]) {
                if (nums[0] <= target && target < nums[middle]) {
                    right = middle - 1;
                } else {
                    left = middle + 1;
                }
            } else {//右边有序
                if (nums[middle] < target && target <= nums[len - 1]) {
                    left = middle + 1;
                } else {
                    right = middle - 1;
                }
            }
        }
        return -1;
    }
}
