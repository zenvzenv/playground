package zhengwei.leetcode.signalweek197;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/12 13:24
 */
public class LeetCode5460NumIdenticalPairs {
    public int numIdenticalPairs(int[] nums) {
        int res = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] == nums[j]) {
                    res++;
                }
            }
        }
        return res;
    }
}
