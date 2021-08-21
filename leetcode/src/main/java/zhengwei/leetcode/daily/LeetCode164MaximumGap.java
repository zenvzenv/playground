package zhengwei.leetcode.daily;

/**
 * @author zhengwei AKA zenv
 * @since 2020/11/26 19:29
 */
public class LeetCode164MaximumGap {
    /*public int maximumGap(int[] nums) {
        final int length = nums.length;
        if (length < 2) return 0;
        int min = Integer.MAX_VALUE, max = -1;
        for (int n : nums) {
            min = Math.min(min, n);
            max = Math.max(max, n);
        }
        if (max - min == 0) return 0;
        int[] bucketMin = new int[length - 1];
        Arrays.fill(bucketMin, -1);
        int[] bucketMax = new int[length - 1];
        Arrays.fill(bucketMax, Integer.MAX_VALUE);

        //确定桶间距
        final int interval = (int) Math.ceil((double) (max - min) / (length - 1));
        for (int i =0;i<length;i++){
            //找到每一个值对应的桶的位置
            final int index = (nums[i] - min) / interval;
            if (nums[i]==min||nums[i]==max) continue;

        }
    }*/
}
