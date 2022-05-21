package zhengwei.leetcode.daily;

/**
 * 172. 阶乘后的零
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/25
 */
public class LeetCode172TrailingZeroes {
    // 会溢出问题
    public int trailingZeroes1(int n) {
        if (0 == n) return 0;
        int m = 1;
        while (n > 0) {
            m *= n;
            n--;
        }
        int c = 0;
        while (m % 10 == 0) {
            c++;
            m /= 10;
        }
        return c;
    }

    /**
     * 对于尾随0，就是10的倍数，即乘数中包含2*5，
     * 只要寻找乘数中的5的个数即可
     */
    public int trailingZeroes2(int n) {
        int result = 0;
        while (n > 0) {
            n /= 5;
            result += n;
        }
        return result;
    }
}
