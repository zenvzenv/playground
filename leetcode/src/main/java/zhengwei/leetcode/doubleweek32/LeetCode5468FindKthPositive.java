package zhengwei.leetcode.doubleweek32;

public class LeetCode5468FindKthPositive {
    public static int findKthPositive(int[] arr, int k) {
        int[] temp = new int[1000];
        for (int value : arr) {
            temp[value] = 1;
        }
        for (int i = 1; i < 1000; i++) {
            if (temp[i] == 0) k--;
            if (k == 0) return i;
        }
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(findKthPositive(new int[]{1, 2}, 1));
        System.out.println(findKthPositive(new int[]{2, 3, 4, 7, 11}, 5));
    }
}
