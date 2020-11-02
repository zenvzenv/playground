package zhengwei.leetcode.daily;

import java.util.HashSet;
import java.util.Set;

/**
 * 349. 两个数组的交集
 *
 * @author zhengwei AKA Awei
 * @since 2020/11/2 9:07
 */
public class LeetCode349Intersection {
    public int[] intersection(int[] nums1, int[] nums2) {
        final Set<Integer> set1 = new HashSet<>();
        final Set<Integer> set2 = new HashSet<>();
        for (int i : nums1) {
            set1.add(i);
        }
        for (int i : nums2) {
            set2.add(i);
        }
        return getIntersection(set1, set2);
    }

    private int[] getIntersection(Set<Integer> set1, Set<Integer> set2) {
        if (set1.size() > set2.size()) {
            return getIntersection(set2, set1);
        }
        final Set<Integer> set = new HashSet<>();
        for (int i : set1) {
            if (set2.contains(i)) {
                set.add(i);
            }
        }
        final int[] ans = new int[set.size()];
        int index = 0;
        for (int i : set) {
            ans[index++] = i;
        }
        return ans;
    }
}
