package zhengwei.leetcode.daily;

import java.util.Arrays;

/**
 * 832. 翻转图像
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/24 14:40
 */
public class LeetCode832FlipAndInvertImage {
    public static int[][] flipAndInvertImage(int[][] A) {
        for (int[] arr : A) {
            reverse(arr);
        }
        return A;
    }

    private static void swap(int[] arr, int a, int b) {
        arr[a] ^= arr[b];
        arr[b] ^= arr[a];
        arr[a] ^= arr[b];
    }

    private static void reverse(int[] arr) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            arr[left] ^= 1;
            arr[right] ^= 1;
            swap(arr, left, right);
            left++;
            right--;
        }
        if (left == right) {
            arr[left] ^= 1;
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(flipAndInvertImage(new int[][]{{1, 1, 0}, {1, 0, 1}, {0, 0, 0}})));
    }
}
