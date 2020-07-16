package zhengwei.leetcode.daily;

/**
 * LeetCode第1343题：大小为 K 且平均值大于等于阈值的子数组数目
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/16 11:41
 */
public class LeetCode1343NumOfSubarrays {
    public static int numOfSubarrays(int[] arr, int k, int threshold) {
        int res = 0;
        int length = arr.length;
        for (int i = 0; i <= length - k; i++) {
            int sum = 0;
            for (int j = i; j < i + k; j++) {
                sum += arr[j];
            }
            if (sum / k >= threshold)
                res++;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(numOfSubarrays(new int[]{2, 2, 2, 2, 5, 5, 5, 8}, 3, 4));
    }
}
