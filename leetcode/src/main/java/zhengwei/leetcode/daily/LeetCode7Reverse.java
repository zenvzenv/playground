package zhengwei.leetcode.daily;

/**
 * LeetCode第7题：反转一个整数
 * @author zhengwei AKA Awei
 * @since 2020/4/27 20:13
 */
public class LeetCode7Reverse {
    private static int reverse(int x) {
        int res = 0;
        while (x != 0) {
            int pop = x % 10;
            x /= 10;
            //Integer.MAX_VALUE = 2147483647
            //Integer.MIN_VALUE = -2147483648
            //只要res * 10 + pop > 2147483647就算溢出，也就是说res > Integer.MAX_VALUE/10就是溢出，因为最后一位是7只要弹出的值大于7也是溢出
            //只要res * 10 + pop > -2147483648就算溢出，也就是说res < Integer.MIN_VALUE/10就是溢出，因为最后一位是-8，只要小于-8就是溢出
            if (res > (Integer.MAX_VALUE / 10) || (res == Integer.MAX_VALUE / 10 && pop > 7)) return 0;
            if (res < (Integer.MIN_VALUE / 10) || (res == Integer.MIN_VALUE / 10 && pop < -8)) return 0;
            res = res * 10 + pop;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Integer.MIN_VALUE);
    }
}
