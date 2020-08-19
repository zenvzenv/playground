package zhengwei.leetcode.daily;

/**
 * 647. 回文子串
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/19 9:16
 */
public class LeetCode647CountSubstrings {
    //暴力法
    public static int countSubstrings(String s) {
        int res = 0;
        for (int i = 0; i < s.length(); i++) {
            for (int j = i; j < s.length(); j++) {
                if (isPalindrome(s, i, j)) res++;
            }
        }
        return res;
    }

    private static boolean isPalindrome(String s, int left, int right) {
        while (left < right) {
            if (s.charAt(left) == s.charAt(right)) {
                left++;
                right--;
            } else return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(countSubstrings("abc"));
    }
}
