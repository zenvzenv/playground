package zhengwei.leetcode.daily;

/**
 * LeetCode第69题：求一个数的算数平方根
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/9 13:52
 */
public class LeetCode69MySqrt {
    private static int mySqrt(int x) {
        int left = 0;
        int right = x;
        while (left < right) {
            int middle = (left + right + 1) >>> 1;
            if (middle * middle > x) {
                right = middle - 1;
            } else {
                left = middle;
            }
        }
        return left;
    }

    public static void main(String[] args) {
        System.out.println(mySqrt(4));
    }
}
