package zhengwei.leetcode.daily;

import java.util.HashSet;

/**
 *
 */
public class LeetCode219ContainsNearbyDuplicate {
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        final HashSet<Integer> set = new HashSet<>();
        final int length = nums.length;
        for (int i = 0; i < length; i++) {
            if (set.contains(nums[i])) {
                return true;
            }
            set.add(nums[i]);
            if (set.size() > k) {
                set.remove(nums[i - k]);
            }
        }
        return false;
    }
}
