package zhengwei.leetcode.daily;

/**
 * @author zhengwei AKA Awei
 * @since 2020/9/3 9:17
 */
public class LeetCode26RemoveDuplicates {
    public static int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;
        int p = 0, q = 1;
        while (q < nums.length) {
            if (nums[p] != nums[q]) {
                nums[p+1]=nums[q];

            }
        }
        return p;
    }

    public static void main(String[] args) {
        System.out.println(removeDuplicates(new int[]{0, 0, 1, 1, 2, 2, 3, 3, 4}));
    }
}
