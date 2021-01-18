package zhengwei.leetcode.daily;

/**
 * 189. 旋转数组
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/8 9:21
 */
public class LeetCode189Rotate {
    public void rotate1(int[] nums, int k) {
        final int length = nums.length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[(i + k) % length] = nums[i];
        }
        System.arraycopy(result, 0, nums, 0, length);
    }

    public void rotate2(int[] nums, int k) {
        final int length = nums.length;
        k %= length;
        reverse(nums, 0, length - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, length - 1);
    }

    private void reverse(int[] nums, int start, int end) {
        while (start <= end) {
            swap(nums, start, end);
            start++;
            end--;
        }
    }

    private void swap(int[] nums, int i, int j) {
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }
}
