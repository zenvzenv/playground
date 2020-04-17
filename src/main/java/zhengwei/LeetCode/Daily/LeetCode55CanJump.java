package zhengwei.LeetCode.Daily;

/**
 * LeetCode第55题：跳跃游戏</p>
 * 利用贪心算法，
 * 只要每次都走当前索引的最大步数，只要最大的步数大于整个数组的长度就说明是能够跳跃完整个数据
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/17 9:17
 */
public class LeetCode55CanJump {
    public static void main(String[] args) {
        System.out.println(canJump(new int[]{1, 0, 1, 3, 6}));
        System.out.println(canJump(new int[]{1, 2, 3, 4, 0}));
    }

    private static boolean canJump(int[] nums) {
        int right = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i <= right) {
                right = Math.max(right, i + nums[i]);
                if (right >= nums.length) return true;
            }
        }
        return false;
    }
}
