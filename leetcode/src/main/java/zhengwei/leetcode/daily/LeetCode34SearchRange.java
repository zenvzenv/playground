package zhengwei.leetcode.daily;

/**
 * 34. 在排序数组中查找元素的第一个和最后一个位置
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/1 9:16
 */
public class LeetCode34SearchRange {
    public int[] searchRange(int[] nums, int target) {
        int[] ans = new int[]{-1, -1};
        if (null == nums || 0 == nums.length) return ans;
        final int left = findLeft(nums, target);
        if (-1 != left) {
            ans[0] = left;
            final int right = findRight(nums, target);
            ans[1] = right;
        }
        return ans;
    }

    /**
     * 寻找第一个大于等于 target 的值
     *
     * @param nums   待寻找的数组
     * @param target 目标值
     * @return 第一个大于等于 target 的下标值
     */
    private static int findLeft(int[] nums, int target) {
        int l = 0, r = nums.length - 1;
        while (l <= r) {
            int m = (l + r) >>> 1;
            if (nums[m] < target) {
                l = m + 1;
            } else if (nums[m] == target) {
                r = m - 1;
            } else {
                r = m - 1;
            }
        }
        if (l > nums.length - 1 || nums[l] != target) return -1;
        return l;
    }

    /**
     * 寻找第一个大于 target 的值
     *
     * @param nums   待寻找数组
     * @param target 目标值
     * @return 第一个大于 target 的索引下标
     */
    private static int findRight(int[] nums, int target) {
        int l = 0, r = nums.length - 1;
        while (l <= r) {
            int m = (l + r) >>> 1;
            if (nums[m] < target) {
                l = m + 1;
            } else if (nums[m] == target) {
                l = m + 1;
            } else {
                r = m - 1;
            }
        }
        return l - 1;
    }

    public static void main(String[] args) {
        System.out.println(findLeft(new int[]{2, 2}, 3));
        System.out.println(findRight(new int[]{5, 7, 7, 8, 8, 10}, 0));
        System.out.println(findRight(new int[]{1}, 0));
    }
}
