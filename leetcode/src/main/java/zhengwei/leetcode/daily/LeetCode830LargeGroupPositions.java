package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 830. 较大分组的位置
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/5 9:17
 */
public class LeetCode830LargeGroupPositions {
    public static List<List<Integer>> largeGroupPositions(String s) {
        if (null == s || 0 == s.length()) return new ArrayList<>(0);
        List<List<Integer>> result = new ArrayList<>();
        final char[] cs = s.toCharArray();
        for (int p = 0, q = 0; q <= cs.length; q++) {
            if (q == cs.length || cs[p] != cs[q]) {
                if (q - p >= 3) {
                    result.add(Arrays.asList(p, q - 1));
                }
                p = q;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        /*System.out.println(largeGroupPositions("abbxxxxzzy"));
        System.out.println(largeGroupPositions("abcdddeeeeaabbbcd"));*/
        System.out.println(largeGroupPositions("aaa"));
    }
}
