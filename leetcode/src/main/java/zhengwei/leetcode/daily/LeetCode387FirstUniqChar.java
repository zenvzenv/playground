package zhengwei.leetcode.daily;

/**
 * 387. 字符串中的第一个唯一字符
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/23 9:26
 */
public class LeetCode387FirstUniqChar {
    public int firstUniqChar(String s) {
        final int[] counts = new int[26];
        final char[] sChars = s.toCharArray();
        for (char sChar : sChars) {
            counts[sChar - 'a']++;
        }
        for (int i = 0; i < sChars.length; i++) {
            if (counts[sChars[i] - 'a'] == 1) return i;
        }
        return -1;
    }
}
