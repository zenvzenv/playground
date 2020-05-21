package zhengwei.util.common;

/**
 * @author zhengwei AKA Awei
 * @since 2020/5/21 20:30
 */
public final class MyMath {
    private MyMath() {
    }

    /**
     * 辗转相除法求两个数的最大公约数
     *
     * @param a 一个数
     * @param b 另一个数
     * @return 最大公约数
     */
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
