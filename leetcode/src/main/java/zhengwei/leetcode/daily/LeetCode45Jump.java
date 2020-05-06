package zhengwei.leetcode.daily;

/**
 * LeetCode第45题：跳跃游戏 II
 * 贪心算法
 *
 * @author zhengwei AKA Awei
 * @since 2020/5/4 9:14
 */
public class LeetCode45Jump {
    private static int jump(int[] nums) {
        //能够跳的最远的距离
        int maxPos = 0;
        //跳跃的步数
        int step = 0;
        //当前能够到达的最后一个的索引
        int endFlag = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            //nums[i]+i代表了在i这个索引位上能够跳的最远距离
            //maxPos表示了
            maxPos = Math.max(maxPos, nums[i] + i);
            //此处表示已经遍历当当前能够跳到的最大的位置
            //需要进行下一轮跳跃
            if (i == endFlag) {
                step++;
                endFlag = maxPos;
            }
        }
        return step;
    }

    public static void main(String[] args) {
        System.out.println(jump(new int[]{2, 3, 1, 1, 4}));
    }
}
