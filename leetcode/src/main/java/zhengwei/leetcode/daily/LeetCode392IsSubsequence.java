package zhengwei.leetcode.daily;

/**
 * LeetCode392题：判断子序列
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/27 17:03
 */
public class LeetCode392IsSubsequence {
    //双指针
    public static boolean isSubsequence(String s, String t) {
        int i = 0, j = 0;
        while (i < s.length() && j < t.length()) {
            if (s.charAt(i) == t.charAt(j)) i++;
            j++;
        }
        return i == s.length();
    }

    public static void main(String[] args) {
        System.out.println(isSubsequence("abc", "ahbgdc"));
    }
}
