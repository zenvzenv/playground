package zhengwei.leetcode.dp;

/**
 * 740. 删除并获得点数
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/14
 */
public class LeetCode470DeleteAndEarn {
    public int deleteAndEarn(int[] nums) {
        int max = Integer.MIN_VALUE;
        for (int num : nums) {
            max = Math.max(max, num);
        }
        final int[] sum = new int[max + 1];
        for (int n : nums) {
            sum[n] += n;
        }
        return rob(sum);
    }

    public int rob(int[] sum) {
        int p = sum[0], q = Math.max(sum[0], sum[1]), len = sum.length;
        for (int i = 2; i <= len; i++) {
            int temp = q;
            q = Math.max(q, p + sum[i]);
            p = temp;
        }
        return q;
    }
}
