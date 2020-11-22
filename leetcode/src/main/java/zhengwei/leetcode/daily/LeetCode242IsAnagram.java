package zhengwei.leetcode.daily;

/**
 * 242. 有效的字母异位词
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/22 10:18
 */
public class LeetCode242IsAnagram {
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] chars = new int[26];
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            chars[c - 'a']++;
        }
        for (int i = 0; i < s.length(); i++) {
            final char c = t.charAt(i);
            chars[c - 'a']--;
            if (chars[c - 'a'] < 0) return false;
        }
        return true;
    }
}
