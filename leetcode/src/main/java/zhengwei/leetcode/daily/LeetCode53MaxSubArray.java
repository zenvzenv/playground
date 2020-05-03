package zhengwei.leetcode.daily;

/**
 * LeetCode第53题：最大子序和
 * 1. 贪心法
 * 2. 动态规划
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/3 8:53
 */
public class LeetCode53MaxSubArray {
    public static void main(String[] args) {
        System.out.println(maxSubArray1(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}));
//        System.out.println(maxSubArray1(new int[]{-1}));
    }

    //贪心算法
    private static int maxSubArray1(int[] nums) {
        if (nums.length == 0) return 0;
        int currSum = 0;
        int maxSum = nums[0];
        for (int num : nums) {
            //如果之前的子序列的和比当前的元素的和要小，那么就丢弃，重新开始计算和
            currSum = Math.max(num, currSum + num);
            maxSum = Math.max(currSum, maxSum);
        }
        return maxSum;
    }
}
