package zhengwei.leetcode.daily;

/**
 * 338. 比特位计数
 *
 * @author zhengwei AKA zenv
 * @since 2021/3/3 9:22
 */
public class LeetCode338CountBits {
    public int[] countBits1(int num) {
        final int[] ans = new int[num + 1];
        for (int i = 0; i <= num; i++) {
            ans[i] = bitCount(i);
        }
        return ans;
    }

    private int bitCount(int n) {
        int count = 0;
        while (n > 0) {
            n &= (n - 1);
            count++;
        }
        return count;
    }

    /**
     * 1. 奇数：二进制表示中，奇数一定比前面那个偶数多一个 1，因为多的就是最低位的 1。
     * 2. 偶数：二进制表示中，偶数中 1 的个数一定和除以 2 之后的那个数一样多。因为最低位是 0，除以 2 就是右移一位，
     *    也就是把那个 0 抹掉而已，所以 1 的个数是不变的。
     */
    public int[] countBits(int num) {
        final int[] ans = new int[num + 1];
        for (int i = 1; i < ans.length; i++) {
            if ((i & 1) == 0) {
                ans[i] = ans[i >> 1];
            } else {
                ans[i] = ans[i >> 1] + 1;
            }
        }
        return ans;
    }
}
