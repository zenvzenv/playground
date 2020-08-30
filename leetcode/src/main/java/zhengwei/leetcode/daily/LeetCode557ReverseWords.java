package zhengwei.leetcode.daily;

/**
 * 557. 反转字符串中的单词 III
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/30 10:24
 */
public class LeetCode557ReverseWords {
    public static String reverseWords(String s) {
        StringBuilder res = new StringBuilder();
        int right = 0;
        while (right < s.length()) {
            int left = right;
            while (right < s.length() && s.charAt(right) != ' ') {
                right++;
            }
            reverse(res, s, left, right);
            while (right < s.length() && s.charAt(right) == ' ') {
                right++;
                res.append(' ');
            }
        }
        return res.toString();
    }

    private static void reverse(StringBuilder res, String s, int l, int r) {
        for (int i = r - 1; i >= l; i--) {
            res.append(s.charAt(i));
        }
    }

    public static void main(String[] args) {
        System.out.println(reverseWords("Let's take LeetCode contest"));
    }
}
