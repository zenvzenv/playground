package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * LeetCode面试题56 - I. 数组中数字出现的次数
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/28 14:10
 */
public class LeetCodeOffer56SingleNumbers {
    private static int[] singleNumbers(int[] nums) {
        //用来记录只出现一次的数据的异或值
        int x = 0;
        //得到只两个出现一次数字的异或值
        for (int num : nums) {
            x ^= num;
        }
        //得到二进制数中最低位的1(实际上随便得到哪位上的1都是可以的，只不过获得最低位的1比较方便)
        //因为异或的性质是相同为0，不同为1，所以异或值上为1的那位，之前的两个数这位上肯定是不同的
        //就利用这一位来对原数据中的数据进行分组计算，就能得到其中一个数字，再用异或值再去异或这个值就会得到另外一个数字了
        int flag = x & -x;
        int y = 0;
        for (int num : nums) {
            //对原数组进行分组
            if ((flag & num) == 0) y ^= num;
        }
        return new int[]{y, x ^ y};
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(singleNumbers(new int[]{1, 2, 2, 3, 3, 5})));
    }
}
