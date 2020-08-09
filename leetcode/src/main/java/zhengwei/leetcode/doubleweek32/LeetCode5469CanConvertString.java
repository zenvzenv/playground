package zhengwei.leetcode.doubleweek32;

public class LeetCode5469CanConvertString {
    public static boolean canConvertString(String s, String t, int k) {
        int sLen = s.length(), tLen = t.length();
        if (sLen != tLen) return false;
        int[] steps = new int[26];
        return false;
    }

    public static void main(String[] args) {
        System.out.println(canConvertString("input", "ouput", 9));
        System.out.println(canConvertString("abc", "bcd", 10));
    }
}
