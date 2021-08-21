package zhengwei.leetcode.daily;

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/25 9:11
 */
public class LeetCode1370SortString {
    public String sortString(String s) {
        char[] chars = new char[26];
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            chars[s.charAt(i) - 'a']++;
        }
        while (ans.length() < s.length()) {
            for (int i = 0; i < 26; i++) {
                if (chars[i] > 0) {
                    ans.append((char) (i + 'a'));
                    chars[i]--;
                }
            }
            for (int i = 25; i >= 0; i--) {
                if (chars[i] > 0) {
                    ans.append((char) (i + 'a'));
                    chars[i]--;
                }
            }
        }
        return ans.toString();
    }
}
