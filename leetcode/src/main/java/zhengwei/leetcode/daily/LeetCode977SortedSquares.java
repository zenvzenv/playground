package zhengwei.leetcode.daily;

/**
 * 977. 有序数组的平方
 *
 * <p>
 *     双指针
 * </p>
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/16 9:28
 */
public class LeetCode977SortedSquares {
    public int[] sortedSquares(int[] A) {
        final int length = A.length;
        if (length == 0) return A;
        final int[] ans = new int[length];
        int left = 0, right = length - 1, index = length - 1;
        while (index >= 0) {
            //如果 A[left] + A[right] > 0 说明右边的元素的绝对值大于左边元素的绝对值
            //那么就把右边的元素的平方放到高 index 位
            if (A[left] + A[right] > 0) {
                ans[index--] = A[right] * A[right];
                right--;
            }
            //反之亦然
            else {
                ans[index--] = A[left] * A[left];
                left++;
            }
        }
        return ans;
    }
}
