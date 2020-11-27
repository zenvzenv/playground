package zhengwei.leetcode.daily;

import java.util.HashMap;
import java.util.Map;

/**
 * 454. 四数相加 II
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/27 9:44
 */
public class LeetCode454FourSumCount {
    public int fourSumCount(int[] A, int[] B, int[] C, int[] D) {
        Map<Integer, Integer> countAB = new HashMap<>();
        for (int a : A) {
            for (int b : B) {
                final int sum = a + b;
                countAB.put(sum, countAB.getOrDefault(sum, 0) + 1);
            }
        }
        int ans = 0;
        for (int c : C) {
            for (int d : D) {
                final int sum = -c - d;
                if (countAB.containsKey(sum)) {
                    ans += countAB.get(sum);
                }
            }
        }
        return ans;
    }
}
