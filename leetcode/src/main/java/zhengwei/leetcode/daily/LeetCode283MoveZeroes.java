package zhengwei.leetcode.daily;

/**
 * 283. 移动零
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/19 9:09
 */
public class LeetCode283MoveZeroes {
    /**
     * 两遍遍历，
     * 第一遍将所有非0的数字移动到数组的左边，
     * 第二遍遍历将数组剩下的数全部补0
     *
     * @param nums 待移动数组
     */
    public void moveZeroes1(int[] nums) {
        if (null == nums) return;
        int j = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                nums[j++] = nums[i];
            }
        }
        for (int i = j; i < nums.length; i++) {
            nums[i] = 0;
        }
    }

    /**
     * 一次遍历，
     * left 指针指向非0的数组的最后一个位置，
     * right 指针指向为0数组的第一个位置
     *
     * @param nums 待移动数组
     */
    public void moveZeroes(int[] nums) {
        if (null == nums) return;
        int length = nums.length, left = 0, right = 0;
        while (right < length) {
            if (nums[right] != 0) {
                swap(nums, left, right);
                left++;
            }
            right++;
        }
    }

    private void swap(int[] nums, int i, int j) {
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }

    public static void main(String[] args) {
        int a = 1, b = 2;
        a ^= b;
        b ^= a;
        a ^= b;
        System.out.println(a);
        System.out.println(b);
    }
}
