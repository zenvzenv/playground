package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * 455. 分发饼干
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/25 15:33
 */
public class LeetCode455FindContentChildren {
    //贪心算法
    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(g);
        Arrays.sort(s);
        int result = 0, i = 0;
        for (int j = 0; i < g.length && j < s.length; j++) {
            if (s[j] >= g[i]) {
                result++;
                i++;
            }
        }
        return result;
    }
}
