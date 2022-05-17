package zhengwei.leetcode.dp;

/**
 * 45. 跳跃游戏 II
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/15
 */
public class LeetCode45Jump {
    // 含有贪心和 dp 思想
    public int jump(int[] nums) {
        int start = 0, end = 1, result = 0, len = nums.length;
        while (end < len) {
            int maxPos = 0;
            for (int i = start; i < end; i++) {
                maxPos = Math.max(maxPos, i + nums[i]);
            }
            result++;
            // 下一次起跳范围起始点
            start = end;
            // 下一次范围结束点
            end = maxPos + 1;
        }
        return result;
    }
}
