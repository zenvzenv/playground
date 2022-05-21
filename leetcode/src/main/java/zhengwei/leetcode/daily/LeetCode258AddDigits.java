package zhengwei.leetcode.daily;

/**
 * 258. 各位相加
 *
 * @author zhengwei AKA zenv
 * @since 2022/3/3
 */
public class LeetCode258AddDigits {
    public int addDigits(int num) {
        while (num >= 10) {
            int temp = 0;
            while (num > 0) {
                temp += num % 10;
                num /= 10;
            }
            num = temp;
        }
        return num;
    }
}
