package zhengwei.LeetCode.Daily;

/**
 * LeetCode第1248题：统计最美子数据</p>
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/21 16:20
 */
public class LeetCode1248NumberOfSubarrays {
    public static void main(String[] args) {
        System.out.println(numberOfSubarrays(new int[]{1, 1, 2, 1, 1}, 3));
//        System.out.println(numberOfSubarrays2(new int[]{1, 1, 2, 1, 1}, 3));
    }

    private static int numberOfSubarrays(int[] nums, int k) {
        int len = nums.length;
        int[] oddIndex = new int[len + 2];
        //实际的奇数索引下标最大值，feed+1=奇数的个数
        int feed = 0;
        oddIndex[feed] = -1;
        //把所有奇数索引加入到数据中
        for (int i = 0; i < len; i++) {
            if ((nums[i] & 1) == 1) {
                oddIndex[++feed] = i;
            }
        }
        oddIndex[++feed] = len;
        int res = 0;
        for (int i = 1; i + k <= feed; i++) {
            res += (oddIndex[i] - oddIndex[i - 1]) * (oddIndex[i + k] - oddIndex[i + k - 1]);
        }
        return res;
    }
}
