package zhengwei.leetcode.daily;

/**
 * 693. 交替位二进制数
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/28
 */
public class LeetCode693HasAlternatingBits {
    public boolean hasAlternatingBits(int n) {
        int a = n ^ (n >> 1);
        return (a & (a + 1)) == 0;
    }
}
