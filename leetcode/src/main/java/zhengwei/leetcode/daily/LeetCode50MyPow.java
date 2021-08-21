package zhengwei.leetcode.daily;

/**
 * LeetCode第50题：pow(x, n)
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/1 11:20
 */
public class LeetCode50MyPow {
    public static double myPow(double x, int n) {
        return n >= 0 ? pow(x, n) : 1 / pow(x, n);
    }

    private static double pow(double x, int n) {
        if (n == 0) return 1.0;
        double y = pow(x, (n >> 1));
        return n % 2 == 0 ? y * y : y * y * x;
    }

    public static void main(String[] args) {
        System.out.println(myPow(2.0, 10));
    }
}
