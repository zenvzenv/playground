package zhengwei.leetcode.daily;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * LeetCode第350题：两个数组的交集II
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/13 9:30
 */
public class LeetCode350Intersect {
    public int[] intersect(int[] nums1, int[] nums2) {
        if (nums1.length > nums2.length) {
            //规整数据，将长数组放后面，短数组放前面
            return intersect(nums2, nums1);
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int n : nums2) {
            final Integer count = map.getOrDefault(n, 0);
            map.put(n, count);
        }
        int index = 0;
        int[] temp = new int[nums2.length];
        for (int n : nums1) {
            Integer count = map.getOrDefault(n, 0);
            if (count > 0) {
                temp[index++] = n;
                count--;
                if (count > 0) {
                    map.put(n, count);
                } else {
                    map.remove(n);
                }
            }
        }
        return Arrays.copyOfRange(temp, 0, index);
    }
}
