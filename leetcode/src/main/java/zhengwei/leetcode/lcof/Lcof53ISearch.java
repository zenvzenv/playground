package zhengwei.leetcode.lcof;

/**
 * 剑指offer第53题：I. 在排序数组中查找数字 I
 * @author zhengwei AKA Awei
 * @since 2020/7/30 13:11
 */
public class Lcof53ISearch {

    public static int search2(int[] nums, int target) {
        // 搜索右边界 right
        int i = 0, j = nums.length - 1;
        while (i <= j) {
            int m = (i + j) / 2;
            if (nums[m] <= target) i = m + 1;
            else j = m - 1;
        }
        int right = i;
        // 若数组中无 target ，则提前返回
        if (j >= 0 && nums[j] != target) return 0;
        // 搜索左边界 right
        i = 0;
        j = nums.length - 1;
        while (i <= j) {
            int m = (i + j) / 2;
            if (nums[m] < target) i = m + 1;
            else j = m - 1;
        }
        int left = j;
        return right - left - 1;
    }

    public static int search1(int[] nums, int target) {
        int right = findRight(nums, target);
        if (right == -1) return 0;
        int left = findLeft(nums, target);
        return right - left - 1;
    }

    //二分法模板
    private static int findRight(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int middle = (left + right) >>> 1;
            if (nums[middle] == target) {
                left = middle + 1;
            } else if (nums[middle] < target) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }
        if (right >= 0 && nums[right] != target) return -1;
        return left;
    }

    //二分法模板
    private static int findLeft(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int middle = (left + right) >>> 1;
            if (nums[middle] == target) {
                right = middle - 1;
            } else if (nums[middle] < target) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }
        return right;
    }

    public static void main(String[] args) {
//        System.out.println(search(new int[]{5, 7, 7, 8, 8, 10}, 8));
//        System.out.println(search(new int[]{5, 7, 7, 8, 8, 10}, 6));
        System.out.println(findRight(new int[]{1}, 0));
        System.out.println(findLeft(new int[]{1}, 0));
    }
}
