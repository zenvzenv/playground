package zhengwei.leetcode.lcof;

/**
 * 剑指offer第16题：数值的整数次方
 *
 * 例如：3^17 -> 3^(1111)b -> 3<sup>2^3</sup> * 3<sup>2^2</sup> * 3<sup>2^1</sup> * 3<sup>2^0</sup>
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/16 13:52
 */
public class Lcof16MyPow {
    public static double myPow(double x, int n) {
        if (x == 0) return 0;
        double res = 1.0;
        long t = n;
        if (t < 0) {
            t = -t;
            x = 1 / x;
        }
        while (t > 0) {
            if ((t & 1) == 1) res *= x;
            x *= x;
            t >>= 1;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(myPow(2.0, 10));
    }
}
