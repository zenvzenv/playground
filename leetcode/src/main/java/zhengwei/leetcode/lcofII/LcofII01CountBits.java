package zhengwei.leetcode.lcofII;

/**
 * 剑指 Offer II 003. 前 n 个数字二进制中 1 的个数
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/25
 */
public class LcofII01CountBits {
    public int[] countBits(int n) {
        final int[] result = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            int cnt = 0;
            while (i > 0) {
                if ((i & 1) == 1) cnt ++;
                i = (i >> 1);
            }
            result[i] = cnt;
        }
        return result;
    }
}
