package zhengwei.leetcode.lcof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhengwei AKA Awei
 * @since 2020/8/6 12:51
 */
public class Lcof57 {
    //II-滑动窗口
    public static int[][] findContinuousSequence(int target) {
        List<int[]> res = new ArrayList<>();
        int i = 1;
        int j = 2;
        int sum = i + j;
        while (i <= target / 2) {
            if (sum < target) {
                j++;
                sum += j;
            } else if (sum > target) {
                sum -= i;
                i++;
            } else {
                int k = j - i;
                int[] temp = new int[k + 1];
                for (int x = 0; x < k + 1; x++) {
                    temp[x] = i + x;
                }
                res.add(temp);
                sum -= i;
                i++;
            }
        }
        return res.toArray(new int[res.size()][]);
    }

    //I-双指针
    public int[] twoSum(int[] nums, int target) {
        int i = 0, j = nums.length - 1;
        while (i <= j) {
            if (nums[i] + nums[j] == target) {
                return new int[]{nums[i], nums[j]};
            } else if (nums[i] + nums[j] > target) {
                j--;
            } else {
                i++;
            }
        }
        return new int[0];
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(findContinuousSequence(9)));
    }
}
