package zhengwei.leetcode.daily;

/**
 * 941. 有效的山脉数组
 *
 * @author zhengwei AKA Awei
 * @since 2020/11/3 9:01
 */
public class LeetCode941ValidMountainArray {
    public static boolean validMountainArray(int[] A) {
        final int length = A.length;
        if (3 > length) return false;
        int i = 0;
        while (i < length - 1 && A[i] < A[i + 1]) {
            i++;
        }
        if (i == 0 || i == length - 1) {
            return false;
        }
        while (i < length - 1 && A[i] > A[i + 1]) {
            i++;
        }
        return i == length - 1;
    }

    public static void main(String[] args) {
        System.out.println(validMountainArray(new int[]{0, 3, 2, 1}));
    }
}
