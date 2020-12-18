package zhengwei.leetcode.daily;

/**
 * 389. 找不同
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/18 9:04
 */
public class LeetCode389FindTheDifference {
    public char findTheDifference(String s, String t) {
        s = s + t;
        char result = 0;
        for (int i = 0; i < s.length(); i++) {
            result ^= s.charAt(i);
        }
        return result;
    }
}
