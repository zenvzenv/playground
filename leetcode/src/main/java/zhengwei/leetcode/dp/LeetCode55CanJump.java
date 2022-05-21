package zhengwei.leetcode.dp;

/**
 * 55. 跳跃游戏
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/15
 */
public class LeetCode55CanJump {
    public boolean canJump(int[] nums) {
        int flag = 0, len = nums.length;
        for (int i = 0; i < len; i++) {
            if (i > flag) return false;
            flag = Math.max(flag, nums[i] + flag);
        }
        return true;
    }
}
