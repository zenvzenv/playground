package zhengwei.leetcode.week201;

public class LeetCode5484FindKthBit {
    public static char findKthBit(int n, int k) {
        String[] dp = new String[n + 1];
        dp[0] = "0";
        dp[1] = "0";
        for (int i = 2; i < dp.length; i++) {
            dp[i] = dp[i - 1] + "1" + new StringBuilder(invert(dp[i - 1])).reverse();
        }
        return dp[n].charAt(k - 1);
    }

    private static String invert(String s) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '0') res.append("1");
            else res.append("0");
        }
        return res.toString();
    }

    public static void main(String[] args) {
        System.out.println(findKthBit(4, 11));
    }
}
