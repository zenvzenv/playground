package zhengwei.leetcode.daily;

/**
 * @author zhengwei AKA Awei
 * @since 2020/10/26 9:05
 */
public class LeetCode1365SmallerNumbersThanCurrent {
    public int[] smallerNumbersThanCurrent(int[] nums) {
        final int length = nums.length;
        final int[] ans = new int[length];
        for (int i = 0; i < length; i++) {
            int count = 0;
            for (int j = 0; j < length; j++) {
                if (nums[i] > nums[j] && i != j) {
                    count++;
                }
            }
            ans[i] = count;
        }
        return ans;
    }
}
