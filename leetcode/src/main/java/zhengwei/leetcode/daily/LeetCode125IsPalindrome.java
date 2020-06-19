package zhengwei.leetcode.daily;

/**
 * LeetCode第125题：验证回文串
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/19 9:15
 */
public class LeetCode125IsPalindrome {
    public static boolean isPalindrome1(String s) {
        if (s.equals("")) return true;
        int left = 0;
        int right = s.length() - 1;
        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                ++left;
            }
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                --right;
            }
            if (left < right) {
                if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                    return false;
                }
                ++left;
                --right;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isPalindrome1("race a car"));
    }
}
