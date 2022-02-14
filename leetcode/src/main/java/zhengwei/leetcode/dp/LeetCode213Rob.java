package zhengwei.leetcode.dp;

/**
 * 213. 打家劫舍 II
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/14
 */
public class LeetCode213Rob {
    public static int rob(int[] nums) {
        if (null == nums || 0 == nums.length) return 0;
        final int len = nums.length;
        if (1 == len) return nums[0];
        if (2 == len) return Math.max(nums[0], nums[1]);
        return Math.max(myRob(0, len - 2, nums), myRob(1, len - 1, nums));
    }

    /**
     * 对数组掐头去尾，分别对数组进行操作
     *
     * @param s
     * @param e
     * @param nums
     * @return
     */
    public static int myRob(int s, int e, int[] nums) {
        int p = nums[s], q = Math.max(nums[s], nums[s + 1]);
        // 可以取到边界
        for (int i = s + 2; i <= e; i++) {
            int temp = q;
            q = Math.max(q, p + nums[i]);
            p = temp;
        }
        return q;
    }

    public static void main(String[] args) {
        System.out.println(rob(new int[]{1, 2, 3, 1}));
    }
}
