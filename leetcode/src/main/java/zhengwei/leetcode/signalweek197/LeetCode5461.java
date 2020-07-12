package zhengwei.leetcode.signalweek197;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/12 13:29
 */
public class LeetCode5461 {
    public int numSub(String s) {
        int oneCount = 0;
        int sum = 0;
        int mod = 1000000007;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '1') {
                oneCount++;
            } else {
                sum += oneCount * (oneCount + 1) / 2;
                sum %= mod;
                oneCount = 0;
            }
        }
        sum += oneCount * (oneCount + 1) / 2;
        return sum;
    }

    public int numSub2(String s) {
        String[] ones = s.split("0");
        int res = 0;
        int maxLen = 0;
        for (String str : ones) {
            maxLen = Math.max(maxLen, str.length());
        }
        int len = 1;
        while (len <= maxLen) {
            for (String one : ones) {
                if (one.length() >= len) {
                    res += (one.length() - len + 1);
                    res %= 1000000007;
                }
            }
            len++;
        }
        return res;
    }
}
